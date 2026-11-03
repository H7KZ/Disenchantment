<!-- generated-by: gsd-doc-writer -->

# Configuration Reference

Complete reference for `plugins/Disenchantment/config.yml`. All settings can also be changed in-game
via [commands](COMMANDS.md) or the `/disenchantment gui` interface.

---

## Full Config Example

A complete annotated `config.yml` showing every option with its default value. Copy this as a starting point or compare
it against your existing file.

```yaml
# ↓ DO NOT EDIT THESE VALUES ↓
migration: 13
locales: [ "cs", "en" ]
# ↑ DO NOT EDIT THESE VALUES ↑

# Enable or disable the entire plugin globally.
enabled: true

# Periodically (every 8 hours) ask SpigotMC for the latest version and log a message when a newer one exists.
# Set to false to disable all outgoing update checks.
update-checker: true

# Language for plugin messages. Available: "en", "cs".
locale: "en"

# Controls how Disenchantment's listeners fire relative to other plugins.
# Adjust if you experience conflicts with other anvil-modifying plugins.
# Available (lowest → highest): LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
event-priorities:
  disenchant-click: HIGHEST
  disenchant: HIGHEST
  shatter-click: HIGHEST
  shatter: HIGHEST
  grindstone-click: HIGHEST
  grindstone: HIGHEST

# ─────────────────────────────────────────────────────────────────────────────

logging:
  # NONE = silent | INFO = startup summary | DEBUG = full operation tracing
  level: INFO
  # Write crash/error reports to plugins/Disenchantment/logs/crash-<timestamp>.txt
  save-reports: true
  # Set to true to record every disenchant/shatter to plugins/Disenchantment/stats.db
  operations: false
  # Delete stats.db operation records older than N days on startup. 0 = keep forever.
  operations-retention-days: 0
  # Discord webhook URL — if set, crash reports are POSTed asynchronously. Leave empty to disable.
  discord-webhook: ""

# ─────────────────────────────────────────────────────────────────────────────

disenchantment:
  # Enable or disable the disenchanting feature.
  enabled: true

  # World restriction. mode: DENYLIST = listed worlds are blocked |
  # ALLOWLIST = only listed worlds are allowed (empty list blocks everywhere).
  worlds:
    mode: DENYLIST
    list: []

  # Material restriction. mode: DENYLIST = listed materials cannot be disenchanted |
  # ALLOWLIST = only listed materials can be disenchanted.
  materials:
    mode: DENYLIST
    list: []

  # Per-enchantment behavior overrides. Format: "enchantment_key:state"
  # States: enable (default), keep (stay on item), delete (destroy), disable (block operation)
  enchantments-states: []

  anvil:
    sound:
      # Play a sound when a disenchantment succeeds.
      enabled: true
      volume: 1.0
      pitch: 1.0

    repair:
      # Reset the item's repair cost (prior work penalty) to 0 after disenchanting.
      reset: false
      # Enable XP cost for disenchanting. Set to false for free disenchanting.
      cost: true
      # Base XP levels added to the cost.
      base: 10
      # Multiplier applied per enchantment level (increases with each enchantment processed).
      multiply: 0.25
      # Per-enchantment fixed cost overrides (bypass the formula entirely).
      # Example: { mending: 30 } means mending always costs exactly 30 levels.
      enchantment-costs: {}

  economy:
    # Charge players currency when disenchanting. Requires Vault + an economy plugin.
    enabled: false
    # Flat currency amount charged per disenchant operation.
    cost: 100.0
    # Show the currency cost in the action bar while the player has a pending result.
    show-cost: true
    # Send a chat confirmation message to the player after they are charged.
    charge-message: true

# ─────────────────────────────────────────────────────────────────────────────

shatterment:
  # Enable or disable the book splitting (shattering) feature.
  enabled: true

  # World restriction (same mode semantics as disenchantment).
  worlds:
    mode: DENYLIST
    list: []

  # Material restriction (checked against the enchanted book being shattered).
  materials:
    mode: DENYLIST
    list: []

  # Per-enchantment behavior overrides — same format as disenchantment.
  enchantments-states: []

  # Number of enchantments to split off per shatter operation (default: 1).
  split-count: 1

  anvil:
    sound:
      enabled: true
      volume: 1.0
      pitch: 1.0

    repair:
      reset: false
      cost: true
      base: 10
      multiply: 0.25
      enchantment-costs: {}

  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true

# ─────────────────────────────────────────────────────────────────────────────

# Grindstone mode (opt-in). Worlds, materials, enchantment states and chances come from "disenchantment".
grindstone:
  # EXTRACT: enchanted item + plain book -> enchanted book.
  enabled: false
  # Curses stay on the item unless this is true.
  curses: false
  repair:
    reset: false
    cost: true
    base: 10
    multiply: 0.25
    enchantment-costs: {}
    max-cost: -1
  sound:
    enabled: true
    volume: 1.0
    pitch: 1.0
  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true
  # TRANSFER: enchanted item + a different kind of item -> the second item gets the enchantments.
  transfer:
    enabled: false
    allow-over-max: false
    max-enchantments: 16
    repair:
      reset: false
      cost: true
      base: 15
      multiply: 0.25
      enchantment-costs: {}
      max-cost: -1
    economy:
      enabled: false
      cost: 100.0
      show-cost: true
      charge-message: true
```

---

## Table of Contents

- [General Settings](#general-settings)
- [Event Priorities](#event-priorities)
- [Logging](#logging)
- [Disenchantment Settings](#disenchantment-settings)
    - [Feature Toggle and Restrictions](#feature-toggle-and-restrictions)
    - [Anvil Sound](#anvil-sound)
    - [Repair Cost](#repair-cost)
    - [Economy Cost (Disenchantment)](#economy-cost-disenchantment)
- [Shatterment Settings](#shatterment-settings)
    - [Feature Toggle and Restrictions (Shatter)](#feature-toggle-and-restrictions-shatter)
    - [Anvil Sound (Shatter)](#anvil-sound-shatter)
    - [Repair Cost (Shatter)](#repair-cost-shatter)
    - [Economy Cost (Shatterment)](#economy-cost-shatterment)
- [Grindstone Settings](#grindstone-settings)
    - [Extract](#grindstone-extract)
    - [Transfer](#grindstone-transfer)
- [Economy Integration](#economy-integration)

---

## General Settings

```yaml
enabled: true
update-checker: true
locale: "en"
```

| Key              | Type    | Default | Description                                                                                                                                   |
|------------------|---------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`        | Boolean | `true`  | Enable or disable the entire plugin globally.                                                                                                 |
| `update-checker` | Boolean | `true`  | Every 8 hours, query SpigotMC for the latest version and log a line when a newer release exists. Set to `false` to make no outgoing requests. |
| `locale`         | String  | `"en"`  | Language for plugin messages. Available: `"en"`, `"cs"`.                                                                                      |

The update checker uses 5 second timeouts, ignores responses that are not a version number, and compares versions
numerically, so running a build newer than the published release is not reported as "outdated". The setting is read on
every check, so `/disenchantment reload` applies a change without a restart.

### Config safety and backups

- **Syntax errors never wipe your config.** If `config.yml` cannot be parsed (for example a bad indent or an unclosed
  bracket), the plugin logs the parser error at `SEVERE`, leaves the file exactly as it is, saves a copy as
  `config.yml.broken-<timestamp>`, and runs on the built-in defaults. While in that state, changes made with commands or
  the GUI are **not** saved (so they cannot overwrite your file). Fix the file and run `/disenchantment reload`.
- **`/disenchantment reload` on a broken file** is refused: the sender sees the parser message and the settings that are
  currently running stay in effect.
- **Upgrades are backed up.** When a plugin update migrates your config to a newer format, the previous file is first
  copied to `config.yml.bak`. A config that is already current is not rewritten.

### Locale files

`plugins/Disenchantment/locales/<locale>.yml` is created on first start and never overwritten, so your edits are kept.
On every start and `/disenchantment reload`, any keys that exist in the bundled locale but are missing from your file
(for example after a plugin update) are added automatically with the bundled text. Existing values are not changed.

---

## Event Priorities

Controls the order in which Disenchantment's event listeners fire relative to other plugins. Adjust these if you
experience conflicts with other anvil-modifying plugins.

```yaml
event-priorities:
  disenchant-click: HIGHEST
  disenchant: HIGHEST
  shatter-click: HIGHEST
  shatter: HIGHEST
  grindstone-click: HIGHEST
  grindstone: HIGHEST
```

`grindstone-click` covers the plain-book placement and the result click on the grindstone, `grindstone` covers the
`PrepareGrindstoneEvent` (see [Grindstone Settings](#grindstone-settings)). Like the others, changing them needs a reload.

Available values (lowest to highest): `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR`.

`HIGHEST` is the default and ensures Disenchantment processes the event last. Lower values allow other plugins to
process the event first. `MONITOR` should only be used for read-only observation — do not use it if you need
Disenchantment to act on the event.

---

## Logging

Controls how much output the plugin sends to the server console and whether error reports are saved to disk.

```yaml
logging:
  level: INFO
  save-reports: true
  operations: false
  operations-retention-days: 0
  discord-webhook: ""
```

| Key                         | Type    | Default | Description                                                                                                      |
|-----------------------------|---------|---------|------------------------------------------------------------------------------------------------------------------|
| `level`                     | String  | `INFO`  | Log verbosity. Options: `NONE`, `INFO`, `DEBUG`.                                                                 |
| `save-reports`              | Boolean | `true`  | Write crash/error reports to `plugins/Disenchantment/logs/crash-<timestamp>.txt`.                                |
| `operations`                | Boolean | `false` | Record every disenchant/shatter to `plugins/Disenchantment/stats.db`.                                            |
| `operations-retention-days` | Integer | `0`     | Delete `stats.db` operation records older than this many days on startup. `0` keeps records forever.             |
| `discord-webhook`           | String  | `""`    | Discord webhook URL; if set, crash reports are POSTed asynchronously. Leave empty to disable.                    |

**Log level meanings:**

| Level   | What is logged                                                                                                 |
|---------|----------------------------------------------------------------------------------------------------------------|
| `NONE`  | Nothing except fatal errors that disable the plugin.                                                           |
| `INFO`  | Startup summary: NMS module resolved, plugin adapters activated. Recommended for production.                   |
| `DEBUG` | Everything in `INFO` plus active event priorities and detailed per-operation output. Use when troubleshooting. |

> **Tip:** Keep `save-reports: true`. When something goes wrong the full report is written to disk automatically, making
> it easy to attach to a bug report. You can also generate a report on demand with `/disenchantment diagnostic save`.

---

## Disenchantment Settings

Settings that control removing enchantments from items onto books.

### Feature Toggle and Restrictions

```yaml
disenchantment:
  enabled: true
  worlds:
    mode: DENYLIST
    list: []
  materials:
    mode: DENYLIST
    list: []
  enchantments-states: []
```

| Key                   | Type           | Default    | Description                                                                                 |
|-----------------------|----------------|------------|---------------------------------------------------------------------------------------------|
| `enabled`             | Boolean        | `true`     | Enable or disable the disenchantment feature.                                               |
| `worlds.mode`         | Enum           | `DENYLIST` | `DENYLIST` = `worlds.list` are blocked; `ALLOWLIST` = only `worlds.list` are allowed.       |
| `worlds.list`         | List\<String\> | `[]`       | World names (case-sensitive) the mode applies to.                                           |
| `materials.mode`      | Enum           | `DENYLIST` | `DENYLIST` = `materials.list` are blocked; `ALLOWLIST` = only `materials.list` are allowed. |
| `materials.list`      | List\<String\> | `[]`       | Materials (e.g. `DIAMOND_SWORD`, case-insensitive) the mode applies to.                     |
| `enchantments-states` | List\<String\> | `[]`       | Per-enchantment behavior overrides in `"key:state"` format (see below).                     |

> **Restriction modes.** Both `worlds` and `materials` use the same `mode` + `list` shape. `DENYLIST` (the default,
> and the behavior of the old `disabled-worlds`/`disabled-materials` keys) blocks the listed entries. `ALLOWLIST`
> permits *only* the listed entries — and an **empty `ALLOWLIST` blocks the feature entirely** (a warning is logged at
> startup). Servers upgrading from an older version are migrated automatically: the old lists become `list` with
> `mode: DENYLIST`, so behavior is unchanged. The `disenchantment.bypass.world` / `disenchantment.bypass.material`
> permissions override the restriction under either mode.

**Enchantment state format:** Each entry is a string like `"sharpness:keep"`. The key is the Bukkit enchantment key
(tab-completion is available in-game via the command).

**State comparison:**

| State     | Item after operation          | Book result                         | Blocks operation? | Use case                                                                       |
|-----------|-------------------------------|-------------------------------------|-------------------|--------------------------------------------------------------------------------|
| `enable`  | Enchantment removed           | Enchantment added to book           | No                | Default — normal disenchanting                                                 |
| `keep`    | Enchantment stays on item     | Enchantment NOT on book             | No                | Allow disenchanting the item, but prevent extracting this specific enchantment |
| `delete`  | Enchantment removed           | Enchantment NOT on book (destroyed) | No                | Silently destroy an unwanted enchantment during disenchanting                  |
| `disable` | No change (operation blocked) | No output produced                  | Yes               | Prevent any disenchanting of items that have this enchantment                  |

> **Example — prevent extracting Mending but allow disenchanting other enchantments:**
> ```yaml
> disenchantment:
>   enchantments-states:
>     - "mending:keep"
> ```
> A sword with Sharpness V + Mending I: the player gets a book with Sharpness V, the sword loses Sharpness V but **keeps
Mending I**.

> **Example — block disenchanting any item that has Silk Touch:**
> ```yaml
> disenchantment:
>   enchantments-states:
>     - "silk_touch:disable"
> ```

These can be managed without editing the file via `/disenchantment disenchant:enchantments`.
See [COMMANDS.md](COMMANDS.md#disenchant-configuration).

### Anvil Sound

```yaml
disenchantment:
  anvil:
    sound:
      enabled: true
      volume: 1.0
      pitch: 1.0
```

| Key                   | Type    | Default | Description                                  |
|-----------------------|---------|---------|----------------------------------------------|
| `anvil.sound.enabled` | Boolean | `true`  | Play a sound when a disenchantment succeeds. |
| `anvil.sound.volume`  | Double  | `1.0`   | Sound volume.                                |
| `anvil.sound.pitch`   | Double  | `1.0`   | Sound pitch.                                 |

### Repair Cost

Controls the XP cost players pay to disenchant.

```yaml
disenchantment:
  anvil:
    repair:
      reset: false
      cost: true
      base: 10
      multiply: 0.25
      enchantment-costs: {}
```

| Key                              | Type    | Default | Description                                                              |
|----------------------------------|---------|---------|--------------------------------------------------------------------------|
| `anvil.repair.reset`             | Boolean | `false` | Reset the item's repair cost to 0 after disenchanting.                   |
| `anvil.repair.cost`              | Boolean | `true`  | Enable XP cost for disenchanting. Set to `false` for free disenchanting. |
| `anvil.repair.base`              | Double  | `10`    | Base XP levels added to the cost before per-enchantment calculation.     |
| `anvil.repair.multiply`          | Double  | `0.25`  | Multiplier applied per enchantment level on top of the base cost.        |
| `anvil.repair.enchantment-costs` | Map     | `{}`    | Per-enchantment cost overrides (advanced; managed via GUI).              |

**Cost formula:** Enchantments are sorted from highest level to lowest, then processed in that order. Starting from
`base`, each enchantment adds `level × multiplier` to the running total. Crucially, the multiplier increments by
`multiply` after each enchantment, so items with more enchantments cost progressively more.

```
totalCost = base
multiplier = multiply

for each enchantment (sorted highest level first):
    totalCost += enchantment.level × multiplier
    multiplier += multiply

final cost = round(totalCost)
```

**Example:** `base = 10`, `multiply = 0.25`, item has Sharpness V and Fire Aspect II:

```
Start:              totalCost = 10,  multiplier = 0.25
+ Sharpness V:      10 + (5 × 0.25) = 11.25, multiplier → 0.50
+ Fire Aspect II:   11.25 + (2 × 0.50) = 12.25
Final XP cost:      12 levels
```

**Cost Calculator — what different settings produce:**

| base | multiply | Scenario                                                                         | Cost      |
|------|----------|----------------------------------------------------------------------------------|-----------|
| `10` | `0.25`   | Default — item with Sharpness V only                                             | 11 levels |
| `10` | `0.25`   | Default — item with Sharpness V + Fire Aspect II                                 | 12 levels |
| `5`  | `0`      | Flat cost — every disenchant costs exactly 5 levels regardless of enchantments   | 5 levels  |
| `0`  | `1`      | Pure per-level — Sharpness V costs 5, Fire Aspect II costs 2 (then 4 for second) | varies    |
| `0`  | `0`      | Free — XP cost is always 0 (same as setting `cost: false`)                       | 0 levels  |
| `1`  | `1`      | Simple — Sharpness V alone costs 6 levels (1 + 5×1)                              | 6 levels  |

> **Negative values:** the final XP cost is never below 0. A negative `base`, `multiply` or override cannot pay the
> player levels; it simply results in a free operation (the same applies to economy costs).

> **Per-enchantment overrides** in `enchantment-costs` bypass the formula entirely for that enchantment. The override
> value is added as a flat amount and the multiplier is not advanced for that enchantment.

### Economy Cost (Disenchantment)

Charges players an in-game currency cost per disenchant. Requires Vault. Disabled by default.

```yaml
disenchantment:
  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true
```

| Key                      | Type    | Default | Description                                                                                 |
|--------------------------|---------|---------|---------------------------------------------------------------------------------------------|
| `economy.enabled`        | Boolean | `false` | Charge players currency when disenchanting. Requires Vault.                                 |
| `economy.cost`           | Double  | `100.0` | Flat currency amount charged per disenchant operation.                                      |
| `economy.show-cost`      | Boolean | `true`  | Show the economy cost in the action bar while the player has a result pending in the anvil. |
| `economy.charge-message` | Boolean | `true`  | Send a chat confirmation message to the player after they are charged.                      |

**What players see when economy is enabled:**

- **Before clicking** — the cost is shown in the player's **action bar** (above the hotbar) as soon as a valid result
  appears in the anvil output slot. This updates live. Example: `Cost: $100.0`
- **On click** — the player takes the result and the currency is deducted. A **chat confirmation** is sent (e.g.
  `You have been charged $100.0`). This message can be suppressed with `charge-message: false`.
- **If the player cannot afford it** — the operation is **cancelled**, no items are consumed, and the player receives an
  insufficient-funds message. The same happens (with a "payment could not be processed" message) if the economy plugin
  rejects the withdrawal.
- **What is charged** — exactly the amount shown in the action bar: the flat `economy.cost`, or the sum of the
  per-enchantment economy overrides, counted only for enchantments that are actually transferred to the result book.
- **Action bar only (no chat)** — set `show-cost: true` and `charge-message: false` for a cleaner experience.

> **Requires:** [Vault](https://www.spigotmc.org/resources/34315/) and a compatible economy plugin such as EssentialsX,
> CMI, or PlayerPoints. See [INSTALLATION.md — Economy Integration](INSTALLATION.md#optional-economy-integration-vault).

> **Creative mode:** Economy costs are never charged to players in Creative mode, consistent with how XP costs are
> handled.

---

## Shatterment Settings

Settings that control splitting multi-enchantment books. The structure mirrors disenchantment settings. The `materials`
restriction is checked against the enchanted book being shattered.

### Feature Toggle and Restrictions (Shatter)

```yaml
shatterment:
  enabled: true
  split-count: 1
  worlds:
    mode: DENYLIST
    list: []
  materials:
    mode: DENYLIST
    list: []
  enchantments-states: []
```

| Key                   | Type           | Default    | Description                                                                                        |
|-----------------------|----------------|------------|----------------------------------------------------------------------------------------------------|
| `enabled`             | Boolean        | `true`     | Enable or disable the shatterment (book splitting) feature.                                        |
| `split-count`         | Integer        | `1`        | Number of enchantments to split off per shatter operation. Default is 1 (one enchantment per use). |
| `worlds.mode`         | Enum           | `DENYLIST` | Same semantics as disenchantment `worlds.mode`.                                                    |
| `worlds.list`         | List\<String\> | `[]`       | Worlds the mode applies to.                                                                        |
| `materials.mode`      | Enum           | `DENYLIST` | Same semantics as disenchantment `materials.mode` (checked against the enchanted book).            |
| `materials.list`      | List\<String\> | `[]`       | Materials the mode applies to.                                                                     |
| `enchantments-states` | List\<String\> | `[]`       | Per-enchantment behavior overrides, same format as disenchantment.                                 |

> The restriction-mode notes from the disenchantment section apply here too. In-game, admins can toggle these modes
> via the Worlds/Materials GUIs (a "Restriction Mode" button: left-click cycles disenchantment, right-click cycles
> shatterment) or the commands `/disenchantment disenchant:worlds mode <ALLOWLIST|DENYLIST>` (and the `shatter:worlds`
> / `disenchant:materials` equivalents).

### Anvil Sound (Shatter)

```yaml
shatterment:
  anvil:
    sound:
      enabled: true
      volume: 1.0
      pitch: 1.0
```

| Key                   | Type    | Default | Description                           |
|-----------------------|---------|---------|---------------------------------------|
| `anvil.sound.enabled` | Boolean | `true`  | Play a sound when a shatter succeeds. |
| `anvil.sound.volume`  | Double  | `1.0`   | Sound volume.                         |
| `anvil.sound.pitch`   | Double  | `1.0`   | Sound pitch.                          |

### Repair Cost (Shatter)

```yaml
shatterment:
  anvil:
    repair:
      reset: false
      cost: true
      base: 10
      multiply: 0.25
      enchantment-costs: {}
```

| Key                              | Type    | Default | Description                                         |
|----------------------------------|---------|---------|-----------------------------------------------------|
| `anvil.repair.reset`             | Boolean | `false` | Reset the book's repair cost to 0 after shattering. |
| `anvil.repair.cost`              | Boolean | `true`  | Enable XP cost for shattering.                      |
| `anvil.repair.base`              | Double  | `10`    | Base XP cost before per-enchantment calculation.    |
| `anvil.repair.multiply`          | Double  | `0.25`  | Multiplier per enchantment level.                   |
| `anvil.repair.enchantment-costs` | Map     | `{}`    | Per-enchantment cost overrides.                     |

The cost formula is identical to the disenchantment formula above.

### Economy Cost (Shatterment)

```yaml
shatterment:
  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true
```

| Key                      | Type    | Default | Description                                                   |
|--------------------------|---------|---------|---------------------------------------------------------------|
| `economy.enabled`        | Boolean | `false` | Charge players currency when shattering. Requires Vault.      |
| `economy.cost`           | Double  | `100.0` | Flat currency amount charged per shatter operation.           |
| `economy.show-cost`      | Boolean | `true`  | Show cost in action bar while result is pending.              |
| `economy.charge-message` | Boolean | `true`  | Send a chat confirmation message after the player is charged. |

The same Vault requirements and Creative-mode exemption apply as for disenchantment.

---

## Grindstone Settings

Two opt-in features that work on the **grindstone** instead of the anvil. Both are off by default, are independent of
each other (`grindstone.enabled` only controls extraction, `grindstone.transfer.enabled` only transfer), and leave every
other grindstone use (repairing two items, vanilla disenchanting a single item) completely vanilla.

They reuse the `disenchantment` section for: `worlds`, `materials`, `enchantments-states` (`KEEP`, `DISABLE`, `DELETE`),
`enchantment-chances`, `cooldown` and `cost-multiplier`. Cost, economy and sound are configured under `grindstone`.

Unlike the anvil, the grindstone has no cost label, so the price is shown in the **action bar**. There is **no XP refund**:
the plugin takes over the result, so vanilla's XP orbs for the inputs are not awarded.

> **Server support.** The result is shown through `PrepareGrindstoneEvent`, which Spigot added in 1.19.4 (Paper has had
> its own copy since 1.16). On older servers the plugin recomputes the result one tick after every grindstone click
> instead; an INFO line is logged at startup. Event priorities for both handlers are in
> [Event Priorities](#event-priorities).

### Grindstone Extract

Put **one enchanted item** in the top slot and a **plain book** in the bottom slot: the result is an enchanted book with
the item's transferable enchantments. Taking it removes exactly those enchantments from the item (it returns to the top
slot), consumes one book, and costs levels.

The vanilla grindstone refuses a plain book in its input slots, so the plugin places it for you: **click the bottom slot
with a book on the cursor**, or **shift-click a book** in your inventory, while the item is already in the top slot.
Exactly one book is placed even from a stack. Number-key swaps and drag-placing are not intercepted.

```yaml
grindstone:
  enabled: false
  curses: false
  repair:
    reset: false
    cost: true
    base: 10
    multiply: 0.25
    enchantment-costs: {}
    max-cost: -1
  sound:
    enabled: true
    volume: 1.0
    pitch: 1.0
  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true
```

| Key                          | Type    | Default | Description                                                                                     |
|------------------------------|---------|---------|-------------------------------------------------------------------------------------------------|
| `enabled`                    | Boolean | `false` | Enable extraction (item + plain book).                                                          |
| `curses`                     | Boolean | `false` | `false`: curses stay on the item (like the vanilla grindstone). `true`: they move to the book too. Also applies to transfer. |
| `repair.reset`               | Boolean | `false` | Reset the item's anvil repair cost after extraction.                                            |
| `repair.cost`                | Boolean | `true`  | Charge experience levels.                                                                       |
| `repair.base`                | Double  | `10`    | Base cost. Formula: `base + sum(level x multiply)`, same as the anvil.                          |
| `repair.multiply`            | Double  | `0.25`  | Per-enchantment multiplier (increases with every enchantment, highest level first).             |
| `repair.enchantment-costs`   | Map     | `{}`    | Per-enchantment fixed cost, `sharpness: 5` or `mending: { xp: 10, economy: 500 }`.              |
| `repair.max-cost`            | Integer | `-1`    | Cap on the level cost, `-1` = none.                                                             |
| `sound.enabled/volume/pitch` | -       | on/1/1  | `block.grindstone.use` played on success.                                                       |
| `economy.*`                  | -       | off     | Same keys and rules as the [anvil economy cost](#economy-cost-disenchantment).                  |

Enchantment **chances** apply as with the anvil: a failed roll destroys that enchantment, and if every roll fails the
player receives a plain book. `DELETE` enchantments are destroyed on the item, `KEEP` ones stay, a `DISABLE` one blocks
the operation.

### Grindstone Transfer

Put an **enchanted item** in the top slot and a **different kind of item** (not a book) in the bottom slot: the result
is the bottom item carrying the enchantments it can take. Unlike the anvil, the source item is **not destroyed**: it
returns to the top slot minus exactly the enchantments that moved. Two items of the **same material** are a vanilla repair
and are never touched. Put the source in the top slot and the receiving item in the bottom slot.

An enchantment moves only if: it is a vanilla enchantment, the target can take it (`canEnchantItem`), it conflicts with
nothing already on the target or already chosen, the target does not already have it at the same or a higher level, and
the source enchantment is not `KEEP` (a `DISABLE` state blocks the whole transfer). Levels above the enchantment's vanilla
maximum are lowered to the maximum unless `allow-over-max` is `true` (the source still loses the enchantment).
Enchantments that cannot move simply stay on the source.

```yaml
grindstone:
  transfer:
    enabled: false
    allow-over-max: false
    max-enchantments: 16
    repair:
      reset: false
      cost: true
      base: 15
      multiply: 0.25
      enchantment-costs: {}
      max-cost: -1
    economy:
      enabled: false
      cost: 100.0
      show-cost: true
      charge-message: true
```

| Key                        | Type    | Default | Description                                                                         |
|----------------------------|---------|---------|-------------------------------------------------------------------------------------|
| `transfer.enabled`         | Boolean | `false` | Enable item-to-item transfer.                                                       |
| `transfer.allow-over-max`  | Boolean | `false` | Keep levels above the vanilla maximum instead of lowering them.                     |
| `transfer.max-enchantments`| Integer | `16`    | Safety cap on enchantments moved per transfer (highest levels first).               |
| `transfer.repair.*`        | -       | base 15 | Same keys as `grindstone.repair`; `reset` resets the repair cost of both items.     |
| `transfer.economy.*`       | -       | off     | Same keys as `grindstone.economy`.                                                  |

The material restriction (`disenchantment.materials`) is checked against the **source** item.

---

## Economy Integration

Economy support is disabled by default and entirely optional.

When enabled, Disenchantment hooks into Vault automatically on startup. The startup log will include
`Economy (Vault): hooked` on success or `Economy (Vault): not available` if Vault or an economy plugin is missing.

### What players see

- When a valid result appears in the anvil, the economy cost is shown in the **action bar** (above the hotbar). This
  updates live.
- When the player takes the result, the cost is deducted and a **chat confirmation** is sent (if
  `charge-message: true`).
- If the player cannot afford the operation, it is **cancelled** and they see an insufficient-funds message. No items
  are consumed.
- Players in **Creative mode** are never charged.

### Configuration summary

| Setting                   | Config key                              | Default |
|---------------------------|-----------------------------------------|---------|
| Disenchant economy on/off | `disenchantment.economy.enabled`        | `false` |
| Disenchant cost           | `disenchantment.economy.cost`           | `100.0` |
| Disenchant action bar     | `disenchantment.economy.show-cost`      | `true`  |
| Disenchant charge message | `disenchantment.economy.charge-message` | `true`  |
| Shatter economy on/off    | `shatterment.economy.enabled`           | `false` |
| Shatter cost              | `shatterment.economy.cost`              | `100.0` |
| Shatter action bar        | `shatterment.economy.show-cost`         | `true`  |
| Shatter charge message    | `shatterment.economy.charge-message`    | `true`  |
| Grindstone extract economy on/off | `grindstone.economy.enabled`    | `false` |
| Grindstone extract cost           | `grindstone.economy.cost`       | `100.0` |
| Grindstone transfer economy on/off| `grindstone.transfer.economy.enabled` | `false` |
| Grindstone transfer cost          | `grindstone.transfer.economy.cost`    | `100.0` |

See also [COMMANDS.md — Disenchant Economy](COMMANDS.md#disenchant-economy)
and [COMMANDS.md — Shatter Economy](COMMANDS.md#shatter-economy) for commands that change these settings without editing
the file.
