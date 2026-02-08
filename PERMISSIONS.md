# Permissions

All permissions for the Disenchantment plugin. Permissions defaulting to `true` are granted to all players. Permissions defaulting to `op` are only available to server operators unless explicitly granted via a permissions plugin like [LuckPerms](https://luckperms.net/).

## Table of Contents

- [Anvil Usage](#anvil-usage)
- [Commands](#commands)
- [GUI](#gui)
- [Wildcard Permissions](#wildcard-permissions)

---

## Anvil Usage

These permissions control whether players can use the disenchanting and book splitting features at the anvil. They default to `true` so all players can use the core functionality out of the box.

| Permission | Description | Default |
|---|---|---|
| `disenchantment.anvil.all` | Allows disenchanting items and splitting books. | `true` |
| `disenchantment.anvil.disenchant` | Allows disenchanting items. | `true` |
| `disenchantment.anvil.shatter` | Allows splitting enchantments from books. | `true` |

---

## Commands

These permissions control access to admin commands. They default to `op`.

| Permission | Description | Default |
|---|---|---|
| `disenchantment.command.use` | Access to the base `/disenchantment` command. | `op` |
| `disenchantment.command.all` | Access to every command. | `op` |
| `disenchantment.command.help` | Access to the help command. | `op` |
| `disenchantment.command.gui` | Access to the GUI command. | `op` |
| `disenchantment.command.status` | Access to the status command. | `op` |
| `disenchantment.command.toggle` | Access to the toggle command. | `op` |
| `disenchantment.command.diagnostic` | Access to the diagnostic command. | `op` |
| `disenchantment.command.disenchant.enchantments` | Manage disenchant enchantment settings. | `op` |
| `disenchantment.command.disenchant.worlds` | Manage disenchant world restrictions. | `op` |
| `disenchantment.command.disenchant.materials` | Manage disenchant material restrictions. | `op` |
| `disenchantment.command.disenchant.repair` | Manage disenchant repair cost settings. | `op` |
| `disenchantment.command.disenchant.sound` | Manage disenchant sound settings. | `op` |
| `disenchantment.command.shatter.enchantments` | Manage shatter enchantment settings. | `op` |
| `disenchantment.command.shatter.worlds` | Manage shatter world restrictions. | `op` |
| `disenchantment.command.shatter.repair` | Manage shatter repair cost settings. | `op` |
| `disenchantment.command.shatter.sound` | Manage shatter sound settings. | `op` |

---

## GUI

These permissions control access to the in-game configuration GUI. They default to `op`.

| Permission | Description | Default |
|---|---|---|
| `disenchantment.gui` | Open the configuration GUI. | `op` |
| `disenchantment.gui.all` | Access all GUI settings. | `op` |
| `disenchantment.gui.status` | View plugin status in the GUI. | `op` |
| `disenchantment.gui.worlds` | Manage world restrictions in the GUI. | `op` |
| `disenchantment.gui.enchantments` | Manage enchantment settings in the GUI. | `op` |
| `disenchantment.gui.materials` | Manage material restrictions in the GUI. | `op` |
| `disenchantment.gui.disenchant.repair` | Manage disenchant repair settings in the GUI. | `op` |
| `disenchantment.gui.disenchant.sound` | Manage disenchant sound settings in the GUI. | `op` |
| `disenchantment.gui.shatter.repair` | Manage shatter repair settings in the GUI. | `op` |
| `disenchantment.gui.shatter.sound` | Manage shatter sound settings in the GUI. | `op` |

---

## Wildcard Permissions

| Permission | Description | Default |
|---|---|---|
| `disenchantment.all` | Grants all permissions. | `op` |
