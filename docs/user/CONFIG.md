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
migration: 9
locales: [ "cs", "en" ]
# ↑ DO NOT EDIT THESE VALUES ↑

# Enable or disable the entire plugin globally.
enabled: true

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

# ─────────────────────────────────────────────────────────────────────────────

logging:
  # NONE = silent | INFO = startup summary | DEBUG = full operation tracing
  level: INFO
  # Write crash/error reports to plugins/Disenchantment/logs/crash-<timestamp>.txt
  save-reports: true

# ─────────────────────────────────────────────────────────────────────────────

disenchantment:
  # Enable or disable the disenchanting feature.
  enabled: true

  # Worlds where disenchanting is blocked (case-sensitive world names).
  disabled-worlds: []

  # Materials that cannot be disenchanted (e.g. DIAMOND_SWORD).
  disabled-materials: []

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

  # Worlds where shattering is blocked.
  disabled-worlds: []

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
- [Economy Integration](#economy-integration)

---

## General Settings

```yaml
enabled: true
locale: "en"
```

| Key       | Type    | Default | Description                                              |
|-----------|---------|---------|----------------------------------------------------------|
| `enabled` | Boolean | `true`  | Enable or disable the entire plugin globally.            |
| `locale`  | String  | `"en"`  | Language for plugin messages. Available: `"en"`, `"cs"`. |

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
```

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
```

| Key            | Type    | Default | Description                                                                       |
|----------------|---------|---------|-----------------------------------------------------------------------------------|
| `level`        | String  | `INFO`  | Log verbosity. Options: `NONE`, `INFO`, `DEBUG`.                                  |
| `save-reports` | Boolean | `true`  | Write crash/error reports to `plugins/Disenchantment/logs/crash-<timestamp>.txt`. |

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
  disabled-worlds: []
  disabled-materials: []
  enchantments-states: []
```

| Key                   | Type           | Default | Description                                                                                        |
|-----------------------|----------------|---------|----------------------------------------------------------------------------------------------------|
| `enabled`             | Boolean        | `true`  | Enable or disable the disenchantment feature.                                                      |
| `disabled-worlds`     | List\<String\> | `[]`    | Worlds where disenchanting is blocked. World names are case-sensitive.                             |
| `disabled-materials`  | List\<String\> | `[]`    | Materials that cannot be disenchanted (e.g. `DIAMOND_SWORD`). Material names are case-insensitive. |
| `enchantments-states` | List\<String\> | `[]`    | Per-enchantment behavior overrides in `"key:state"` format (see below).                            |

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
  insufficient-funds message.
- **Action bar only (no chat)** — set `show-cost: true` and `charge-message: false` for a cleaner experience.

> **Requires:** [Vault](https://www.spigotmc.org/resources/34315/) and a compatible economy plugin such as EssentialsX,
> CMI, or PlayerPoints. See [INSTALLATION.md — Economy Integration](INSTALLATION.md#optional-economy-integration-vault).

> **Creative mode:** Economy costs are never charged to players in Creative mode, consistent with how XP costs are
> handled.

---

## Shatterment Settings

Settings that control splitting multi-enchantment books. The structure mirrors disenchantment settings, except there is
no `disabled-materials` option (shattering only applies to enchanted books).

### Feature Toggle and Restrictions (Shatter)

```yaml
shatterment:
  enabled: true
  split-count: 1
  disabled-worlds: []
  enchantments-states: []
```

| Key                   | Type           | Default | Description                                                                                        |
|-----------------------|----------------|---------|----------------------------------------------------------------------------------------------------|
| `enabled`             | Boolean        | `true`  | Enable or disable the shatterment (book splitting) feature.                                        |
| `split-count`         | Integer        | `1`     | Number of enchantments to split off per shatter operation. Default is 1 (one enchantment per use). |
| `disabled-worlds`     | List\<String\> | `[]`    | Worlds where shattering is blocked.                                                                |
| `enchantments-states` | List\<String\> | `[]`    | Per-enchantment behavior overrides, same format as disenchantment.                                 |

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

See also [COMMANDS.md — Disenchant Economy](COMMANDS.md#disenchant-economy)
and [COMMANDS.md — Shatter Economy](COMMANDS.md#shatter-economy) for commands that change these settings without editing
the file.
