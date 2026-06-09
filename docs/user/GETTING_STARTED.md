<!-- generated-by: gsd-doc-writer -->
# Getting Started

This is the first thing to read after installing Disenchantment. You will be up and running in five minutes.

## What This Plugin Does

Disenchantment adds two features to the vanilla anvil — no new blocks, no new commands for players, just the anvil:

1. **Disenchanting** — Place any enchanted item in the first anvil slot and a blank book in the second. The enchantments are stripped from the item and transferred to the book for an XP cost. Useful for salvaging enchantments from gear you want to replace.

2. **Book Splitting (Shattering)** — Place a book with multiple enchantments in the first anvil slot and a blank book in the second. One enchantment is lifted off onto the new book. Repeat until each enchantment is on its own book.

Both features work out of the box with default settings. No configuration is needed to get started.

---

## Quick Start (5 Steps)

1. **Download** the latest `Disenchantment-<version>.jar` from [GitHub Releases](https://github.com/H7KZ/Disenchantment/releases/latest), [SpigotMC](https://www.spigotmc.org/resources/110741), or [Modrinth](https://modrinth.com/plugin/disenchantment).

2. **Drop the JAR** into your server's `plugins/` folder.

3. **Restart the server** (not `/reload` — a full restart is required for the plugin hooks to register correctly).

4. **Check the console** for a startup line like:
   ```
   [Disenchantment] NMS module resolved: v1_21_R5
   ```
   If you see this, the plugin is running. If you see an error, check [INSTALLATION.md](INSTALLATION.md#requirements) — the most common cause is an unsupported Java version (Java 21+ required).

5. **Try it in-game** — open an anvil, put an enchanted item in slot 1 and a blank book in slot 2. A result should appear immediately. See the sections below for the exact steps.

---

## Your First Disenchant

**What you need:** Any enchanted item (a sword, pickaxe, armor piece, etc.) and a blank book (not an enchanted book — just a plain `Book`).

**Steps:**
1. Open an anvil.
2. Place your enchanted item in the **first (left) slot**.
3. Place a **blank book** in the **second (right) slot**.
4. A result appears in the output slot. Hover over it to see the XP cost.
5. Click the result to take it. You receive the item (now unenchanted) and an enchanted book containing the enchantment that was removed.

> **Note:** If the item has multiple enchantments, all of them transfer to the book in a single operation. To get each enchantment on a separate book, use the Shattering feature on the resulting book afterward.

---

## Your First Book Split

**What you need:** An enchanted book with two or more enchantments (e.g. a book with "Sharpness V, Fire Aspect II") and a blank book.

**Steps:**
1. Open an anvil.
2. Place the **multi-enchantment book** in the **first (left) slot**.
3. Place a **blank book** in the **second (right) slot**.
4. A result appears in the output slot. By default, one enchantment splits off.
5. Click the result. You receive a **new book with one enchantment** and the **original book with one fewer enchantment**.
6. Repeat with the original book until you have one enchantment per book.

> **Example:** A book with "Sharpness V, Fire Aspect II, Looting III" takes three shatter operations to fully split into three individual books.

---

## Costs

### XP Cost

The XP cost formula is: `base + (enchantment_level × multiplier)`

The default config values are `base: 10` and `multiply: 0.25`. For a single enchantment, the cost is `base + (level × multiplier)`. When multiple enchantments are processed (disenchanting an item with several enchantments), the multiplier increases with each enchantment so that items with more enchantments cost progressively more.

**Examples with default settings (base=10, multiply=0.25):**

| What you are disenchanting | Calculation | XP Cost |
|---|---|---|
| Item with Sharpness V only | 10 + (5 × 0.25) | 11 levels |
| Item with Sharpness V + Fire Aspect II | 10 + (5 × 0.25) + (2 × 0.50) | 12 levels |
| Book with one enchantment (any shatter) | same formula for that enchantment | varies |

> **Tip:** To make disenchanting free, set `disenchantment.anvil.repair.cost: false` in config, or run `/disenchantment disenchant:repair cost disable`.

### Economy Cost (Optional)

If you have [Vault](https://www.spigotmc.org/resources/34315/) and an economy plugin installed, you can also charge players an in-game currency cost. This is **disabled by default**. See [CONFIG.md — Economy Cost](CONFIG.md#economy-cost-disenchantment) and [INSTALLATION.md — Economy Integration](INSTALLATION.md#optional-economy-integration-vault) to set it up.

When economy is enabled, the cost appears in the player's **action bar** while the result is pending in the anvil.

---

## Common First Questions

**"Players say they can't use the plugin / no result appears for them."**
Anvil usage permissions (`disenchantment.anvil.disenchant` and `disenchantment.anvil.shatter`) default to `true` for all players. If a player can't use the feature, a permission plugin like LuckPerms has likely denied these nodes explicitly. Grant them back:
```
/lp user <player> permission set disenchantment.anvil.all true
```

**"No result appears when I put the items in the anvil."**
Check these in order:
- Is the world disabled? (`disenchantment.disabled-worlds` in config — see [CONFIG.md](CONFIG.md#feature-toggle-and-restrictions))
- Is the feature enabled? (`disenchantment.enabled: true` in config)
- Is the item a book in slot 1? Books cannot be disenchanted (only shattering works with books). Make sure the enchanted item is in slot 1 and the blank book is in slot 2.
- Is the material blocked? (`disenchantment.disabled-materials` in config)

**"An enchantment won't transfer / the book comes out blank."**
The enchantment may be set to `keep` (stays on item) or `delete` (destroyed) state. Check with:
```
/disenchantment disenchant:enchantments
```
If you see a `keep` or `delete` entry for that enchantment, change it:
```
/disenchantment disenchant:enchantments <enchantment_key> enable
```

**"Economy isn't working / players aren't being charged."**
Economy requires both Vault and an economy plugin (such as EssentialsX). Economy is also disabled by default — you must explicitly enable it in config. See [FAQ.md — Economy is enabled but players aren't being charged](FAQ.md#economy-is-enabled-but-players-arent-being-charged) for a step-by-step checklist.

**"The result disappears / nothing happens when I click the output."**
The most common causes:
- The player does not have enough XP levels to pay the cost.
- Economy is enabled and the player cannot afford the currency cost.
- The player lacks the anvil usage permission.

---

## Where to Go Next

| Document | What it covers |
|---|---|
| [INSTALLATION.md](INSTALLATION.md) | Full requirements, Java version, Vault setup |
| [CONFIG.md](CONFIG.md) | Every config option explained in detail |
| [COMMANDS.md](COMMANDS.md) | All `/disenchantment` admin commands |
| [PERMISSIONS.md](PERMISSIONS.md) | Full permission node list |
| [FAQ.md](FAQ.md) | Common issues and known plugin conflicts |
| [HOW_TO.md](HOW_TO.md) | Step-by-step guides for common admin tasks |
