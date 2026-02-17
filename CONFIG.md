# Configuration Reference

Complete reference for `plugins/Disenchantment/config.yml`. All settings can also be changed in-game
via [commands](COMMANDS.md) or the [GUI](COMMANDS.md#general).

## Table of Contents

- [General Settings](#general-settings)
- [Event Priorities](#event-priorities)
- [Disenchantment Settings](#disenchantment-settings)
- [Shatterment Settings](#shatterment-settings)

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
