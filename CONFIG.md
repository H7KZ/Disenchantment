# Config file handbook

1. [Plugin configuration](#plugin-configuration)
    - [enabled](#enabled)
    - [enable-logging](#enable-logging)
    - [logging-level](#logging-level)
2. [Disenchantment configuration](#disenchantment-configuration)
    - [disable-disenchantment](#disable-disenchantment)
    - [disabled-disenchantment-worlds](#disabled-disenchantment-worlds)
    - [disabled-disenchantment-materials](#disabled-disenchantment-materials)
    - [disenchantment-enchantments-status](#disenchantment-enchantments-status)
    - [disenchantment-enable-anvil-sound](#disenchantment-enable-anvil-sound)
    - [disenchantment-anvil-volume](#disenchantment-anvil-volume)
    - [disenchantment-anvil-pitch](#disenchantment-anvil-pitch)
    - [disenchantment-enable-repair-reset](#disenchantment-enable-repair-reset)
    - [disenchantment-enable-repair-cost](#disenchantment-enable-repair-cost)
    - [disenchantment-base](#disenchantment-base)
    - [disenchantment-multiply](#disenchantment-multiply)
3. [Shatterment configuration](#shatterment-configuration)
    - [disable-shatterment](#disable-shatterment)
    - [disabled-shatterment-worlds](#disabled-shatterment-worlds)
    - [shatterment-enchantments-status](#shatterment-enchantments-status)
    - [shatterment-enable-anvil-sound](#shatterment-enable-anvil-sound)
    - [shatterment-anvil-volume](#shatterment-anvil-volume)
    - [shatterment-anvil-pitch](#shatterment-anvil-pitch)
    - [shatterment-enable-repair-reset](#shatterment-enable-repair-reset)
    - [shatterment-enable-repair-cost](#shatterment-enable-repair-cost)
    - [shatterment-base](#shatterment-base)
    - [shatterment-multiply](#shatterment-multiply)
4. [Full config with comments](#full-config-with-comments)

## Plugin configuration

### `enabled`

- Enable or disable the plugin
- Type: `boolean`
- Default: `true`

```yaml
enabled: true
```

### `enable-logging`

- Enable or disable the log
- Type: `boolean`
- Default: `true`

```yaml
enable-logging: true
```

### `logging-level`

- Logging level: INFO, DEBUG
- Type: `string`
- Default: `INFO`

```yaml
logging-level: "INFO"
```

## Disenchantment configuration

### `disable-disenchantment`

- Enable or disable the disenchantment
- Type: `boolean`
- Default: `false`

```yaml
disable-disenchantment: false
```

### `disabled-disenchantment-worlds`

- List of worlds where the plugin is disabled
- Type: `list` of `string`
- Default: `[ ]`

```yaml
disabled-disenchantment-worlds: ['world', 'world_nether']
```

### `disabled-disenchantment-materials`

- List of materials that are not allowed to be disenchanted
- Type: `list` of `string`
- Default: `[ ]`

```yaml
disabled-disenchantment-materials: ['DIAMOND_SWORD', 'DIAMOND_AXE']
```

### `disenchantment-enchantments-status`

- List of enchantments status for item disenchantment
- Type: `map` of `string`
- Default: `[ ]`

```yaml
disenchantment-enchantments-status:
  sharpness: keep
  knockback: disabled
```

### `disenchantment-enable-anvil-sound`

- Enable or disable the anvil sound
- Type: `boolean`
- Default: `true`

```yaml
disenchantment-enable-anvil-sound: true
```

### `disenchantment-anvil-volume`

- Edit the volume of the anvil sound
- Type: `double`
- Default: `1.0`

```yaml
disenchantment-anvil-volume: 2.4
```

### `disenchantment-anvil-pitch`

- Edit the pitch of the anvil sound
- Type: `double`
- Default: `1.0`


```yaml
disenchantment-anvil-pitch: 3.6
```

### `disenchantment-enable-repair-reset`

- Enable or disable the repair cost reset back to 0 when the item is disenchanted
- Type: `boolean`
- Default: `false`

```yaml
disenchantment-enable-repair-reset: true
```

### `disenchantment-enable-repair-cost`

- Enable or disable the repair cost
- Type: `boolean`
- Default: `true`

```yaml
disenchantment-enable-repair-cost: true
```

### `disenchantment-base`

- Base value for the cost of the disenchantment
- Type: `int`
- Default: `10`

```yaml
disenchantment-base: 15
```

### `disenchantment-multiply`

- This multiplication is applied to the base value
- Type: `double`
- Default: `0.25`

```yaml
disenchantment-multiply: 0.34
```

## Shatterment configuration

### `disable-shatterment`

- Enable or disable the book splitting
- Type: `boolean`
- Default: `false`

```yaml
disable-shatterment: false
```

### `disabled-shatterment-worlds`

- List of worlds where the book splitting is disabled
- Type: `list` of `string`
- Default: `[ ]`

```yaml
disabled-shatterment-worlds: ['world', 'world_nether']
```

### `shatterment-enchantments-status`

- List of enchantments status for book splitting
- Type: `map` of `string`
- Default: `[ ]`

```yaml
shatterment-enchantments-status:
  sharpness: keep
  knockback: disabled
```

### `shatterment-enable-anvil-sound`

- Enable or disable the anvil sound
- Type: `boolean`
- Default: `true`

```yaml
shatterment-enable-anvil-sound: true
```

### `shatterment-anvil-volume`

- Edit the volume of the anvil sound
- Type: `double`
- Default: `1.0`

```yaml
shatterment-anvil-volume: 2.4
```

### `shatterment-anvil-pitch`

- Edit the pitch of the anvil sound
- Type: `double`
- Default: `1.0`

```yaml
shatterment-anvil-pitch: 3.6
```

### `shatterment-enable-repair-reset`

- Enable or disable the repair cost reset back to 0 when the item is disenchanted
- Type: `boolean`
- Default: `false`

```yaml
shatterment-enable-repair-reset: true
```

### `shatterment-enable-repair-cost`

- Enable or disable the repair cost
- Type: `boolean`
- Default: `true`

```yaml
shatterment-enable-repair-cost: true
```

### `shatterment-base`

- Base value for the cost of the disenchantment
- Type: `int`
- Default: `10`

```yaml
shatterment-base: 15
```

### `shatterment-multiply`

- This multiplication is applied to the base value
- Type: `double`
- Default: `0.25`

```yaml
shatterment-multiply: 0.34
```

## Full config with comments

<details>
<summary>Full config with comments</summary>
<br>

```yaml
# Enable or disable the plugin
enabled: true

# Enable or disable the log
enable-logging: true

# Logging level: INFO, DEBUG
logging-level: "INFO"


# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------


# Enable or disable the disenchantment
disable-disenchantment: false

# List of worlds where the plugin is disabled
disabled-disenchantment-worlds: [ ]

# List of materials that are not allowed to be disenchanted
disabled-disenchantment-materials: [ ]

# List of enchantments status for item disenchantment.
disenchantment-enchantments-status:
# Here is example of how it should look like.
# enchantments can take value "keep" and "disabled" (by default the enchantment is enabled)
# for example:
#sharpness: keep
#knockback: disabled

# Enable or disable the anvil sound
disenchantment-enable-anvil-sound: true

# Edit the volume and pitch of the anvil sound
disenchantment-anvil-volume: 1.0
disenchantment-anvil-pitch: 1.0

# Enable or disable the repair cost reset back to 0 when the item is disenchanted
disenchantment-enable-repair-reset: false

# Enable or disable the repair cost
disenchantment-enable-repair-cost: true

# Base value for the cost of the disenchantment
disenchantment-base: 10

# This multiplication is applied to the base value
# The enchantments are sorted by level in descending order, so the more enchantments you have, the more expensive it is
disenchantment-multiply: 0.25
# Multiplication value from 0 to infinity (0.25 = 25%)
#
# It is calculated by the following:
#   base + (level * multiply)
#
# Example: base = 10, sharpness = 5, knockback = 1, multiplier = 0.25
#
#   10 + (1 * 1) = 11  ->  11 + (5 * 1.25) = 17.25  ->  17.25


# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------


# Enable or disable the book splitting
disable-shatterment: false

# List of worlds where the book splitting is disabled
disabled-shatterment-worlds: [ ]

# List of enchantments status for book splitting
shatterment-enchantments-status:
# Here is example of how it should look like.
# enchantments can take value "keep" and "disabled" (by default the enchantment is enabled)
# for example:
#sharpness: keep
#knockback: disabled

# Enable or disable the anvil sound
shatterment-enable-anvil-sound: true

# Edit the volume and pitch of the anvil sound
shatterment-anvil-volume: 1.0
shatterment-anvil-pitch: 1.0

# Enable or disable the repair cost reset back to 0 when the item is disenchanted
shatterment-enable-repair-reset: false

# Enable or disable the repair cost
shatterment-enable-repair-cost: true

# Base value for the cost of the disenchantment
shatterment-base: 10

# This multiplication is applied to the base value
# The enchantments are sorted by level in descending order, so the more enchantments you have, the more expensive it is
shatterment-multiply: 0.25
# Multiplication value from 0 to infinity (0.25 = 25%)
#
# It is calculated by the following:
#   base + (level * multiply)
#
# Example: base = 10, sharpness = 5, knockback = 1, multiplier = 0.25
#
#   10 + (1 * 1) = 11  ->  11 + (5 * 1.25) = 17.25  ->  17.25
```

</details>
