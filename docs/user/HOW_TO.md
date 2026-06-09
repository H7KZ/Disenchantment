<!-- generated-by: gsd-doc-writer -->
# How-To Guide

A practical cookbook of common admin tasks. Each section gives you the exact commands or config changes needed to get the result you want.

## Table of Contents

- [Restrict disenchanting to specific worlds](#restrict-disenchanting-to-specific-worlds)
- [Restrict shattering to specific worlds](#restrict-shattering-to-specific-worlds)
- [Block a specific enchantment from being disenchanted](#block-a-specific-enchantment-from-being-disenchanted)
- [Make an enchantment stay on the item (not transfer to book)](#make-an-enchantment-stay-on-the-item-not-transfer-to-book)
- [Destroy an enchantment on disenchant instead of transferring it](#destroy-an-enchantment-on-disenchant-instead-of-transferring-it)
- [Restrict specific materials from being disenchanted](#restrict-specific-materials-from-being-disenchanted)
- [Set up economy costs](#set-up-economy-costs)
- [Make disenchanting free (XP-wise)](#make-disenchanting-free-xp-wise)
- [Adjust costs per enchantment](#adjust-costs-per-enchantment)
- [Disable only shattering, keep disenchanting](#disable-only-shattering-keep-disenchanting)
- [Fix event priority conflicts with other plugins](#fix-event-priority-conflicts-with-other-plugins)
- [Grant or revoke permissions with LuckPerms](#grant-or-revoke-permissions-with-luckperms)
- [Open and use the in-game GUI](#open-and-use-the-in-game-gui)
- [Generate a diagnostic report for a bug report](#generate-a-diagnostic-report-for-a-bug-report)
- [Reload the config without restarting](#reload-the-config-without-restarting)

---

## Restrict disenchanting to specific worlds

You can block the disenchant feature in one or more worlds while leaving it active everywhere else.

**Via command (no restart needed):**
```
/disenchantment disenchant:worlds <worldname>
```
Running this command toggles the world on or off. Run it again to re-enable. World names are case-sensitive.

**Via config (`plugins/Disenchantment/config.yml`):**
```yaml
disenchantment:
  disabled-worlds:
    - world_nether
    - world_the_end
```
After editing, run `/disenchantment reload` or restart the server.

> **Check what is disabled:** `/disenchantment disenchant:worlds` lists all currently disabled worlds.

---

## Restrict shattering to specific worlds

The same approach as above, but for book splitting.

**Via command:**
```
/disenchantment shatter:worlds <worldname>
```

**Via config:**
```yaml
shatterment:
  disabled-worlds:
    - world_nether
    - minigames
```

> **Check what is disabled:** `/disenchantment shatter:worlds` lists all currently disabled worlds.

---

## Block a specific enchantment from being disenchanted

Setting an enchantment to `disable` state causes the entire disenchant operation to fail if that enchantment is on the item. The player sees no result in the anvil output slot.

**Via command:**
```
/disenchantment disenchant:enchantments <enchantment_key> disable
```

For example, to block Mending from ever being disenchanted:
```
/disenchantment disenchant:enchantments mending disable
```

Enchantment keys are the Bukkit/vanilla key (e.g. `sharpness`, `mending`, `protection`, `fire_aspect`). Tab-completion is available in-game.

**Via config:**
```yaml
disenchantment:
  enchantments-states:
    - "mending:disable"
    - "curse_of_binding:disable"
```

> **List current overrides:** `/disenchantment disenchant:enchantments` shows all configured states.

> **Undo it:** `/disenchantment disenchant:enchantments mending enable` (or remove the entry from config and reload).

---

## Make an enchantment stay on the item (not transfer to book)

Setting an enchantment to `keep` state means it is **removed from the item** during disenchanting but **not added to the book**. The player gets an item without that enchantment and a book without that enchantment either. The enchantment is effectively invisible to the operation.

Use this if you want to prevent players from extracting a specific enchantment (like Mending) but still let them disenchant the rest of the item's enchantments.

**Via command:**
```
/disenchantment disenchant:enchantments mending keep
```

**Via config:**
```yaml
disenchantment:
  enchantments-states:
    - "mending:keep"
```

---

## Destroy an enchantment on disenchant instead of transferring it

Setting an enchantment to `delete` state means it is **removed from the item and destroyed** — it never appears on the book.

**Via command:**
```
/disenchantment disenchant:enchantments <enchantment_key> delete
```

**Via config:**
```yaml
disenchantment:
  enchantments-states:
    - "mending:delete"
```

> **Difference from `keep`:** Both `keep` and `delete` prevent the enchantment from reaching the book. The behavior is identical from the player's perspective — the enchantment disappears. The distinction matters if you are building logic on the custom events API.

---

## Restrict specific materials from being disenchanted

You can prevent certain item types from being used in the disenchant feature entirely.

**Via command:**
```
/disenchantment disenchant:materials <material>
```

For example, to prevent Netherite Swords from being disenchanted:
```
/disenchantment disenchant:materials NETHERITE_SWORD
```

Running the command again toggles the material back on. Material names are the Bukkit material key in uppercase (e.g. `DIAMOND_CHESTPLATE`, `ELYTRA`, `TRIDENT`).

**Via config:**
```yaml
disenchantment:
  disabled-materials:
    - NETHERITE_SWORD
    - ELYTRA
```

> **Check what is disabled:** `/disenchantment disenchant:materials` lists all currently blocked materials.

> **Note:** The shattering feature does not have a `disabled-materials` option — it only applies to enchanted books.

---

## Set up economy costs

Disenchantment can optionally charge players in-game currency per operation. This requires [Vault](https://www.spigotmc.org/resources/34315/) and a Vault-compatible economy plugin (EssentialsX, CMI, PlayerPoints, etc.).

**Step 1: Install Vault and an economy plugin.**
Place both JARs in your `plugins/` folder.

**Step 2: Enable economy in config.**

```yaml
disenchantment:
  economy:
    enabled: true
    cost: 500.0
    show-cost: true      # shows cost in action bar while result is pending
    charge-message: true # sends a chat message to the player after they are charged

shatterment:
  economy:
    enabled: true
    cost: 250.0
```

**Step 3: Restart the server** (or run `/disenchantment reload` if Vault was already running before).

**Step 4: Verify the hook.**
Check the console for `Economy (Vault): hooked`. If you see `Economy (Vault): not available`, Vault or the economy plugin failed to load — check for errors in the console.

**Via commands (no file editing required):**
```
/disenchantment disenchant:economy enabled true
/disenchantment disenchant:economy cost 500
/disenchantment disenchant:economy show-cost true
/disenchantment disenchant:economy charge-message true
```

And for shattering:
```
/disenchantment shatter:economy enabled true
/disenchantment shatter:economy cost 250
```

> **Verify the full setup:** Run `/disenchantment diagnostic` after enabling economy. The report includes the economy hook status.

---

## Make disenchanting free (XP-wise)

**Option 1: Disable the XP cost entirely**

Via command:
```
/disenchantment disenchant:repair cost disable
```

Via config:
```yaml
disenchantment:
  anvil:
    repair:
      cost: false
```

**Option 2: Set the cost to zero mathematically**

```
/disenchantment disenchant:repair base 0
/disenchantment disenchant:repair multiply 0
```

Or in config:
```yaml
disenchantment:
  anvil:
    repair:
      base: 0
      multiply: 0
```

With both values at zero, the formula produces 0 for all enchantments. Either approach has the same effect.

> To make **shattering** free as well, use the `shatter:repair` commands or `shatterment.anvil.repair` config keys instead.

---

## Adjust costs per enchantment

You can set a fixed XP cost for specific enchantments, overriding the formula entirely for those enchantments.

**Via config:**
```yaml
disenchantment:
  anvil:
    repair:
      base: 10
      multiply: 0.25
      enchantment-costs:
        mending: 30      # mending always costs exactly 30 levels
        silk_touch: 20   # silk touch always costs exactly 20 levels
```

When a per-enchantment cost override is present, the formula (`base + level × multiplier`) is bypassed for that enchantment and the fixed value is used instead. Other enchantments on the same item still use the formula.

> **Manage via GUI:** The in-game GUI (`/disenchantment gui`) has a dedicated panel for enchantment cost overrides.

---

## Disable only shattering, keep disenchanting

```yaml
shatterment:
  enabled: false
```

Or via command:
```
/disenchantment toggle
```

> **Note:** `/disenchantment toggle` disables the entire plugin (both features). To disable only shattering, editing the config is the correct approach. After editing, run `/disenchantment reload`.

---

## Fix event priority conflicts with other plugins

If another plugin (such as UberEnchants, CMI, or other anvil-modifying plugins) conflicts with Disenchantment, you may see incorrect results, missing outputs, or broken anvil behavior.

**What to try:**

Adjust the `event-priorities` settings in `config.yml`. Lower priorities let other plugins process anvil events first; higher priorities let Disenchantment act last.

```yaml
event-priorities:
  disenchant-click: HIGHEST  # when Disenchantment handles the click
  disenchant: HIGHEST        # when Disenchantment prepares the result
  shatter-click: HIGHEST
  shatter: HIGHEST
```

**Guideline for choosing a priority:**

| Scenario | Recommended setting |
|---|---|
| Disenchantment conflicts with another plugin that also handles `PrepareAnvilEvent` | Try `HIGH` or `NORMAL` so the other plugin runs after |
| Another plugin cancels anvil events before Disenchantment sees them | Try `LOW` or `LOWEST` so Disenchantment runs before |
| Default (no conflict) | `HIGHEST` |

Available values from lowest to highest: `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`. Do not use `MONITOR` — it is read-only and Disenchantment cannot act on the event from that priority level.

After editing, run `/disenchantment reload`.

> See also [FAQ.md — UberEnchants](FAQ.md#why-arent-uberenchants-working-with-disenchantment) for the specific fix that resolved the known UberEnchants conflict.

---

## Grant or revoke permissions with LuckPerms

Anvil usage permissions default to `true` for all players. Admin command permissions default to `op`.

**Grant anvil usage to a specific player (if it was denied):**
```
/lp user <player> permission set disenchantment.anvil.all true
```

**Grant only disenchanting (not shattering):**
```
/lp user <player> permission set disenchantment.anvil.disenchant true
```

**Revoke anvil usage from a player:**
```
/lp user <player> permission set disenchantment.anvil.all false
```

**Revoke anvil usage from everyone (deny at default group level):**
```
/lp group default permission set disenchantment.anvil.all false
```

**Grant all admin commands to a staff group:**
```
/lp group staff permission set disenchantment.command.all true
```

**Grant all Disenchantment permissions at once:**
```
/lp user <player> permission set disenchantment.all true
```

> See [PERMISSIONS.md](PERMISSIONS.md) for the complete list of every permission node and its default.

---

## Open and use the in-game GUI

```
/disenchantment gui
```

The GUI provides a visual interface for all configuration options — no config file editing required.

**What each section does:**

| GUI panel | What you can change |
|---|---|
| Status | Enable/disable the entire plugin or individual features |
| Worlds | Enable or disable the plugin per world |
| Enchantments | Set per-enchantment states (enable, keep, delete, disable) |
| Materials | Enable or disable disenchanting per material type |
| Disenchant Repair | Base cost, multiplier, per-enchantment cost overrides, cost reset on/off |
| Disenchant Sound | Enable/disable the anvil sound, set volume and pitch |
| Disenchant Economy | Enable economy cost, set amount, toggle action bar and charge message |
| Shatter Repair | Same as Disenchant Repair but for book splitting |
| Shatter Sound | Same as Disenchant Sound but for book splitting |
| Shatter Economy | Same as Disenchant Economy but for book splitting |

All changes made through the GUI are saved immediately to `plugins/Disenchantment/config.yml`.

> **Permission required:** `disenchantment.command.gui` (default: `op`).

---

## Generate a diagnostic report for a bug report

If something is not working and you want to open a GitHub issue, include a diagnostic report.

**In-game (easiest):**
```
/disenchantment diagnostic
```
Click the output message to copy a GitHub-ready code block to your clipboard. Paste it directly into the issue body.

**For extended details (worlds, sounds, enchantment states, permissions):**
```
/disenchantment diagnostic all
```

**Save to a file (attach to issue):**
```
/disenchantment diagnostic save
```
This writes the report to `plugins/Disenchantment/logs/diagnostic-<timestamp>.txt`. Attach that file to your GitHub issue.

**From console / RCON:**
```
/disenchantment diagnostic log
```
Prints the full report as plain text to the server console.

---

## Reload the config without restarting

After editing `plugins/Disenchantment/config.yml`:
```
/disenchantment reload
```

This reloads all config values from disk. You do not need to restart the server for most changes. The exception is the economy Vault hook — if you are enabling economy for the first time on a server where Vault was not previously loaded, a restart is required.

> **Permission required:** `disenchantment.command.reload` (default: `op`).
