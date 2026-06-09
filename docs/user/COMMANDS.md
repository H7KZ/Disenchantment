<!-- generated-by: gsd-doc-writer -->

# Commands

Full reference for all `/disenchantment` commands. The alias `/disenchant` works everywhere.

## Table of Contents

- [Enchantment States](#enchantment-states)
- [General Commands](#general-commands)
- [Diagnostic Commands](#diagnostic-commands)
- [Disenchant Configuration](#disenchant-configuration)
- [Disenchant Repair](#disenchant-repair)
- [Disenchant Sound](#disenchant-sound)
- [Disenchant Economy](#disenchant-economy)
- [Shatter Configuration](#shatter-configuration)
- [Shatter Repair](#shatter-repair)
- [Shatter Sound](#shatter-sound)
- [Shatter Economy](#shatter-economy)

---

## Enchantment States

Several commands let you configure per-enchantment behavior. The following states are available:

| State     | Behavior                                                                                      |
|-----------|-----------------------------------------------------------------------------------------------|
| `enable`  | Enchantment can be removed from the item (default behavior).                                  |
| `keep`    | Enchantment stays on the source item and is not transferred to the book.                      |
| `delete`  | Enchantment is removed from the source item but destroyed — not transferred to the book.      |
| `disable` | Blocks the entire disenchant or shatter operation if this enchantment is present on the item. |

---

## General Commands

| Command                       | Description                                               | Permission                      |
|-------------------------------|-----------------------------------------------------------|---------------------------------|
| `/disenchantment`             | Show help page 1.                                         | `disenchantment.command.use`    |
| `/disenchantment help [page]` | Show help pages 1–8.                                      | `disenchantment.command.help`   |
| `/disenchantment gui`         | Open the in-game configuration GUI.                       | `disenchantment.command.gui`    |
| `/disenchantment status`      | Show whether the plugin is currently enabled or disabled. | `disenchantment.command.status` |
| `/disenchantment toggle`      | Toggle the plugin on or off globally.                     | `disenchantment.command.toggle` |
| `/disenchantment reload`      | Reload the configuration from disk.                       | `disenchantment.command.reload` |

---

## Diagnostic Commands

Diagnostic commands generate a report of the current plugin state. This is the first thing to run when filing a bug
report.

| Command                           | Description                                                                                                                           | Permission                          |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| `/disenchantment diagnostic`      | In-game: shows a clickable message that copies a GitHub-ready code block to your clipboard. Console: prints the report as plain text. | `disenchantment.command.diagnostic` |
| `/disenchantment diagnostic all`  | Same as above but includes extended details: sounds, worlds, enchantment states, and player permissions.                              | `disenchantment.command.diagnostic` |
| `/disenchantment diagnostic save` | Writes the full report to `plugins/Disenchantment/logs/diagnostic-<timestamp>.txt` and tells you the path.                            | `disenchantment.command.diagnostic` |
| `/disenchantment diagnostic log`  | Prints the full report to the server console. Useful for RCON users who cannot click in-game.                                         | `disenchantment.command.diagnostic` |

> **Tip:** Run `/disenchantment diagnostic save` and attach the generated file when opening a GitHub issue, or run
`/disenchantment diagnostic` in-game and paste the copied code block directly into the issue body.

---

## Disenchant Configuration

Commands for managing which enchantments, worlds, and materials are active for the disenchant feature.

### Enchantments

| Command                                                         | Description                                                                                                                                                | Permission                                       |
|-----------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| `/disenchantment disenchant:enchantments`                       | List all configured enchantment state overrides.                                                                                                           | `disenchantment.command.disenchant.enchantments` |
| `/disenchantment disenchant:enchantments <enchantment> <state>` | Set the state for a specific enchantment. `<enchantment>` is the Bukkit key (e.g. `sharpness`). `<state>` is one of `enable`, `keep`, `delete`, `disable`. | `disenchantment.command.disenchant.enchantments` |

### Worlds

| Command                                     | Description                                                | Permission                                 |
|---------------------------------------------|------------------------------------------------------------|--------------------------------------------|
| `/disenchantment disenchant:worlds`         | List all worlds where disenchanting is currently disabled. | `disenchantment.command.disenchant.worlds` |
| `/disenchantment disenchant:worlds <world>` | Toggle disenchanting on or off for the named world.        | `disenchantment.command.disenchant.worlds` |

### Materials

| Command                                           | Description                                                                   | Permission                                    |
|---------------------------------------------------|-------------------------------------------------------------------------------|-----------------------------------------------|
| `/disenchantment disenchant:materials`            | List all materials that are currently blocked from disenchanting.             | `disenchantment.command.disenchant.materials` |
| `/disenchantment disenchant:materials <material>` | Toggle disenchanting on or off for the named material (e.g. `DIAMOND_SWORD`). | `disenchantment.command.disenchant.materials` |

---

## Disenchant Repair

Commands for adjusting the XP cost of disenchanting.

| Command                                                     | Description                                                                  | Permission                                 |
|-------------------------------------------------------------|------------------------------------------------------------------------------|--------------------------------------------|
| `/disenchantment disenchant:repair`                         | Show the current repair cost settings.                                       | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair cost <enable\|disable>`  | Enable or disable the XP cost for disenchanting.                             | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair reset <enable\|disable>` | Enable or disable resetting the item's repair cost to 0 after disenchanting. | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair base <value>`            | Set the base XP cost (accepts decimals).                                     | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair multiply <value>`        | Set the per-enchantment-level cost multiplier (accepts decimals).            | `disenchantment.command.disenchant.repair` |

See [CONFIG.md — Repair Cost](CONFIG.md#repair-cost) for a description of the cost formula.

---

## Disenchant Sound

Commands for configuring the sound played when a disenchantment succeeds.

| Command                                           | Description                        | Permission                                |
|---------------------------------------------------|------------------------------------|-------------------------------------------|
| `/disenchantment disenchant:sound`                | Show the current sound settings.   | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound enable`         | Enable the disenchant sound.       | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound disable`        | Disable the disenchant sound.      | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound volume <value>` | Set the sound volume (e.g. `1.0`). | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound pitch <value>`  | Set the sound pitch (e.g. `1.0`).  | `disenchantment.command.disenchant.sound` |

---

## Disenchant Economy

Commands for configuring the in-game currency cost of disenchanting. Requires Vault and an economy plugin.
See [INSTALLATION.md — Economy Integration](INSTALLATION.md#optional-economy-integration-vault).

| Command                                                           | Description                                                                    | Permission                                  |
|-------------------------------------------------------------------|--------------------------------------------------------------------------------|---------------------------------------------|
| `/disenchantment disenchant:economy`                              | Show the current economy settings.                                             | `disenchantment.command.disenchant.economy` |
| `/disenchantment disenchant:economy enabled <true\|false>`        | Enable or disable economy cost for disenchanting.                              | `disenchantment.command.disenchant.economy` |
| `/disenchantment disenchant:economy cost <amount>`                | Set the flat currency cost per disenchant operation.                           | `disenchantment.command.disenchant.economy` |
| `/disenchantment disenchant:economy show-cost <true\|false>`      | Show or hide the cost in the action bar while the player has a pending result. | `disenchantment.command.disenchant.economy` |
| `/disenchantment disenchant:economy charge-message <true\|false>` | Send or suppress the chat confirmation message after a player is charged.      | `disenchantment.command.disenchant.economy` |

---

## Shatter Configuration

Commands for managing which enchantments and worlds are active for the shatter (book splitting) feature.

### Enchantments

| Command                                                      | Description                                                     | Permission                                    |
|--------------------------------------------------------------|-----------------------------------------------------------------|-----------------------------------------------|
| `/disenchantment shatter:enchantments`                       | List all configured enchantment state overrides for shattering. | `disenchantment.command.shatter.enchantments` |
| `/disenchantment shatter:enchantments <enchantment> <state>` | Set the state for a specific enchantment during shattering.     | `disenchantment.command.shatter.enchantments` |

### Worlds

| Command                                  | Description                                             | Permission                              |
|------------------------------------------|---------------------------------------------------------|-----------------------------------------|
| `/disenchantment shatter:worlds`         | List all worlds where shattering is currently disabled. | `disenchantment.command.shatter.worlds` |
| `/disenchantment shatter:worlds <world>` | Toggle shattering on or off for the named world.        | `disenchantment.command.shatter.worlds` |

---

## Shatter Repair

Commands for adjusting the XP cost of shattering. These mirror the disenchant repair commands.

| Command                                                  | Description                                                               | Permission                              |
|----------------------------------------------------------|---------------------------------------------------------------------------|-----------------------------------------|
| `/disenchantment shatter:repair`                         | Show the current repair cost settings.                                    | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair cost <enable\|disable>`  | Enable or disable the XP cost for shattering.                             | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair reset <enable\|disable>` | Enable or disable resetting the book's repair cost to 0 after shattering. | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair base <value>`            | Set the base XP cost.                                                     | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair multiply <value>`        | Set the per-enchantment-level cost multiplier.                            | `disenchantment.command.shatter.repair` |

---

## Shatter Sound

Commands for configuring the sound played when a shatter succeeds.

| Command                                        | Description                      | Permission                             |
|------------------------------------------------|----------------------------------|----------------------------------------|
| `/disenchantment shatter:sound`                | Show the current sound settings. | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound enable`         | Enable the shatter sound.        | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound disable`        | Disable the shatter sound.       | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound volume <value>` | Set the sound volume.            | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound pitch <value>`  | Set the sound pitch.             | `disenchantment.command.shatter.sound` |

---

## Shatter Economy

Commands for configuring the in-game currency cost of shattering. These mirror the disenchant economy commands.

| Command                                                        | Description                                                                    | Permission                               |
|----------------------------------------------------------------|--------------------------------------------------------------------------------|------------------------------------------|
| `/disenchantment shatter:economy`                              | Show the current economy settings.                                             | `disenchantment.command.shatter.economy` |
| `/disenchantment shatter:economy enabled <true\|false>`        | Enable or disable economy cost for shattering.                                 | `disenchantment.command.shatter.economy` |
| `/disenchantment shatter:economy cost <amount>`                | Set the flat currency cost per shatter operation.                              | `disenchantment.command.shatter.economy` |
| `/disenchantment shatter:economy show-cost <true\|false>`      | Show or hide the cost in the action bar while the player has a pending result. | `disenchantment.command.shatter.economy` |
| `/disenchantment shatter:economy charge-message <true\|false>` | Send or suppress the chat confirmation message after a player is charged.      | `disenchantment.command.shatter.economy` |
