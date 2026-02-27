package com.mineclawd.files;

import com.mineclawd.kubejs.KubeJsToolExecutor.ToolExecutionResult;
import dev.architectury.platform.Platform;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public final class WorkspaceFileToolExecutor {
    private static final int MAX_LIST_RESULTS = 500;
    private static final int MAX_FILE_CHARS = 64_000;
    private static final int MAX_GREP_MATCHES = 500;
    private static final int MAX_CURL_BODY_CHARS = 80_000;
    private static final Duration DEFAULT_CURL_TIMEOUT = Duration.ofSeconds(30);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private WorkspaceFileToolExecutor() {
    }

    public static ToolExecutionResult listFiles(ServerCommandSource source, String path, Boolean recursive, Integer limit) {
        Path root = serverRoot(source);
        if (root == null) {
            return new ToolExecutionResult(false, "No server available.");
        }
        Path target = resolvePath(root, path, true);
        if (target == null) {
            return new ToolExecutionResult(false, "Invalid path. Paths must stay inside the server root and cannot use `..`.");
        }
        if (!Files.exists(target)) {
            return new ToolExecutionResult(false, "Path not found: " + displayPath(root, target));
        }

        int safeLimit = limit == null ? 120 : Math.max(1, Math.min(MAX_LIST_RESULTS, limit));
        boolean deep = recursive != null && recursive;

        List<Path> entries = new ArrayList<>();
        if (Files.isRegularFile(target)) {
            entries.add(target);
        } else {
            try (Stream<Path> stream = deep ? Files.walk(target) : Files.list(target)) {
                stream.filter(pathEntry -> !pathEntry.equals(target)).forEach(entries::add);
            } catch (IOException exception) {
                return new ToolExecutionResult(false, "Failed to list files: " + formatException(exception));
            }
        }

        entries.sort(Comparator.comparing(pathEntry -> displayPath(root, pathEntry), String.CASE_INSENSITIVE_ORDER));
        StringBuilder out = new StringBuilder();
        out.append("Root: ").append(root).append("\n");
        out.append("Target: ").append(displayPath(root, target)).append("\n");
        out.append("Recursive: ").append(deep).append("\n\n");

        if (entries.isEmpty()) {
            out.append("(empty)");
            return new ToolExecutionResult(true, out.toString());
        }

        int count = 0;
        for (Path entry : entries) {
            count++;
            if (count > safeLimit) {
                out.append("... truncated at ").append(safeLimit).append(" entries.\n");
                break;
            }
            String label = displayPath(root, entry);
            if (Files.isDirectory(entry)) {
                label += "/";
            }
            long size = 0L;
            FileTime modified = FileTime.fromMillis(0L);
            try {
                if (Files.isRegularFile(entry)) {
                    size = Files.size(entry);
                }
                modified = Files.getLastModifiedTime(entry);
            } catch (IOException ignored) {
            }
            out.append("- ").append(label)
                    .append(" (size=").append(size)
                    .append(" bytes, modified=").append(modified)
                    .append(")\n");
        }
        out.append("Total entries: ").append(entries.size());
        return new ToolExecutionResult(true, out.toString().trim());
    }

    public static ToolExecutionResult readFile(ServerCommandSource source, String path) {
        Path root = serverRoot(source);
        if (root == null) {
            return new ToolExecutionResult(false, "No server available.");
        }
        Path file = resolvePath(root, path, false);
        if (file == null) {
            return new ToolExecutionResult(false, "Invalid path. Paths must stay inside the server root and cannot use `..`.");
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return new ToolExecutionResult(false, "File not found: " + displayPath(root, file));
        }

        try {
            byte[] bytes = Files.readAllBytes(file);
            boolean binary = looksBinary(bytes);
            if (binary) {
                return new ToolExecutionResult(true,
                        "BINARY FILE " + displayPath(root, file) + "\n"
                                + "size: " + bytes.length + " bytes\n"
                                + "Use copy/move tools for binary assets.");
            }

            String content = new String(bytes, StandardCharsets.UTF_8);
            boolean truncated = false;
            if (content.length() > MAX_FILE_CHARS) {
                content = content.substring(0, MAX_FILE_CHARS);
                truncated = true;
            }

            StringBuilder out = new StringBuilder();
            out.append("FILE ").append(displayPath(root, file)).append("\n");
            out.append("size: ").append(bytes.length).append(" bytes\n");
            if (truncated) {
                out.append("WARNING: truncated to ").append(MAX_FILE_CHARS).append(" chars.\n");
            }
            out.append("-----BEGIN CONTENT-----\n");
            out.append(content);
            if (!content.endsWith("\n")) {
                out.append("\n");
            }
            out.append("-----END CONTENT-----");
            return new ToolExecutionResult(true, out.toString());
        } catch (IOException exception) {
            return new ToolExecutionResult(false, "Failed to read file: " + formatException(exception));
        }
    }

    public static ToolExecutionResult writeFile(ServerCommandSource source, String path, String content) {
        Path root = serverRoot(source);
        if (root == null) {
            return new ToolExecutionResult(false, "No server available.");
        }
        Path file = resolvePath(root, path, false);
        if (file == null) {
            return new ToolExecutionResult(false, "Invalid path. Paths must stay inside the server root and cannot use `..`.");
        }
        if (content == null) {
            content = "";
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(
                    file,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            long size = Files.size(file);
            return new ToolExecutionResult(true, "Wrote " + displayPath(root, file) + " (" + size + " bytes).");
        } catch (IOException exception) {
            return new ToolExecutionResult(false, "Failed to write file: " + formatException(exception));
        }
    }

    public static ToolExecutionResult copyFiles(ServerCommandSource source, String fromPath, String toPath) {
        Path root = serverRoot(source);
        if (root == null) {
            return new ToolExecutionResult(false, "No server available.");
        }
        Path from = resolvePath(root, fromPath, false);
        Path to = resolvePath(root, toPath, false);
        if (from == null || to == null) {
            return new ToolExecutionResult(false, "Invalid path. Paths must stay inside the server root and cannot use `..`.");
        }
        if (!Files.exists(from)) {
            return new ToolExecutionResult(false, "Source path not found: " + displayPath(root, from));
        }
        if (from.equals(to)) {
            return new ToolExecutionResult(false, "Source and target paths are identical.");
        }
        if (Files.isDirectory(from) && to.startsWith(from)) {
            return new ToolExecutionResult(false, "Cannot copy a directory into itself.");
        }

        try {
            int copied = copyRecursively(from, to);
            return new ToolExecutionResult(true,
                    "Copied " + copied + " entr" + (copied == 1 ? "y" : "ies") + " from "
                            + displayPath(root, from) + " to " + displayPath(root, to) + ".");
        } catch (IOException exception) {
            return new ToolExecutionResult(false, "Failed to copy path: " + formatException(exception));
        }
    }

    public static ToolExecutionResult moveFiles(ServerCommandSource source, String fromPath, String toPath) {
        Path root = serverRoot(source);
        if (root == null) {
            return new ToolExecutionResult(false, "No server available.");
        }
        Path from = resolvePath(root, fromPath, false);
        Path to = resolvePath(root, toPath, false);
        if (from == null || to == null) {
            return new ToolExecutionResult(false, "Invalid path. Paths must stay inside the server root and cannot use `..`.");
        }
        if (!Files.exists(from)) {
            return new ToolExecutionResult(false, "Source path not found: " + displayPath(root, from));
        }
        if (from.equals(to)) {
            return new ToolExecutionResult(false, "Source and target paths are identical.");
        }
        if (Files.isDirectory(from) && to.startsWith(from)) {
            return new ToolExecutionResult(false, "Cannot move a directory into itself.");
        }

        try {
            if (to.getParent() != null) {
                Files.createDirectories(to.getParent());
            }
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            return new ToolExecutionResult(true,
                    "Moved " + displayPath(root, from) + " to " + displayPath(root, to) + ".");
        } catch (IOException firstMoveError) {
            try {
                int copied = copyRecursively(from, to);
                deleteRecursively(from);
                return new ToolExecutionResult(true,
                        "Moved " + copied + " entr" + (copied == 1 ? "y" : "ies") + " from "
                                + displayPath(root, from) + " to " + displayPath(root, to) + " (copy/delete fallback).");
            } catch (IOException fallbackError) {
                return new ToolExecutionResult(false,
                        "Failed to move path: " + formatException(firstMoveError) + "; fallback failed: "
                                + formatException(fallbackError));
            }
        }
    }

    public static ToolExecutionResult grepFiles(
            ServerCommandSource source,
            String pattern,
            String path,
            String glob,
            Boolean caseSensitive,
            Integer maxMatches
    ) {
        Path root = serverRoot(source);
        if (root == null) {
            return new ToolExecutionResult(false, "No server available.");
        }
        if (pattern == null || pattern.isBlank()) {
            return new ToolExecutionResult(false, "`pattern` is required.");
        }
        Path target = resolvePath(root, path, true);
        if (target == null) {
            return new ToolExecutionResult(false, "Invalid path. Paths must stay inside the server root and cannot use `..`.");
        }
        if (!Files.exists(target)) {
            return new ToolExecutionResult(false, "Path not found: " + displayPath(root, target));
        }

        int flags = Pattern.MULTILINE;
        if (caseSensitive == null || !caseSensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern compiled;
        try {
            compiled = Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException exception) {
            return new ToolExecutionResult(false, "Invalid regex pattern: " + exception.getDescription());
        }

        java.nio.file.PathMatcher matcher = null;
        if (glob != null && !glob.isBlank()) {
            try {
                matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob.trim());
            } catch (Exception exception) {
                return new ToolExecutionResult(false, "Invalid glob: " + exception.getMessage());
            }
        }

        int limit = maxMatches == null ? 120 : Math.max(1, Math.min(MAX_GREP_MATCHES, maxMatches));
        List<Path> files = new ArrayList<>();
        if (Files.isRegularFile(target)) {
            files.add(target);
        } else {
            try (Stream<Path> stream = Files.walk(target)) {
                stream.filter(Files::isRegularFile).forEach(files::add);
            } catch (IOException exception) {
                return new ToolExecutionResult(false, "Failed to walk files: " + formatException(exception));
            }
        }

        files.sort(Comparator.comparing(pathEntry -> displayPath(root, pathEntry), String.CASE_INSENSITIVE_ORDER));
        StringBuilder out = new StringBuilder();
        int matchCount = 0;
        for (Path file : files) {
            Path relPath = root.relativize(file);
            if (matcher != null && !matcher.matches(relPath)) {
                continue;
            }
            String text;
            try {
                text = Files.readString(file, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                continue;
            }
            String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                Matcher lineMatcher = compiled.matcher(lines[i]);
                if (!lineMatcher.find()) {
                    continue;
                }
                matchCount++;
                out.append(displayPath(root, file))
                        .append(":")
                        .append(i + 1)
                        .append(": ")
                        .append(lines[i])
                        .append("\n");
                if (matchCount >= limit) {
                    out.append("... truncated at ").append(limit).append(" matches.\n");
                    return new ToolExecutionResult(true, out.toString().trim());
                }
            }
        }

        if (matchCount == 0) {
            return new ToolExecutionResult(true, "No matches found.");
        }
        out.append("Total matches: ").append(matchCount);
        return new ToolExecutionResult(true, out.toString().trim());
    }

    public static ToolExecutionResult curl(
            String url,
            String method,
            String body,
            Map<String, String> headers,
            Integer timeoutSeconds
    ) {
        String trimmedUrl = url == null ? "" : url.trim();
        if (trimmedUrl.isBlank()) {
            return new ToolExecutionResult(false, "`url` is required.");
        }
        String lower = trimmedUrl.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return new ToolExecutionResult(false, "URL must start with http:// or https://.");
        }

        String verb = method == null || method.isBlank() ? "GET" : method.trim().toUpperCase(Locale.ROOT);
        if (!List.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD").contains(verb)) {
            return new ToolExecutionResult(false, "Unsupported HTTP method: " + verb);
        }

        int seconds = timeoutSeconds == null ? (int) DEFAULT_CURL_TIMEOUT.getSeconds() : Math.max(1, Math.min(120, timeoutSeconds));
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(trimmedUrl))
                .timeout(Duration.ofSeconds(seconds))
                .header("User-Agent", "MineClawd/1.0")
                .header("Accept", "*/*");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry == null) {
                    continue;
                }
                String key = entry.getKey();
                String value = entry.getValue();
                if (key == null || key.isBlank() || value == null) {
                    continue;
                }
                builder.header(key.trim(), value);
            }
        }

        String payload = body == null ? "" : body;
        if ("GET".equals(verb) || "HEAD".equals(verb)) {
            builder.method(verb, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(verb, HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        }

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            return new ToolExecutionResult(false, "HTTP request failed: " + formatException(exception));
        }

        String responseBody = response.body() == null ? "" : response.body();
        boolean truncated = false;
        if (responseBody.length() > MAX_CURL_BODY_CHARS) {
            responseBody = responseBody.substring(0, MAX_CURL_BODY_CHARS);
            truncated = true;
        }

        StringBuilder out = new StringBuilder();
        out.append("status: ").append(response.statusCode()).append("\n");
        out.append("url: ").append(trimmedUrl).append("\n");
        out.append("method: ").append(verb).append("\n");
        String contentType = response.headers().firstValue("content-type").orElse("");
        if (!contentType.isBlank()) {
            out.append("content-type: ").append(contentType).append("\n");
        }
        if (truncated) {
            out.append("WARNING: response truncated to ").append(MAX_CURL_BODY_CHARS).append(" chars.\n");
        }
        out.append("-----BEGIN BODY-----\n");
        out.append(responseBody);
        if (!responseBody.endsWith("\n")) {
            out.append("\n");
        }
        out.append("-----END BODY-----");
        boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
        return new ToolExecutionResult(success, out.toString());
    }

    public static Path resolveServerPath(ServerCommandSource source, String path, boolean allowRoot) {
        Path root = serverRoot(source);
        if (root == null) {
            return null;
        }
        return resolvePath(root, path, allowRoot);
    }

    public static Path serverRoot(ServerCommandSource source) {
        try {
            Path gameFolder = Platform.getGameFolder();
            if (gameFolder != null) {
                return gameFolder.toAbsolutePath().normalize();
            }
        } catch (Exception ignored) {
        }
        if (source == null || source.getServer() == null) {
            return null;
        }
        Path runDirectory = source.getServer().getRunDirectory();
        return runDirectory == null ? null : runDirectory.toAbsolutePath().normalize();
    }

    public static String displayPath(Path root, Path path) {
        if (root == null || path == null) {
            return "";
        }
        Path normalizedRoot = root.normalize();
        Path normalizedPath = path.normalize();
        if (!normalizedPath.startsWith(normalizedRoot)) {
            return normalizedPath.toString().replace('\\', '/');
        }
        Path relative = normalizedRoot.relativize(normalizedPath);
        String rel = relative.toString().replace('\\', '/');
        return rel.isBlank() ? "." : rel;
    }

    private static Path resolvePath(Path root, String rawPath, boolean allowRoot) {
        if (root == null) {
            return null;
        }
        Path normalizedRoot = root.toAbsolutePath().normalize();
        String candidate = rawPath == null ? "" : rawPath.trim().replace('\\', '/');
        while (candidate.startsWith("/")) {
            candidate = candidate.substring(1);
        }
        if (candidate.isBlank()) {
            return allowRoot ? normalizedRoot : null;
        }
        if (candidate.contains("\u0000")) {
            return null;
        }

        Path parsedPath;
        try {
            parsedPath = Path.of(candidate);
        } catch (Exception exception) {
            return null;
        }
        if (!parsedPath.isAbsolute() && candidate.contains(":")) {
            return null;
        }

        Path resolved = parsedPath.isAbsolute()
                ? parsedPath.normalize()
                : normalizedRoot.resolve(parsedPath).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            return null;
        }
        if (!allowRoot && resolved.equals(normalizedRoot)) {
            return null;
        }
        return resolved;
    }

    private static int copyRecursively(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            List<Path> paths;
            try (Stream<Path> stream = Files.walk(source)) {
                paths = stream.sorted(Comparator.naturalOrder()).toList();
            }
            int copied = 0;
            for (Path path : paths) {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    if (destination.getParent() != null) {
                        Files.createDirectories(destination.getParent());
                    }
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
                copied++;
            }
            return copied;
        }

        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        return 1;
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> stream = Files.walk(root)) {
            paths = stream.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }

    private static boolean looksBinary(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        int sample = Math.min(data.length, 512);
        int suspicious = 0;
        for (int i = 0; i < sample; i++) {
            int b = data[i] & 0xFF;
            if (b == 0) {
                return true;
            }
            if (b < 0x09 || (b > 0x0D && b < 0x20)) {
                suspicious++;
            }
        }
        return suspicious > sample / 8;
    }

    private static String formatException(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        String type = throwable.getClass().getSimpleName();
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return type;
        }
        return type + ": " + message;
    }
}
