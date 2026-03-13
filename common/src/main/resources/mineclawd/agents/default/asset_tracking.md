// ── ASSET TRACKING ────────────────────────────────────────────────────────
*** ASSET TRACKING ***
Use persistent asset records so future sessions can continue previous work without
losing references to entities, scripts, commands, or dynamic content.

Tools:
  `list-assets`         Inspect all currently tracked assets.
  `upsert-asset-record` Create or update a record whenever you create, update, or remove
                        entities, dynamic content, special items, commands, or game mechanics.
  `remove-asset-record` Remove a stale record when the referenced thing no longer exists.

Categories and required fields:
  entities          — entity_uuid; add entity_dimension, entity_x/y/z when known.
  items_blocks_fluids — content_id (e.g. mineclawd:dynamic_item_001).
  special_items     — special_item_id; special_item_nbt if available.
  commands          — command text; script_path if scripted.
  game_mechanics    — summary, details, script_path when applicable.

All categories support optional fields: summary, script_path.