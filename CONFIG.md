# Configuration Reference

Complete reference for `plugins/Disenchantment/config.yml`. All settings can also be changed in-game
via [commands](COMMANDS.md) or the [GUI](COMMANDS.md#general).

## Table of Contents

- [General Settings](#general-settings)
- [Event Priorities](#event-priorities)
- [Logging](#logging)
- [Disenchantment Settings](#disenchantment-settings)
- [Shatterment Settings](#shatterment-settings)
- [Economy Integration](#economy-integration)

---

## General Settings

```yaml
enabled: true
locale: "en"
```

| Key       | Type    | Default | Description                                              |
|-----------|---------|---------|----------------------------------------------------------|
| `enabled` | Boolean | `true`  | Enable or disable the entire plugin.                     |
| `locale`  | String  | `"en"`  | Language for plugin messages. Available: `"en"`, `"cs"`. |

---

## Event Priorities

Controls the order in which Disenchantment's event listeners fire relative to other plugins. Useful for resolving
conflicts with other anvil plugins.

```yaml
event-priorities:
  disenchant-click: HIGHEST
  disenchant: HIGHEST
  shatter-click: HIGHEST
  shatter: HIGHEST
```

Available priorities (lowest to highest): `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR`.

See the [Spigot EventPriority docs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html) for
details.

---

## Logging

Controls how much the plugin logs to the server console and whether crash reports are saved to disk.

```yaml
logging:
  level: INFO
  save-reports: true
```

| Key            | Type    | Default | Description                                                                           |
|----------------|---------|---------|---------------------------------------------------------------------------------------|
| `level`        | String  | `INFO`  | Log verbosity. Options: `NONE`, `INFO`, `DEBUG`. See table below.                     |
| `save-reports` | Boolean | `true`  | Write crash reports to `plugins/Disenchantment/logs/crash-<timestamp>.txt` on errors. |

**Log levels:**

| Level   | What is logged                                                                        |
|---------|---------------------------------------------------------------------------------------|
| `NONE`  | Nothing except fatal errors that disable the plugin.                                  |
| `INFO`  | Startup summary: NMS module resolved, plugin adapters activated. *(recommended)*      |
| `DEBUG` | Everything in `INFO`, plus active event priorities and detailed per-operation output. |

> **Tip:** `save-reports: true` is strongly recommended. When something goes wrong the full diagnostic report
> is automatically written to disk, making it much easier to attach to a bug report without needing to
> reproduce the issue. You can also generate a report on demand with `/disenchantment diagnostic save`.

---

## Disenchantment Settings

Controls the behavior of removing enchantments from items onto books.

### Feature Toggle and Restrictions

```yaml
disenchantment:
  enabled: true
  disabled-worlds: []
  disabled-materials: []
  enchantments-states: []
```

| Key                   | Type           | Default | Description                                                    |
|-----------------------|----------------|---------|----------------------------------------------------------------|
| `enabled`             | Boolean        | `true`  | Enable or disable the disenchantment feature.                  |
| `disabled-worlds`     | List\<String\> | `[]`    | Worlds where disenchanting is disabled.                        |
| `disabled-materials`  | List\<String\> | `[]`    | Materials that cannot be disenchanted (e.g., `DIAMOND_SWORD`). |
| `enchantments-states` | List\<String\> | `[]`    | Per-enchantment behavior overrides.                            |

**Enchantment states format:** `"enchantment:state"` where state is one of:

| State     | Behavior                                                                      |
|-----------|-------------------------------------------------------------------------------|
| `enable`  | Enchantment can be removed from items (default).                              |
| `keep`    | Enchantment stays on the item and is not transferred.                         |
| `delete`  | Enchantment is removed from the item but destroyed (not transferred to book). |
| `disable` | Blocks the entire disenchantment process if the enchantment is present.       |

### Anvil Sound

```yaml
disenchantment:
  anvil:
    sound:
      enabled: true
      volume: 1.0
      pitch: 1.0
```

| Key                   | Type    | Default | Description                      |
|-----------------------|---------|---------|----------------------------------|
| `anvil.sound.enabled` | Boolean | `true`  | Play a sound when disenchanting. |
| `anvil.sound.volume`  | Double  | `1.0`   | Sound volume.                    |
| `anvil.sound.pitch`   | Double  | `1.0`   | Sound pitch.                     |

### Repair Cost

```yaml
disenchantment:
  anvil:
    repair:
      reset: false
      cost: true
      base: 10
      multiply: 0.25
```

| Key                     | Type    | Default | Description                                            |
|-------------------------|---------|---------|--------------------------------------------------------|
| `anvil.repair.reset`    | Boolean | `false` | Reset the item's repair cost to 0 after disenchanting. |
| `anvil.repair.cost`     | Boolean | `true`  | Enable XP cost for disenchanting.                      |
| `anvil.repair.base`     | Double  | `10`    | Base XP cost.                                          |
| `anvil.repair.multiply` | Double  | `0.25`  | Multiplier applied per enchantment level.              |

**Cost formula:** Enchantments are processed in ascending level order. For each enchantment:

```
cost = cost + (level * (1 + multiply))
```

**Example:** `base = 10`, Sharpness V, Fire Aspect I, `multiply = 0.25`:

```
10 + (1 * 1) = 11  ->  11 + (5 * 1.25) = 17.25
Final cost: 17
```

### Economy Cost (Disenchantment)

```yaml
disenchantment:
  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true
```

| Key                | Type    | Default | Description                                                                           |
|--------------------|---------|---------|---------------------------------------------------------------------------------------|
| `economy.enabled`  | Boolean | `false` | Charge players an in-game currency cost when disenchanting. Requires Vault.           |
| `economy.cost`     | Double  | `100.0` | Flat currency amount charged per disenchant operation.                                |
| `economy.show-cost`| Boolean | `true`  | Show the economy cost in an action bar message while the player has a result pending. |
| `economy.charge-message` | Boolean | `true` | Send a chat confirmation message after the player is charged.                   |

> **Requires:** [Vault](https://www.spigotmc.org/resources/34315/) and a compatible economy plugin
> (e.g. EssentialsX, CMI, PlayerPoints). If `economy.enabled` is `true` but Vault is not installed,
> operations will be blocked and players will see an error message. A warning is also printed to the
> server console on startup.

> **Creative mode:** Economy costs are never charged to players in Creative mode, consistent with how
> XP costs are handled.

---

## Shatterment Settings

Controls the behavior of splitting enchanted books. The settings mirror the disenchantment settings above, except there
is no `disabled-materials` option (shatterment only applies to books).

### Feature Toggle and Restrictions

```yaml
shatterment:
  enabled: true
  disabled-worlds: []
  enchantments-states: []
```

| Key                   | Type           | Default | Description                                                         |
|-----------------------|----------------|---------|---------------------------------------------------------------------|
| `enabled`             | Boolean        | `true`  | Enable or disable the shatterment (book splitting) feature.         |
| `disabled-worlds`     | List\<String\> | `[]`    | Worlds where book splitting is disabled.                            |
| `enchantments-states` | List\<String\> | `[]`    | Per-enchantment behavior overrides (same format as disenchantment). |

### Anvil Sound

```yaml
shatterment:
  anvil:
    sound:
      enabled: true
      volume: 1.0
      pitch: 1.0
```

| Key                   | Type    | Default | Description                        |
|-----------------------|---------|---------|------------------------------------|
| `anvil.sound.enabled` | Boolean | `true`  | Play a sound when splitting books. |
| `anvil.sound.volume`  | Double  | `1.0`   | Sound volume.                      |
| `anvil.sound.pitch`   | Double  | `1.0`   | Sound pitch.                       |

### Repair Cost

```yaml
shatterment:
  anvil:
    repair:
      reset: false
      cost: true
      base: 10
      multiply: 0.25
```

| Key                     | Type    | Default | Description                                        |
|-------------------------|---------|---------|----------------------------------------------------|
| `anvil.repair.reset`    | Boolean | `false` | Reset the book's repair cost to 0 after splitting. |
| `anvil.repair.cost`     | Boolean | `true`  | Enable XP cost for book splitting.                 |
| `anvil.repair.base`     | Double  | `10`    | Base XP cost.                                      |
| `anvil.repair.multiply` | Double  | `0.25`  | Multiplier applied per enchantment level.          |

The cost formula is the same as for disenchantment.

### Economy Cost (Shatterment)

```yaml
shatterment:
  economy:
    enabled: false
    cost: 100.0
    show-cost: true
    charge-message: true
```

| Key                      | Type    | Default | Description                                                                           |
|--------------------------|---------|---------|---------------------------------------------------------------------------------------|
| `economy.enabled`        | Boolean | `false` | Charge players an in-game currency cost when splitting books. Requires Vault.         |
| `economy.cost`           | Double  | `100.0` | Flat currency amount charged per shatter operation.                                   |
| `economy.show-cost`      | Boolean | `true`  | Show the economy cost in an action bar message while the player has a result pending. |
| `economy.charge-message` | Boolean | `true`  | Send a chat confirmation message after the player is charged.                         |

The same Vault requirements and Creative-mode exemption apply as described for disenchantment above.

---

## Economy Integration

Economy support is **disabled by default** and entirely optional. When enabled, it requires:

1. [Vault](https://www.spigotmc.org/resources/34315/) installed on the server.
2. A Vault-compatible economy plugin (e.g. [EssentialsX](https://essentialsx.net/),
   [CMI](https://www.spigotmc.org/resources/3742/), [PlayerPoints](https://www.spigotmc.org/resources/80590/)).

Disenchantment hooks into Vault automatically on startup if it is present. The currency format displayed in
messages (e.g. `$100.00` vs `100 coins`) is controlled entirely by your economy plugin.

### Configuration summary

| Feature       | Config key                              | Default |
|---------------|-----------------------------------------|---------|
| Disenchant economy on/off | `disenchantment.economy.enabled` | `false` |
| Disenchant cost           | `disenchantment.economy.cost`    | `100.0` |
| Disenchant action bar     | `disenchantment.economy.show-cost` | `true` |
| Disenchant charge message | `disenchantment.economy.charge-message` | `true` |
| Shatter economy on/off    | `shatterment.economy.enabled`    | `false` |
| Shatter cost              | `shatterment.economy.cost`       | `100.0` |
| Shatter action bar        | `shatterment.economy.show-cost`  | `true`  |
| Shatter charge message    | `shatterment.economy.charge-message` | `true` |

### Player experience

- When items are placed in the anvil and a valid result appears, the economy cost is shown in the **action bar**
  (the line above the hotbar). This refreshes while the result is visible so the player always knows what
  they'll be charged before clicking.
- When the player takes the result, the cost is deducted and a **chat confirmation** is sent (if
  `charge-message: true`).
- If the player cannot afford the operation, it is **cancelled** and they receive an insufficient-funds
  message in chat. No items are consumed.
- Players in **Creative mode** are never charged.
