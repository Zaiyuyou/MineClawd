package com.mineclawd.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineclawd.kubejs.KubeJsToolExecutor.ToolExecutionResult;
import com.mojang.brigadier.tree.CommandNode;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.server.command.ServerCommandSource;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ModDocsToolExecutor {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(20);
    private static final String USER_AGENT = "MineClawd/1.0";
    private static final int MAX_COMMAND_RESULTS = 500;
    private static final int MAX_SOURCE_CHARS = 120_000;
    private static final int MAX_OUTPUT_CHARS = 14_000;
    private static final int CHUNK_TARGET_CHARS = 1_200;
    private static final int CHUNK_TOP_COUNT = 3;
    private static final int MAX_LINKS = 300;

    private static final Pattern MODRINTH_LINK_PATTERN = Pattern.compile(
            "https?://(?:www\\.)?modrinth\\.com/(?:mod|plugin|datapack|resourcepack|shader|project)/([A-Za-z0-9._-]+)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern GITHUB_WIKI_PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)/wiki(?:/.*)?$");
    private static final Pattern ANCHOR_PATTERN = Pattern.compile("(?is)<a\\s+[^>]*href\\s*=\\s*(['\"])(.*?)\\1[^>]*>(.*?)</a>");
    private static final Pattern HTML_TITLE_PATTERN = Pattern.compile("(?is)<title[^>]*>(.*?)</title>");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
    private static final Pattern NON_ALNUM_PATTERN = Pattern.compile("[^a-z0-9]+");
    private static final Pattern NUMERIC_ENTITY_PATTERN = Pattern.compile("&#(x?[0-9a-fA-F]+);");

    private ModDocsToolExecutor() {
    }

    public static ToolExecutionResult listMods() {
        Collection<Mod> allMods = Platform.getMods();
        if (allMods == null || allMods.isEmpty()) {
            return new ToolExecutionResult(true, "No loaded mods found.");
        }

        Map<String, Mod> byId = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Mod mod : allMods) {
            if (mod == null || blank(mod.getModId())) {
                continue;
            }
            byId.putIfAbsent(mod.getModId(), mod);
        }

        if (byId.isEmpty()) {
            return new ToolExecutionResult(true, "No loaded mods found.");
        }

        StringBuilder out = new StringBuilder();
        out.append("Loaded mods (").append(byId.size()).append("):\n");
        for (Mod mod : byId.values()) {
            if (mod == null) {
                continue;
            }
            String id = safe(mod.getModId());
            String name = safe(mod.getName());
            String version = safe(mod.getVersion());

            out.append("- ").append(id);
            if (!blank(name) && !name.equalsIgnoreCase(id)) {
                out.append(" (").append(name).append(")");
            }
            if (!blank(version)) {
                out.append(" v").append(version);
            }
            out.append("\n");

            appendMetaLine(out, "homepage", mod.getHomepage().orElse(""));
            appendMetaLine(out, "sources", mod.getSources().orElse(""));
            appendMetaLine(out, "issues", mod.getIssueTracker().orElse(""));
        }

        return new ToolExecutionResult(true, trimToMax(out.toString().trim(), MAX_OUTPUT_CHARS));
    }

    public static ToolExecutionResult listCommands(ServerCommandSource source, String modIdFilter) {
        if (source == null || source.getServer() == null) {
            return new ToolExecutionResult(false, "No server available.");
        }

        List<String> commands = new ArrayList<>();
        for (CommandNode<ServerCommandSource> node : source.getServer().getCommandManager().getDispatcher().getRoot().getChildren()) {
            if (node == null || blank(node.getName())) {
                continue;
            }
            commands.add(node.getName());
        }

        commands = commands.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        if (!blank(modIdFilter)) {
            Set<String> aliases = buildModAliases(modIdFilter);
            commands = commands.stream()
                    .filter(name -> matchesCommandFilter(name, aliases))
                    .toList();
        }

        if (commands.isEmpty()) {
            if (blank(modIdFilter)) {
                return new ToolExecutionResult(true, "No commands available.");
            }
            return new ToolExecutionResult(
                    true,
                    "No commands matched mod_id `" + safe(modIdFilter).trim() + "` (filtering is best-effort by command name/prefix)."
            );
        }

        StringBuilder out = new StringBuilder();
        if (blank(modIdFilter)) {
            out.append("Available root commands (").append(commands.size()).append("):\n");
        } else {
            out.append("Commands matching mod_id `")
                    .append(safe(modIdFilter).trim())
                    .append("` (best-effort filter, ")
                    .append(commands.size())
                    .append("):\n");
        }

        int count = 0;
        for (String command : commands) {
            count++;
            if (count > MAX_COMMAND_RESULTS) {
                out.append("... truncated at ").append(MAX_COMMAND_RESULTS).append(" commands.\n");
                break;
            }
            out.append("- /").append(command).append("\n");
        }

        return new ToolExecutionResult(true, trimToMax(out.toString().trim(), MAX_OUTPUT_CHARS));
    }

    public static ToolExecutionResult fetchModrinth(String modId) {
        String normalizedModId = safe(modId).trim().toLowerCase(Locale.ROOT);
        if (blank(normalizedModId)) {
            return new ToolExecutionResult(false, "Missing required mod_id.");
        }

        Mod installedMod = Platform.getOptionalMod(normalizedModId).orElse(null);
        Map<String, String> contactLinks = readFabricContactLinks(installedMod);
        ModrinthProject project = resolveModrinthProject(normalizedModId, installedMod, contactLinks);
        if (project == null) {
            return new ToolExecutionResult(
                    false,
                    "Mod `" + normalizedModId + "` was not found on Modrinth. Retry with the exact installed mod id."
            );
        }

        String formatted = formatModrinthHomepage(project, "overview");
        if (blank(formatted)) {
            formatted = "No Modrinth project content was returned.";
        }
        return new ToolExecutionResult(true, trimToMax(formatted.trim(), MAX_OUTPUT_CHARS));
    }

    public static ToolExecutionResult fetchUrl(String url) {
        String normalizedUrl = safe(url).trim();
        if (blank(normalizedUrl)) {
            return new ToolExecutionResult(false, "Missing required url.");
        }
        if (!looksHttpUrl(normalizedUrl)) {
            return new ToolExecutionResult(false, "URL must start with http:// or https://.");
        }

        HttpResult response = httpGet(
                normalizedUrl,
                Map.of("Accept", "text/html, text/plain, application/json, application/xml;q=0.9, */*;q=0.8")
        );
        if (!response.success()) {
            String message = blank(response.error()) ? "HTTP " + response.statusCode() : response.error();
            return new ToolExecutionResult(false, "Failed to fetch URL: " + message);
        }

        String body = safe(response.body());
        if (blank(body)) {
            return new ToolExecutionResult(false, "Fetched URL but got empty response body.");
        }

        String content = looksLikeHtml(body) ? extractTextFromHtml(body) : normalizeTextBlock(body);
        if (blank(content)) {
            content = normalizeTextBlock(body);
        }
        if (blank(content)) {
            return new ToolExecutionResult(false, "Fetched URL but could not extract readable content.");
        }

        String sourceUrl = blank(response.finalUrl()) ? normalizedUrl : response.finalUrl();
        String output = "Source: " + sourceUrl + "\n\n" + content;
        return new ToolExecutionResult(true, trimToMax(output.trim(), MAX_OUTPUT_CHARS));
    }

    public static ToolExecutionResult fetchModDocs(String modId, String query) {
        String normalizedModId = safe(modId).trim().toLowerCase(Locale.ROOT);
        String normalizedQuery = blank(query) ? "overview" : query.trim();
        if (blank(normalizedModId)) {
            return new ToolExecutionResult(false, "Missing required mod_id.");
        }

        Mod installedMod = Platform.getOptionalMod(normalizedModId).orElse(null);
        Map<String, String> contactLinks = readFabricContactLinks(installedMod);
        ModrinthProject project = resolveModrinthProject(normalizedModId, installedMod, contactLinks);
        if (project == null) {
            return new ToolExecutionResult(
                    false,
                    "Mod `" + normalizedModId + "` was not found on Modrinth. " +
                            "Use the exact installed mod id and retry."
            );
        }

        String wikiUrl = resolveWikiUrl(project, contactLinks);
        if (blank(wikiUrl)) {
            String homepage = formatModrinthHomepage(project, normalizedQuery);
            String message = "No wiki links found for `" + normalizedModId + "`. Returning Modrinth homepage content.\n\n" + homepage;
            return new ToolExecutionResult(true, trimToMax(message.trim(), MAX_OUTPUT_CHARS));
        }

        WikiDocument wiki = fetchWikiDocument(wikiUrl, normalizedQuery);
        if (wiki == null || blank(wiki.content())) {
            String homepage = formatModrinthHomepage(project, normalizedQuery);
            String message = "Wiki URL resolved (`" + wikiUrl + "`) but no readable page content was fetched. " +
                    "Returning Modrinth homepage content.\n\n" + homepage;
            return new ToolExecutionResult(true, trimToMax(message.trim(), MAX_OUTPUT_CHARS));
        }

        String formatted = formatDocument(wiki, normalizedQuery);
        return new ToolExecutionResult(true, trimToMax(formatted.trim(), MAX_OUTPUT_CHARS));
    }

    private static String formatModrinthHomepage(ModrinthProject project, String query) {
        if (project == null) {
            return "No Modrinth project data available.";
        }

        String content = safe(project.body());
        if (blank(content)) {
            content = safe(project.description());
        }

        if (blank(content) && !blank(project.projectUrl())) {
            HttpResult response = httpGet(project.projectUrl(), Map.of("Accept", "text/html"));
            if (response.success()) {
                content = extractTextFromHtml(response.body());
            }
        }

        if (blank(content)) {
            content = "No Modrinth homepage text available.";
        }

        WikiDocument homepage = new WikiDocument(
                blank(project.projectUrl()) ? "https://modrinth.com/" : project.projectUrl(),
                blank(project.title()) ? project.slug() : project.title(),
                content,
                "modrinth-homepage"
        );
        return formatDocument(homepage, query);
    }

    private static WikiDocument fetchWikiDocument(String wikiUrl, String query) {
        WikiDocument github = fetchGitHubWikiDocument(wikiUrl, query);
        if (github != null && !blank(github.content())) {
            return github;
        }
        return fetchGenericWikiDocument(wikiUrl, query);
    }

    private static WikiDocument fetchGitHubWikiDocument(String wikiUrl, String query) {
        GitHubRepo repo = parseGitHubWikiRepo(wikiUrl);
        if (repo == null) {
            return null;
        }

        List<GitHubWikiPage> pages = new ArrayList<>();
        String rootApi = "https://api.github.com/repos/"
                + encodePathSegment(repo.owner())
                + "/"
                + encodePathSegment(repo.repo() + ".wiki")
                + "/contents";
        collectGitHubPages(rootApi, repo, 0, pages);
        if (pages.isEmpty()) {
            return null;
        }

        GitHubWikiPage selected = chooseBestGitHubPage(pages, query);
        if (selected == null) {
            selected = pages.get(0);
        }

        String content = fetchGitHubPageContent(selected);
        if (blank(content)) {
            WikiDocument fallbackHtml = fetchGenericWikiDocument(selected.webUrl(), query);
            if (fallbackHtml != null && !blank(fallbackHtml.content())) {
                return fallbackHtml;
            }
            return null;
        }

        return new WikiDocument(
                selected.webUrl(),
                selected.title(),
                normalizeTextBlock(content),
                "github-wiki"
        );
    }

    private static void collectGitHubPages(String apiUrl, GitHubRepo repo, int depth, List<GitHubWikiPage> sink) {
        if (depth > 2 || blank(apiUrl) || repo == null || sink == null) {
            return;
        }
        HttpResult response = httpGet(apiUrl, Map.of("Accept", "application/vnd.github+json"));
        if (!response.success()) {
            return;
        }

        JsonElement rootElement;
        try {
            rootElement = JsonParser.parseString(response.body());
        } catch (Exception ignored) {
            return;
        }
        if (!rootElement.isJsonArray()) {
            return;
        }

        JsonArray array = rootElement.getAsJsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String type = stringValue(entry, "type");
            if ("file".equalsIgnoreCase(type)) {
                String name = stringValue(entry, "name");
                String path = stringValue(entry, "path");
                if (blank(name) || !name.toLowerCase(Locale.ROOT).endsWith(".md")) {
                    continue;
                }
                if (name.startsWith("_")) {
                    continue;
                }
                String pagePath = path.substring(0, path.length() - 3);
                String title = humanizePageTitle(pagePath);
                String webUrl = buildGitHubWikiWebUrl(repo, pagePath);
                sink.add(new GitHubWikiPage(
                        title,
                        path,
                        webUrl,
                        stringValue(entry, "download_url"),
                        stringValue(entry, "url")
                ));
            } else if ("dir".equalsIgnoreCase(type)) {
                collectGitHubPages(stringValue(entry, "url"), repo, depth + 1, sink);
            }
        }
    }

    private static GitHubWikiPage chooseBestGitHubPage(List<GitHubWikiPage> pages, String query) {
        if (pages == null || pages.isEmpty()) {
            return null;
        }
        Set<String> tokens = tokenize(query);
        return pages.stream()
                .max(Comparator.comparingInt(page -> {
                    int score = scoreText(tokens, page.title() + " " + page.path());
                    String lower = page.path().toLowerCase(Locale.ROOT);
                    if (lower.contains("home")) {
                        score += 8;
                    }
                    return score;
                }))
                .orElse(null);
    }

    private static String fetchGitHubPageContent(GitHubWikiPage page) {
        if (page == null) {
            return "";
        }
        if (!blank(page.downloadUrl())) {
            HttpResult raw = httpGet(page.downloadUrl(), Map.of("Accept", "text/plain"));
            if (raw.success() && !blank(raw.body())) {
                return raw.body();
            }
        }

        if (!blank(page.apiUrl())) {
            HttpResult metadata = httpGet(page.apiUrl(), Map.of("Accept", "application/vnd.github+json"));
            if (metadata.success()) {
                try {
                    JsonObject root = JsonParser.parseString(metadata.body()).getAsJsonObject();
                    String encoded = stringValue(root, "content").replace("\n", "");
                    String encoding = stringValue(root, "encoding");
                    if ("base64".equalsIgnoreCase(encoding) && !blank(encoded)) {
                        byte[] decoded = Base64.getDecoder().decode(encoded);
                        return new String(decoded, StandardCharsets.UTF_8);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return "";
    }

    private static WikiDocument fetchGenericWikiDocument(String wikiUrl, String query) {
        if (blank(wikiUrl)) {
            return null;
        }

        HttpResult index = httpGet(wikiUrl, Map.of("Accept", "text/html"));
        if (!index.success() || blank(index.body())) {
            return null;
        }

        String indexText = extractTextFromHtml(index.body());
        String indexTitle = extractHtmlTitle(index.body());
        List<LinkCandidate> links = extractLinks(index.finalUrl(), index.body());
        LinkCandidate bestLink = chooseBestLink(links, index.finalUrl(), query);

        if (bestLink != null && !sameUrl(index.finalUrl(), bestLink.url())) {
            HttpResult page = httpGet(bestLink.url(), Map.of("Accept", "text/html"));
            if (page.success() && !blank(page.body())) {
                String text = extractTextFromHtml(page.body());
                if (!blank(text)) {
                    String title = blank(bestLink.title()) ? extractHtmlTitle(page.body()) : bestLink.title();
                    return new WikiDocument(page.finalUrl(), title, text, "html-page");
                }
            }
        }

        if (!blank(indexText)) {
            return new WikiDocument(index.finalUrl(), indexTitle, indexText, "html-index");
        }
        return null;
    }

    private static List<LinkCandidate> extractLinks(String baseUrl, String html) {
        if (blank(html)) {
            return List.of();
        }
        URI base = safeUri(baseUrl);
        if (base == null) {
            return List.of();
        }

        List<LinkCandidate> links = new ArrayList<>();
        Matcher matcher = ANCHOR_PATTERN.matcher(html);
        int count = 0;
        while (matcher.find()) {
            if (count >= MAX_LINKS) {
                break;
            }
            String href = decodeHtmlEntities(safe(matcher.group(2)).trim());
            if (blank(href)) {
                continue;
            }
            String loweredHref = href.toLowerCase(Locale.ROOT);
            if (href.startsWith("#")
                    || loweredHref.startsWith("javascript:")
                    || loweredHref.startsWith("mailto:")
                    || loweredHref.startsWith("tel:")) {
                continue;
            }

            String resolved;
            try {
                resolved = base.resolve(href).toString();
            } catch (Exception exception) {
                continue;
            }
            if (!looksHttpUrl(resolved)) {
                continue;
            }

            String title = normalizeTextBlock(stripHtmlTags(matcher.group(3)));
            links.add(new LinkCandidate(resolved, title));
            count++;
        }
        return links;
    }

    private static LinkCandidate chooseBestLink(List<LinkCandidate> links, String baseUrl, String query) {
        if (links == null || links.isEmpty()) {
            return null;
        }
        URI base = safeUri(baseUrl);
        Set<String> tokens = tokenize(query);
        LinkCandidate best = null;
        int bestScore = Integer.MIN_VALUE;

        for (LinkCandidate link : links) {
            if (link == null || blank(link.url())) {
                continue;
            }
            String lowerUrl = link.url().toLowerCase(Locale.ROOT);
            int score = scoreText(tokens, link.title() + " " + lowerUrl);

            if (lowerUrl.contains("/wiki") || lowerUrl.contains("docs") || lowerUrl.contains("guide") || lowerUrl.contains("manual")) {
                score += 35;
            }
            if (lowerUrl.contains("discord")
                    || lowerUrl.contains("twitter")
                    || lowerUrl.contains("x.com")
                    || lowerUrl.contains("reddit")
                    || lowerUrl.contains("patreon")
                    || lowerUrl.contains("kofi")) {
                score -= 60;
            }

            URI target = safeUri(link.url());
            if (base != null && target != null && sameHost(base, target)) {
                score += 20;
            } else {
                score -= 10;
            }

            if (score > bestScore) {
                best = link;
                bestScore = score;
            }
        }

        return best;
    }

    private static String formatDocument(WikiDocument document, String query) {
        if (document == null || blank(document.content())) {
            return "No document content available.";
        }

        String normalized = normalizeTextBlock(document.content());
        if (blank(normalized)) {
            return "No document content available.";
        }
        if (normalized.length() > MAX_SOURCE_CHARS) {
            normalized = normalized.substring(0, MAX_SOURCE_CHARS);
        }

        StringBuilder out = new StringBuilder();
        out.append("source: ").append(blank(document.sourceUrl()) ? "unknown" : document.sourceUrl()).append("\n");
        if (!blank(document.title())) {
            out.append("title: ").append(document.title()).append("\n");
        }
        out.append("mode: ").append(blank(document.mode()) ? "document" : document.mode()).append("\n");

        if (normalized.length() <= 3_500) {
            out.append("content:\n").append(normalized);
            return out.toString().trim();
        }

        List<String> chunks = buildChunks(normalized, CHUNK_TARGET_CHARS);
        if (chunks.isEmpty()) {
            out.append("content:\n").append(trimToMax(normalized, MAX_OUTPUT_CHARS));
            return out.toString().trim();
        }

        Set<String> tokens = tokenize(query);
        List<ChunkScore> scored = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            int score = scoreText(tokens, chunk);
            if (chunk.length() > 800) {
                score += 4;
            }
            scored.add(new ChunkScore(i, score, chunk));
        }

        scored.sort((a, b) -> {
            int byScore = Integer.compare(b.score(), a.score());
            if (byScore != 0) {
                return byScore;
            }
            return Integer.compare(a.index(), b.index());
        });

        int take = Math.min(CHUNK_TOP_COUNT, scored.size());
        List<ChunkScore> selected = new ArrayList<>(scored.subList(0, take));
        selected.sort(Comparator.comparingInt(ChunkScore::index));

        out.append("content: top ").append(selected.size()).append(" chunks for query `").append(safe(query)).append("`\n");
        for (ChunkScore chunk : selected) {
            out.append("\n[chunk ").append(chunk.index() + 1).append("]\n");
            out.append(chunk.content()).append("\n");
        }
        return out.toString().trim();
    }

    private static List<String> buildChunks(String text, int targetChars) {
        if (blank(text)) {
            return List.of();
        }
        String normalized = normalizeTextBlock(text);
        if (blank(normalized)) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        String[] paragraphs = normalized.split("\\n\\n+");
        StringBuilder current = new StringBuilder();

        for (String rawParagraph : paragraphs) {
            String paragraph = normalizeTextBlock(rawParagraph);
            if (blank(paragraph)) {
                continue;
            }

            if (paragraph.length() > targetChars * 2) {
                if (current.length() > 0) {
                    chunks.add(current.toString().trim());
                    current.setLength(0);
                }
                for (int start = 0; start < paragraph.length(); start += targetChars) {
                    int end = Math.min(paragraph.length(), start + targetChars);
                    chunks.add(paragraph.substring(start, end).trim());
                }
                continue;
            }

            int projected = current.length() + paragraph.length() + 2;
            if (projected > targetChars && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }

            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(paragraph);
        }

        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    private static String resolveWikiUrl(ModrinthProject project, Map<String, String> contactLinks) {
        if (project != null && looksHttpUrl(project.wikiUrl())) {
            return project.wikiUrl();
        }

        if (contactLinks != null && !contactLinks.isEmpty()) {
            List<String> keys = List.of("wiki", "documentation", "docs", "homepage", "source", "sources", "issues");
            for (String key : keys) {
                String value = contactLinks.get(key);
                if (looksHttpUrl(value)) {
                    if (value.toLowerCase(Locale.ROOT).contains("/wiki") || key.equals("wiki") || key.equals("documentation") || key.equals("docs")) {
                        return value;
                    }
                }
            }
            for (String value : contactLinks.values()) {
                if (looksHttpUrl(value) && value.toLowerCase(Locale.ROOT).contains("/wiki")) {
                    return value;
                }
            }
            for (String value : contactLinks.values()) {
                String guessed = guessGitHubWikiUrl(value);
                if (!blank(guessed)) {
                    return guessed;
                }
            }
        }

        if (project != null) {
            String guessedFromSource = guessGitHubWikiUrl(project.sourceUrl());
            if (!blank(guessedFromSource)) {
                return guessedFromSource;
            }
        }
        return "";
    }

    private static ModrinthProject resolveModrinthProject(String modId, Mod installedMod, Map<String, String> contactLinks) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        if (!blank(modId)) {
            candidates.add(modId);
        }

        if (installedMod != null) {
            addModrinthCandidate(candidates, installedMod.getHomepage().orElse(""));
            addModrinthCandidate(candidates, installedMod.getSources().orElse(""));
            addModrinthCandidate(candidates, installedMod.getIssueTracker().orElse(""));
        }

        if (contactLinks != null) {
            for (String link : contactLinks.values()) {
                addModrinthCandidate(candidates, link);
            }
        }

        for (String candidate : candidates) {
            Optional<ModrinthProject> project = fetchModrinthProject(candidate);
            if (project.isPresent()) {
                return project.get();
            }
        }

        String query = installedMod != null && !blank(installedMod.getName())
                ? installedMod.getName()
                : modId;
        return searchModrinthProject(query, modId).orElse(null);
    }

    private static Optional<ModrinthProject> fetchModrinthProject(String idOrSlug) {
        if (blank(idOrSlug)) {
            return Optional.empty();
        }
        String url = "https://api.modrinth.com/v2/project/" + encodePathSegment(idOrSlug);
        HttpResult response = httpGet(url, Map.of("Accept", "application/json"));
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        if (!response.success()) {
            return Optional.empty();
        }

        try {
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            String slug = stringValue(root, "slug");
            String title = stringValue(root, "title");
            String description = stringValue(root, "description");
            String body = stringValue(root, "body");
            String wikiUrl = stringValue(root, "wiki_url");
            String sourceUrl = stringValue(root, "source_url");
            String projectType = stringValue(root, "project_type");
            String webType = blank(projectType) ? "mod" : projectType;
            String projectUrl = blank(slug) ? "" : "https://modrinth.com/" + webType + "/" + slug;
            return Optional.of(new ModrinthProject(slug, title, description, body, wikiUrl, sourceUrl, projectUrl));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Optional<ModrinthProject> searchModrinthProject(String query, String modId) {
        if (blank(query)) {
            return Optional.empty();
        }
        String url = "https://api.modrinth.com/v2/search?query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&limit=10";
        HttpResult response = httpGet(url, Map.of("Accept", "application/json"));
        if (!response.success()) {
            return Optional.empty();
        }

        try {
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            if (!root.has("hits") || !root.get("hits").isJsonArray()) {
                return Optional.empty();
            }
            JsonArray hits = root.getAsJsonArray("hits");
            JsonObject best = null;
            int bestScore = Integer.MIN_VALUE;

            Set<String> modIdTokens = tokenize(modId);
            for (JsonElement hitElement : hits) {
                if (!hitElement.isJsonObject()) {
                    continue;
                }
                JsonObject hit = hitElement.getAsJsonObject();
                String projectType = stringValue(hit, "project_type");
                String slug = stringValue(hit, "slug");
                String projectId = stringValue(hit, "project_id");
                String title = stringValue(hit, "title");
                String description = stringValue(hit, "description");

                int score = scoreText(modIdTokens, slug + " " + title + " " + description);
                if ("mod".equalsIgnoreCase(projectType)) {
                    score += 40;
                }
                if (!blank(modId) && slug.equalsIgnoreCase(modId)) {
                    score += 320;
                }
                if (!blank(modId) && projectId.equalsIgnoreCase(modId)) {
                    score += 260;
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = hit;
                }
            }

            if (best == null) {
                return Optional.empty();
            }

            String slug = stringValue(best, "slug");
            String projectId = stringValue(best, "project_id");
            if (!blank(slug)) {
                Optional<ModrinthProject> bySlug = fetchModrinthProject(slug);
                if (bySlug.isPresent()) {
                    return bySlug;
                }
            }
            if (!blank(projectId)) {
                return fetchModrinthProject(projectId);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private static Map<String, String> readFabricContactLinks(Mod installedMod) {
        if (installedMod == null) {
            return Map.of();
        }
        Optional<Path> path = installedMod.findResource("fabric.mod.json");
        if (path.isEmpty()) {
            return Map.of();
        }

        try {
            String json = Files.readString(path.get(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            LinkedHashMap<String, String> links = new LinkedHashMap<>();

            if (root.has("contact") && root.get("contact").isJsonObject()) {
                JsonObject contact = root.getAsJsonObject("contact");
                for (Map.Entry<String, JsonElement> entry : contact.entrySet()) {
                    if (entry.getValue() == null || !entry.getValue().isJsonPrimitive()) {
                        continue;
                    }
                    String value = entry.getValue().getAsString();
                    if (looksHttpUrl(value)) {
                        links.put(entry.getKey().toLowerCase(Locale.ROOT), value.trim());
                    }
                }
            }

            if (root.has("links") && root.get("links").isJsonObject()) {
                JsonObject extra = root.getAsJsonObject("links");
                for (Map.Entry<String, JsonElement> entry : extra.entrySet()) {
                    if (entry.getValue() == null || !entry.getValue().isJsonPrimitive()) {
                        continue;
                    }
                    String value = entry.getValue().getAsString();
                    if (looksHttpUrl(value)) {
                        links.putIfAbsent(entry.getKey().toLowerCase(Locale.ROOT), value.trim());
                    }
                }
            }

            return Map.copyOf(links);
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private static void addModrinthCandidate(Set<String> candidates, String link) {
        String extracted = extractModrinthSlug(link);
        if (!blank(extracted)) {
            candidates.add(extracted);
        }
    }

    private static String extractModrinthSlug(String text) {
        if (blank(text)) {
            return "";
        }
        Matcher matcher = MODRINTH_LINK_PATTERN.matcher(text);
        if (matcher.find()) {
            return safe(matcher.group(1)).trim();
        }
        return "";
    }

    private static Set<String> buildModAliases(String modId) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        String normalized = normalizeForMatch(modId);
        if (!blank(normalized)) {
            aliases.add(normalized);
        }

        Platform.getOptionalMod(modId == null ? "" : modId.trim()).ifPresent(mod -> {
            aliases.add(normalizeForMatch(mod.getModId()));
            for (String token : tokenize(mod.getName())) {
                if (token.length() >= 3) {
                    aliases.add(token);
                }
            }
        });
        aliases.removeIf(ModDocsToolExecutor::blank);
        return aliases;
    }

    private static boolean matchesCommandFilter(String command, Set<String> aliases) {
        if (blank(command) || aliases == null || aliases.isEmpty()) {
            return false;
        }
        String normalizedCommand = normalizeForMatch(command);
        String lowerRaw = command.toLowerCase(Locale.ROOT);
        for (String alias : aliases) {
            if (blank(alias)) {
                continue;
            }
            if (normalizedCommand.equals(alias)
                    || normalizedCommand.startsWith(alias + " ")
                    || lowerRaw.startsWith(alias + ":")
                    || lowerRaw.startsWith(alias + "_")
                    || lowerRaw.startsWith(alias + "-")
                    || lowerRaw.contains(":" + alias)) {
                return true;
            }
            if (alias.length() >= 5 && normalizedCommand.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private static HttpResult httpGet(String url, Map<String, String> extraHeaders) {
        if (blank(url)) {
            return new HttpResult(0, "", "", "empty url");
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url.trim()))
                    .timeout(HTTP_TIMEOUT)
                    .header("User-Agent", USER_AGENT)
                    .GET();
            if (extraHeaders != null) {
                for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        builder.header(entry.getKey(), entry.getValue());
                    }
                }
            }
            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            URI finalUri = response.uri();
            return new HttpResult(
                    response.statusCode(),
                    response.body() == null ? "" : response.body(),
                    finalUri == null ? url : finalUri.toString(),
                    ""
            );
        } catch (Exception exception) {
            return new HttpResult(0, "", url, exception.getClass().getSimpleName() + ": " + safe(exception.getMessage()));
        }
    }

    private static URI safeUri(String url) {
        if (blank(url)) {
            return null;
        }
        try {
            return URI.create(url.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(safe(value), StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String buildGitHubWikiWebUrl(GitHubRepo repo, String pagePath) {
        if (repo == null || blank(pagePath)) {
            return "";
        }
        String[] segments = pagePath.split("/");
        List<String> encoded = new ArrayList<>();
        for (String segment : segments) {
            if (blank(segment)) {
                continue;
            }
            encoded.add(encodePathSegment(segment));
        }
        String joined = String.join("/", encoded);
        return "https://github.com/" + repo.owner() + "/" + repo.repo() + "/wiki/" + joined;
    }

    private static GitHubRepo parseGitHubWikiRepo(String wikiUrl) {
        URI uri = safeUri(wikiUrl);
        if (uri == null || blank(uri.getHost())) {
            return null;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (!"github.com".equals(host) && !"www.github.com".equals(host)) {
            return null;
        }
        String path = safe(uri.getPath());
        Matcher matcher = GITHUB_WIKI_PATH_PATTERN.matcher(path);
        if (!matcher.matches()) {
            return null;
        }
        String owner = safe(matcher.group(1)).trim();
        String repo = safe(matcher.group(2)).trim();
        if (blank(owner) || blank(repo)) {
            return null;
        }
        return new GitHubRepo(owner, repo);
    }

    private static String guessGitHubWikiUrl(String link) {
        URI uri = safeUri(link);
        if (uri == null || blank(uri.getHost())) {
            return "";
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (!"github.com".equals(host) && !"www.github.com".equals(host)) {
            return "";
        }
        String[] segments = Arrays.stream(safe(uri.getPath()).split("/"))
                .filter(segment -> !blank(segment))
                .toArray(String[]::new);
        if (segments.length < 2) {
            return "";
        }
        String owner = segments[0];
        String repo = segments[1];
        if ("wiki".equalsIgnoreCase(repo)) {
            return "";
        }
        if (segments.length >= 3 && "wiki".equalsIgnoreCase(segments[2])) {
            return "https://github.com/" + owner + "/" + repo + "/wiki";
        }
        return "https://github.com/" + owner + "/" + repo + "/wiki";
    }

    private static String humanizePageTitle(String pagePath) {
        if (blank(pagePath)) {
            return "";
        }
        String leaf = pagePath;
        int slash = leaf.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < leaf.length()) {
            leaf = leaf.substring(slash + 1);
        }
        return leaf.replace('-', ' ').replace('_', ' ').trim();
    }

    private static String extractHtmlTitle(String html) {
        if (blank(html)) {
            return "";
        }
        Matcher matcher = HTML_TITLE_PATTERN.matcher(html);
        if (!matcher.find()) {
            return "";
        }
        return normalizeTextBlock(stripHtmlTags(matcher.group(1)));
    }

    private static boolean looksLikeHtml(String text) {
        if (blank(text)) {
            return false;
        }
        String lower = text.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("<!doctype html")
                || lower.contains("<html")
                || lower.contains("<body")
                || lower.contains("<head")
                || lower.contains("</p>")
                || lower.contains("</div>");
    }

    private static String extractTextFromHtml(String html) {
        if (blank(html)) {
            return "";
        }
        String text = html;
        text = text.replaceAll("(?is)<(script|style|noscript|svg|canvas)[^>]*>.*?</\\1>", " ");
        text = text.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("(?i)</(p|div|li|section|article|h[1-6]|tr|pre|code)>", "\n");
        text = stripHtmlTags(text);
        text = decodeHtmlEntities(text);
        text = text.replace("\r\n", "\n").replace('\r', '\n');
        text = text.replaceAll("[ \\t\\f]+", " ");
        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.trim();
        if (text.length() > MAX_SOURCE_CHARS) {
            text = text.substring(0, MAX_SOURCE_CHARS);
        }
        return text;
    }

    private static String stripHtmlTags(String text) {
        if (blank(text)) {
            return "";
        }
        return HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
    }

    private static String decodeHtmlEntities(String text) {
        if (blank(text)) {
            return "";
        }
        String decoded = text
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        Matcher matcher = NUMERIC_ENTITY_PATTERN.matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String raw = matcher.group(1);
            String replacement = "";
            try {
                int codePoint;
                if (raw != null && (raw.startsWith("x") || raw.startsWith("X"))) {
                    codePoint = Integer.parseInt(raw.substring(1), 16);
                } else {
                    codePoint = Integer.parseInt(raw);
                }
                replacement = new String(Character.toChars(codePoint));
            } catch (Exception ignored) {
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static boolean sameUrl(String left, String right) {
        if (blank(left) || blank(right)) {
            return false;
        }
        URI leftUri = safeUri(left);
        URI rightUri = safeUri(right);
        if (leftUri == null || rightUri == null) {
            return left.trim().equalsIgnoreCase(right.trim());
        }
        String leftNormalized = leftUri.normalize().toString();
        String rightNormalized = rightUri.normalize().toString();
        return leftNormalized.equalsIgnoreCase(rightNormalized);
    }

    private static boolean sameHost(URI left, URI right) {
        if (left == null || right == null || left.getHost() == null || right.getHost() == null) {
            return false;
        }
        return left.getHost().equalsIgnoreCase(right.getHost());
    }

    private static int scoreText(Set<String> tokens, String text) {
        if (tokens == null || tokens.isEmpty() || blank(text)) {
            return 0;
        }
        String normalized = normalizeForMatch(text);
        if (blank(normalized)) {
            return 0;
        }
        int score = 0;
        int matched = 0;
        for (String token : tokens) {
            if (blank(token)) {
                continue;
            }
            if (normalized.equals(token)) {
                score += 60;
                matched++;
                continue;
            }
            if (normalized.contains(token)) {
                score += 20 + token.length();
                matched++;
                if (normalized.startsWith(token)) {
                    score += 10;
                }
            }
        }
        if (matched == tokens.size() && matched > 0) {
            score += 25;
        }
        return score;
    }

    private static Set<String> tokenize(String text) {
        String normalized = normalizeForMatch(text);
        if (blank(normalized)) {
            return Set.of();
        }
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (String token : normalized.split(" ")) {
            if (token.length() >= 2) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static String normalizeForMatch(String text) {
        if (blank(text)) {
            return "";
        }
        String normalized = NON_ALNUM_PATTERN.matcher(text.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
        while (normalized.contains("  ")) {
            normalized = normalized.replace("  ", " ");
        }
        return normalized;
    }

    private static String normalizeTextBlock(String text) {
        if (blank(text)) {
            return "";
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        normalized = normalized.replaceAll("[ \\t\\f]+", " ");
        normalized = normalized.replaceAll("\\n{3,}", "\n\n");
        return normalized.trim();
    }

    private static boolean looksHttpUrl(String value) {
        if (blank(value)) {
            return false;
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static void appendMetaLine(StringBuilder builder, String key, String value) {
        if (builder == null || blank(key) || blank(value)) {
            return;
        }
        builder.append("  ").append(key).append(": ").append(value.trim()).append("\n");
    }

    private static String stringValue(JsonObject object, String key) {
        if (object == null || blank(key) || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String trimToMax(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars).trim() + "\n... truncated ...";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record HttpResult(int statusCode, String body, String finalUrl, String error) {
        private boolean success() {
            return statusCode >= 200 && statusCode < 300;
        }
    }

    private record ModrinthProject(
            String slug,
            String title,
            String description,
            String body,
            String wikiUrl,
            String sourceUrl,
            String projectUrl
    ) {
    }

    private record WikiDocument(String sourceUrl, String title, String content, String mode) {
    }

    private record LinkCandidate(String url, String title) {
    }

    private record GitHubRepo(String owner, String repo) {
    }

    private record GitHubWikiPage(String title, String path, String webUrl, String downloadUrl, String apiUrl) {
    }

    private record ChunkScore(int index, int score, String content) {
    }
}
