# Commands

All commands use the base command `/disenchantment` (alias: `/disenchant`).

## Table of Contents

- [Enchantment States](#enchantment-states)
- [General](#general)
- [Diagnostic](#diagnostic)
- [Disenchant Configuration](#disenchant-configuration)
- [Disenchant Repair](#disenchant-repair)
- [Disenchant Sound](#disenchant-sound)
- [Shatter Configuration](#shatter-configuration)
- [Shatter Repair](#shatter-repair)
- [Shatter Sound](#shatter-sound)

---

## Enchantment States

When configuring per-enchantment behavior, the following states are available:

| State     | Behavior                                                        |
|-----------|-----------------------------------------------------------------|
| `enable`  | Enchantment can be removed from items.                          |
| `keep`    | Enchantment stays on the item (not transferred).                |
| `delete`  | Enchantment is removed and destroyed (not transferred to book). |
| `disable` | Blocks the entire disenchantment/shatterment process.           |

---

## General

| Command                       | Description                                | Permission                          |
|-------------------------------|--------------------------------------------|-------------------------------------|
| `/disenchantment`             | Show help page 1.                          | `disenchantment.command.use`        |
| `/disenchantment help [page]` | Show help pages 1-8.                       | `disenchantment.command.help`       |
| `/disenchantment diagnostic`  | See [Diagnostic](#diagnostic) section.     | `disenchantment.command.diagnostic` |
| `/disenchantment gui`         | Open the in-game configuration GUI.        | `disenchantment.command.gui`        |
| `/disenchantment status`      | Show if the plugin is enabled or disabled. | `disenchantment.command.status`     |
| `/disenchantment toggle`      | Toggle the plugin on/off.                  | `disenchantment.command.toggle`     |

---

## Diagnostic

Commands for generating diagnostic reports used when reporting bugs.

| Command                                | Description                                                                                                               | Permission                          |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| `/disenchantment diagnostic`           | In-game: click to copy a GitHub-ready code block. Console: prints the standard report.                                   | `disenchantment.command.diagnostic` |
| `/disenchantment diagnostic all`       | Same as above but includes extended details (sounds, worlds, enchantment states, player permissions).                     | `disenchantment.command.diagnostic` |
| `/disenchantment diagnostic save`      | Writes the full report to `plugins/Disenchantment/logs/diagnostic-<timestamp>.txt` and prints the path to the sender.    | `disenchantment.command.diagnostic` |
| `/disenchantment diagnostic log`       | Prints the full report directly to the server console. Useful for RCON users who cannot click in-game.                   | `disenchantment.command.diagnostic` |

> **Tip for bug reports:** Run `/disenchantment diagnostic save` and attach the generated file to your GitHub issue,
> or run `/disenchantment diagnostic` in-game and paste the copied code block directly into the issue body.

---

## Disenchant Configuration

| Command                                                         | Description                        | Permission                                       |
|-----------------------------------------------------------------|------------------------------------|--------------------------------------------------|
| `/disenchantment disenchant:enchantments`                       | Show current enchantment settings. | `disenchantment.command.disenchant.enchantments` |
| `/disenchantment disenchant:enchantments [enchantment] [state]` | Set an enchantment's state.        | `disenchantment.command.disenchant.enchantments` |
| `/disenchantment disenchant:worlds`                             | Show disabled worlds.              | `disenchantment.command.disenchant.worlds`       |
| `/disenchantment disenchant:worlds [world]`                     | Toggle a world on/off.             | `disenchantment.command.disenchant.worlds`       |
| `/disenchantment disenchant:materials`                          | Show disabled materials.           | `disenchantment.command.disenchant.materials`    |
| `/disenchantment disenchant:materials [material]`               | Toggle a material on/off.          | `disenchantment.command.disenchant.materials`    |

---

## Disenchant Repair

| Command                                                     | Description                            | Permission                                 |
|-------------------------------------------------------------|----------------------------------------|--------------------------------------------|
| `/disenchantment disenchant:repair`                         | Show repair cost configuration.        | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair [enable\|disable]`       | Enable/disable repair cost.            | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair reset [enable\|disable]` | Enable/disable repair cost reset to 0. | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair base [int]`              | Set the base repair cost.              | `disenchantment.command.disenchant.repair` |
| `/disenchantment disenchant:repair multiply [float]`        | Set the cost multiplier.               | `disenchantment.command.disenchant.repair` |

---

## Disenchant Sound

| Command                                              | Description                 | Permission                                |
|------------------------------------------------------|-----------------------------|-------------------------------------------|
| `/disenchantment disenchant:sound`                   | Show sound configuration.   | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound [enable\|disable]` | Enable/disable anvil sound. | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound volume [float]`    | Set the sound volume.       | `disenchantment.command.disenchant.sound` |
| `/disenchantment disenchant:sound pitch [float]`     | Set the sound pitch.        | `disenchantment.command.disenchant.sound` |

---

## Shatter Configuration

| Command                                                      | Description                        | Permission                                    |
|--------------------------------------------------------------|------------------------------------|-----------------------------------------------|
| `/disenchantment shatter:enchantments`                       | Show current enchantment settings. | `disenchantment.command.shatter.enchantments` |
| `/disenchantment shatter:enchantments [enchantment] [state]` | Set an enchantment's state.        | `disenchantment.command.shatter.enchantments` |
| `/disenchantment shatter:worlds`                             | Show disabled worlds.              | `disenchantment.command.shatter.worlds`       |
| `/disenchantment shatter:worlds [world]`                     | Toggle a world on/off.             | `disenchantment.command.shatter.worlds`       |

---

## Shatter Repair

| Command                                                  | Description                            | Permission                              |
|----------------------------------------------------------|----------------------------------------|-----------------------------------------|
| `/disenchantment shatter:repair`                         | Show repair cost configuration.        | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair [enable\|disable]`       | Enable/disable repair cost.            | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair reset [enable\|disable]` | Enable/disable repair cost reset to 0. | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair base [int]`              | Set the base repair cost.              | `disenchantment.command.shatter.repair` |
| `/disenchantment shatter:repair multiply [float]`        | Set the cost multiplier.               | `disenchantment.command.shatter.repair` |

---

## Shatter Sound

| Command                                           | Description                 | Permission                             |
|---------------------------------------------------|-----------------------------|----------------------------------------|
| `/disenchantment shatter:sound`                   | Show sound configuration.   | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound [enable\|disable]` | Enable/disable anvil sound. | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound volume [float]`    | Set the sound volume.       | `disenchantment.command.shatter.sound` |
| `/disenchantment shatter:sound pitch [float]`     | Set the sound pitch.        | `disenchantment.command.shatter.sound` |
