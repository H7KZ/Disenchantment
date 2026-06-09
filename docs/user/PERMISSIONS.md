<!-- generated-by: gsd-doc-writer -->

# Permissions

All permission nodes for Disenchantment, their defaults, and what they control.

Permissions defaulting to `true` are granted to every player automatically. Permissions defaulting to `op` require the
player to be a server operator unless explicitly granted via a permission plugin such
as [LuckPerms](https://luckperms.net/).

## Table of Contents

- [Anvil Usage](#anvil-usage)
- [Commands](#commands)
- [GUI](#gui)
- [Wildcard Permission](#wildcard-permission)

---

## Anvil Usage

These permissions control whether a player can use the disenchanting and book splitting features at the anvil. They
default to `true` so all players can use the core features out of the box.

| Permission                        | Description                                            | Default |
|-----------------------------------|--------------------------------------------------------|---------|
| `disenchantment.anvil.all`        | Allows disenchanting items and splitting books.        | `true`  |
| `disenchantment.anvil.disenchant` | Allows disenchanting items at the anvil.               | `true`  |
| `disenchantment.anvil.shatter`    | Allows splitting enchantments from books at the anvil. | `true`  |

> **Note:** If non-op players cannot use the anvil features, check whether a permission plugin (e.g. LuckPerms) has
> explicitly denied these nodes. See [FAQ.md](FAQ.md#why-cant-players-who-arent-server-operators-non-ops-use-the-plugin).

---

## Commands

These permissions control access to `/disenchantment` commands. All default to `op`.

### General

| Permission                          | Description                                                | Default |
|-------------------------------------|------------------------------------------------------------|---------|
| `disenchantment.command.use`        | Access to the base `/disenchantment` command (shows help). | `op`    |
| `disenchantment.command.all`        | Access to every command.                                   | `op`    |
| `disenchantment.command.help`       | Access to the help command.                                | `op`    |
| `disenchantment.command.gui`        | Access to the GUI command.                                 | `op`    |
| `disenchantment.command.status`     | Access to the status command.                              | `op`    |
| `disenchantment.command.toggle`     | Access to the toggle command.                              | `op`    |
| `disenchantment.command.diagnostic` | Access to the diagnostic command and all its subcommands.  | `op`    |
| `disenchantment.command.reload`     | Access to the reload command.                              | `op`    |

### Disenchant Configuration

| Permission                                       | Description                                               | Default |
|--------------------------------------------------|-----------------------------------------------------------|---------|
| `disenchantment.command.disenchant.enchantments` | Manage per-enchantment state overrides for disenchanting. | `op`    |
| `disenchantment.command.disenchant.worlds`       | Manage world restrictions for disenchanting.              | `op`    |
| `disenchantment.command.disenchant.materials`    | Manage material restrictions for disenchanting.           | `op`    |
| `disenchantment.command.disenchant.repair`       | Manage the XP repair cost settings for disenchanting.     | `op`    |
| `disenchantment.command.disenchant.sound`        | Manage the sound settings for disenchanting.              | `op`    |
| `disenchantment.command.disenchant.economy`      | Manage the economy cost settings for disenchanting.       | `op`    |

### Shatter Configuration

| Permission                                    | Description                                            | Default |
|-----------------------------------------------|--------------------------------------------------------|---------|
| `disenchantment.command.shatter.enchantments` | Manage per-enchantment state overrides for shattering. | `op`    |
| `disenchantment.command.shatter.worlds`       | Manage world restrictions for shattering.              | `op`    |
| `disenchantment.command.shatter.repair`       | Manage the XP repair cost settings for shattering.     | `op`    |
| `disenchantment.command.shatter.sound`        | Manage the sound settings for shattering.              | `op`    |
| `disenchantment.command.shatter.economy`      | Manage the economy cost settings for shattering.       | `op`    |

---

## GUI

These permissions control access to panels within the in-game configuration GUI (`/disenchantment gui`). All default to
`op`.

| Permission                              | Description                                    | Default |
|-----------------------------------------|------------------------------------------------|---------|
| `disenchantment.gui`                    | Open the configuration GUI.                    | `op`    |
| `disenchantment.gui.all`                | Access all panels within the GUI.              | `op`    |
| `disenchantment.gui.status`             | View and toggle the plugin status panel.       | `op`    |
| `disenchantment.gui.worlds`             | Manage world restrictions in the GUI.          | `op`    |
| `disenchantment.gui.enchantments`       | Manage per-enchantment settings in the GUI.    | `op`    |
| `disenchantment.gui.materials`          | Manage material restrictions in the GUI.       | `op`    |
| `disenchantment.gui.disenchant.repair`  | Manage disenchant repair settings in the GUI.  | `op`    |
| `disenchantment.gui.disenchant.sound`   | Manage disenchant sound settings in the GUI.   | `op`    |
| `disenchantment.gui.disenchant.economy` | Manage disenchant economy settings in the GUI. | `op`    |
| `disenchantment.gui.shatter.repair`     | Manage shatter repair settings in the GUI.     | `op`    |
| `disenchantment.gui.shatter.sound`      | Manage shatter sound settings in the GUI.      | `op`    |
| `disenchantment.gui.shatter.economy`    | Manage shatter economy settings in the GUI.    | `op`    |

---

## Wildcard Permission

| Permission           | Description                                     | Default |
|----------------------|-------------------------------------------------|---------|
| `disenchantment.all` | Grants every Disenchantment permission at once. | `op`    |

Use this node in LuckPerms or another permission plugin to give an admin group full access without listing individual
permissions.
