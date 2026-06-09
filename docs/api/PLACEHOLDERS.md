<!-- generated-by: gsd-doc-writer -->

# PlaceholderAPI Integration

Disenchantment registers a PlaceholderAPI expansion under the `disenchantment` identifier. These placeholders can be
used in any plugin that supports PlaceholderAPI (scoreboards, chat formats, holograms, etc.).

## Requirements

- [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) must be installed on the server.

No additional setup is required. The expansion is registered automatically when Disenchantment loads and PlaceholderAPI
is detected.

## Available Placeholders

| Placeholder                           | Returns                       | Description                                                       |
|---------------------------------------|-------------------------------|-------------------------------------------------------------------|
| `%disenchantment_enabled%`            | `true` / `false`              | Whether the Disenchantment plugin as a whole is enabled           |
| `%disenchantment_disenchant_enabled%` | `true` / `false`              | Whether the disenchant feature is currently enabled               |
| `%disenchantment_shatter_enabled%`    | `true` / `false`              | Whether the shatter (book-splitting) feature is currently enabled |
| `%disenchantment_version%`            | version string (e.g. `6.4.0`) | The currently running version of Disenchantment                   |

All values are returned as plain strings. Boolean values are the lowercase strings `true` or `false`.

## Example Usage

### Scoreboard (using a scoreboard plugin)

```yaml
lines:
  - "Disenchant: %disenchantment_disenchant_enabled%"
  - "Shatter: %disenchantment_shatter_enabled%"
```

### Chat Format (using a chat plugin)

```
Plugin version: %disenchantment_version%
```

### Conditional Display (using a plugin that supports PlaceholderAPI conditions)

```yaml
# Show a line only when disenchanting is active
condition: "%disenchantment_disenchant_enabled% == true"
value: "Disenchanting is available!"
```

## Expansion Details

The expansion is registered with the identifier `disenchantment` and persists across PlaceholderAPI reloads (
`persist: true`). Placeholder lookups are not player-specific — the same value is returned for all players and for
offline players alike, since these reflect global plugin state rather than per-player data.
