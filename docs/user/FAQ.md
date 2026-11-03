<!-- generated-by: gsd-doc-writer -->

# Frequently Asked Questions

Common issues and questions about Disenchantment.

## Table of Contents

- [Why can't non-op players use the plugin?](#why-cant-non-op-players-use-the-plugin)
- [The anvil shows a result but nothing happens when I click](#the-anvil-shows-a-result-but-nothing-happens-when-i-click)
- [The book I get has no enchantments / is blank](#the-book-i-get-has-no-enchantments--is-blank)
- [I got a plain book instead of an enchanted book](#i-got-a-plain-book-instead-of-an-enchanted-book)
- [The blank book is not accepted](#the-blank-book-is-not-accepted)
- [Disenchanting removes the enchantment but doesn't give a book](#disenchanting-removes-the-enchantment-but-doesnt-give-a-book)
- [I want to disable only the shattering feature, not disenchanting](#i-want-to-disable-only-the-shattering-feature-not-disenchanting)
- [Does the plugin work with custom enchantments from other plugins?](#does-the-plugin-work-with-custom-enchantments-from-other-plugins)
- [How do I reload the config without restarting?](#how-do-i-reload-the-config-without-restarting)
- [Can I require both XP and economy cost?](#can-i-require-both-xp-and-economy-cost)
- [What happens to the original item after disenchanting?](#what-happens-to-the-original-item-after-disenchanting)
- [Can I disenchant or shatter a stack of items?](#can-i-disenchant-or-shatter-a-stack-of-items)
- [Can players disenchant in Creative mode?](#can-players-disenchant-in-creative-mode)
- [Why isn't Disenchantment working with EcoEnchants?](#why-isnt-disenchantment-working-with-ecoenchants)
- [Why aren't UberEnchants working with Disenchantment?](#why-arent-uberenchants-working-with-disenchantment)
- [I'm using GeyserMC and there are issues.](#im-using-geysermc-and-there-are-issues-is-this-a-known-problem)
- [How much exactly is charged when `enchantment-costs` economy overrides are set?](#how-much-exactly-is-charged-when-enchantment-costs-economy-overrides-are-set)
- [Economy is enabled but players aren't being charged.](#economy-is-enabled-but-players-arent-being-charged)
- [Do I need Vault for the plugin to work?](#do-i-need-vault-for-the-plugin-to-work)
- [Does Disenchantment support Minecraft 26.1.x?](#does-disenchantment-support-minecraft-261x)
- [Nothing happens at some anvils: special anvils](#nothing-happens-at-some-anvils-special-anvils)
- [The grindstone does not accept my book](#the-grindstone-does-not-accept-my-book)
- [Do players get XP back on the grindstone?](#do-players-get-xp-back-on-the-grindstone)
- [Grindstone transfer does nothing](#grindstone-transfer-does-nothing)

---

### Why can't non-op players use the plugin?

This is usually a permission plugin override rather than a plugin bug.

The anvil usage permissions (`disenchantment.anvil.disenchant` and `disenchantment.anvil.shatter`) default to `true` for
all players. Admin commands (`/disenchantment gui`, `/disenchantment toggle`, etc.) default to `op`.

If non-op players cannot use the anvil features, check whether your permission plugin (such as LuckPerms) has explicitly
set one of these nodes to `false`. An explicit denial overrides the `true` default.

To grant anvil usage manually in LuckPerms:

```
/lp user <player> permission set disenchantment.anvil.all true
```

See [PERMISSIONS.md](PERMISSIONS.md#anvil-usage) for the full list of anvil permissions.

---

### The anvil shows a result but nothing happens when I click

The result appeared in the output slot, which means the plugin recognised the operation as valid. If nothing happens
when the player clicks, the most common causes are:

1. **Not enough XP levels.** The click is rejected and the player is told how many levels they need. With
   `anvil.bypass-too-expensive` enabled this is the *real* cost, not the cheap 1 level shown in the anvil.
2. **Economy cost not met.** If economy is enabled and the player cannot afford the currency cost, the operation is
   cancelled and they receive an insufficient-funds message. Check the action bar for the cost display. If the
   economy plugin refuses the withdrawal (for example a bank limit) the operation is cancelled as well, nothing is
   consumed, and the player is told the payment could not be processed.
3. **Permission denied.** The player's permission plugin may have set `disenchantment.anvil.disenchant` or
   `disenchantment.anvil.shatter` to `false`. Check with your permission plugin.
4. **Cooldown or maintenance mode.** The player is told when they must wait (cooldown) or that the plugin is under
   maintenance. These messages are sent when the result is *clicked*, not while the anvil is being set up. Maintenance
   mode only blocks Disenchantment operations; normal anvil repair and renaming keep working.
5. **Shift-click, number-key or other special clicks.** The result must be taken with a normal left or right click.
   Other click types are blocked (they could duplicate the book) and the player gets a hint saying so.
6. **Two clicks in the same instant.** While an operation is still consuming its blank book (about 2 ticks), further
   clicks on that anvil are ignored.

---

### The book I get has no enchantments / is blank

One or more enchantments have been configured with a `keep` or `delete` state. Check what states are active:

```
/disenchantment disenchant:enchantments
```

If you see `keep` or `delete` for the enchantments you expect to receive, change them to `enable`:

```
/disenchantment disenchant:enchantments <enchantment_key> enable
```

The difference between `keep` and `delete` from the player's perspective is identical — neither transfers the
enchantment to the book. See [CONFIG.md — Enchantment States](CONFIG.md#feature-toggle-and-restrictions) for the full
explanation.

---

### I got a plain book instead of an enchanted book

Every enchantment on the operation failed its success-chance roll (`enchantment-chances`), so there was nothing to put on
the book. The player receives a plain book and a message that the enchantments failed to transfer. The enchantments were
still removed from the source. Raise the chance in config (`disenchantment.enchantment-chances.<enchantment>`) if this is too
harsh.

---

### The blank book is not accepted

Only a plain, unmodified `BOOK` works as the blank book. A book that another plugin has tagged — a custom display name,
lore, custom model data, or any persistent-data tag — is rejected so quest items, vouchers and similar books cannot be
consumed by accident.

---

### Disenchanting removes the enchantment but doesn't give a book

The enchantment is set to `delete` state. It is removed from the item and destroyed without being transferred to the
book. To fix it:

```
/disenchantment disenchant:enchantments <enchantment_key> enable
```

Or in config:

```yaml
disenchantment:
  enchantments-states:
    - "enchantment_key:enable"
```

---

### I want to disable only the shattering feature, not disenchanting

Set `shatterment.enabled: false` in `plugins/Disenchantment/config.yml`:

```yaml
shatterment:
  enabled: false
```

Then run `/disenchantment reload`. Disenchanting will continue to work normally.

> **Note:** The `/disenchantment toggle` command disables the entire plugin (both features). To target only one feature,
> edit the config directly.

---

### Does the plugin work with custom enchantments from other plugins?

**Natively supported plugins** (adapters built into Disenchantment — no extra configuration needed):

| Plugin               | Support added in                    |
|----------------------|-------------------------------------|
| AdvancedEnchantments | all versions                        |
| EcoEnchants          | 6.5.12+ (no patched build required) |
| EnchantsSquared      | all versions                        |
| UberEnchant          | all versions (bug fixed in 6.1.8)   |
| ExcellentEnchants    | MC 1.21+                            |
| Vane                 | MC 1.21+                            |
| Zenchantments        | MC 1.21+                            |

Adapters activate automatically when the supported plugin is detected on the server. No config changes are needed.

**Unsupported plugins:** For custom enchantment plugins not listed above, Disenchantment handles only the vanilla
enchantments on the item. Custom enchantments added by other plugins will be ignored (not transferred, not destroyed —
they remain on the item unchanged). If you need support for a specific plugin, open a GitHub issue.

---

### How do I reload the config without restarting?

```
/disenchantment reload
```

This reloads all settings from `plugins/Disenchantment/config.yml` immediately. You do not need to restart the server
for most changes.

> **Exception:** If you are enabling the Vault economy hook for the first time on a server where Vault was not loaded on
> startup, a full server restart is required.

---

### Can I require both XP and economy cost?

Yes. XP cost and economy cost are independent. Set a non-zero XP cost (via `base` and `multiply` in config) **and**
enable economy (`economy.enabled: true`) at the same time. Players will need both sufficient levels and sufficient
currency to complete an operation.

---

### What happens to the original item after disenchanting?

- All enchantments are removed from the item and transferred to the book (subject to enchantment state overrides —
  see [CONFIG.md](CONFIG.md#feature-toggle-and-restrictions)).
- The item's **durability is unchanged**.
- The item's **prior work penalty** (the repair cost counter that makes future anvil uses more expensive) is preserved
  by default. Set `disenchantment.anvil.repair.reset: true` to reset it to 0 after disenchanting.

---

### Can players disenchant in Creative mode?

Yes. Creative mode players can use the anvil features. However, neither the XP cost nor the economy cost is charged —
consistent with how vanilla Minecraft handles Creative mode (XP is not consumed for any anvil operation in Creative).

---

### Why isn't Disenchantment working with EcoEnchants?

EcoEnchants V13.0.0 is fully supported as of **Disenchantment 6.5.12**. No patched build of EcoEnchants is required.
Update Disenchantment to **6.5.12 or newer** and use any standard EcoEnchants release.

---

### Why aren't UberEnchants working with Disenchantment?

This was a confirmed bug caused by an event priority conflict between Disenchantment and UberEnchants. It was fixed in *
*Disenchantment v6.1.8**. Update to the [latest release](https://github.com/H7KZ/Disenchantment/releases/latest) to
resolve the issue.

If you are on 6.1.8 or newer and still see problems, try adjusting the `event-priorities` settings in `config.yml`.
See [CONFIG.md — Event Priorities](CONFIG.md#event-priorities).

---

### I'm using GeyserMC and there are issues. Is this a known problem?

As of **Disenchantment 6.5.12**, Geyser clients are handled gracefully. The `ClassCastException` that previously crashed
anvil interactions for Bedrock players has been fixed in all NMS modules.

Bedrock players connected via Geyser can use the anvil features without errors. However, the anvil cost display (the
level cost shown in the anvil UI) may appear inaccurate for Bedrock clients. This is because Geyser's proxy
`InventoryView` does not support cost propagation via the `AnvilView` API. This is a known limitation of the Geyser
proxy layer and cannot be worked around on the server side.

---

### How much exactly is charged when `enchantment-costs` economy overrides are set?

Exactly what the action bar displayed: the enchantments that are really transferred to the result book. For
disenchanting, enchantments in the `delete` state are destroyed but not charged for. For shattering, only the enchantments
that were randomly chosen for this split are charged — not every enchantment on the source book.

---

### Economy is enabled but players aren't being charged.

Check the following in order:

1. **Is Vault installed?** Disenchantment requires [Vault](https://www.spigotmc.org/resources/34315/) as a bridge to
   your economy plugin. Place the Vault JAR in your `plugins/` directory.

2. **Is an economy plugin installed?** Vault is only a bridge — it does not provide a currency itself. You also need a
   Vault-compatible economy plugin such
   as [EssentialsX](https://essentialsx.net/), [CMI](https://www.spigotmc.org/resources/3742/),
   or [PlayerPoints](https://www.spigotmc.org/resources/80590/).

3. **Did the server start with both plugins loaded?** Vault must load before Disenchantment. After restarting, check the
   server console for `Economy (Vault): hooked`. If you see `Economy (Vault): not available` or a warning about Vault
   not being found, the plugin or economy provider is missing or failed to load.

4. **Is economy enabled in config?** Economy is disabled by default. Confirm that `disenchantment.economy.enabled: true`
   and/or `shatterment.economy.enabled: true` is set in `plugins/Disenchantment/config.yml` (or use
   `/disenchantment disenchant:economy enabled true`). Save and reload.

5. **Is the player in Creative mode?** Economy costs are intentionally skipped for players in Creative mode, the same as
   XP costs are skipped by vanilla Minecraft.

See [INSTALLATION.md — Economy Integration](INSTALLATION.md#optional-economy-integration-vault) for the full setup
guide.

---

### Do I need Vault for the plugin to work?

No. Vault is entirely optional. Economy integration is disabled by default and has zero effect if Vault is not
installed. All disenchanting and book splitting features work without it.

---

### Does Disenchantment support Minecraft 26.1.x?

Yes. Explicit support entries exist for 26.1, 26.1.1, 26.1.2, and 26.1.3. All are routed to the `v1_21_R5` NMS module.
Any 26.x.x version not yet listed explicitly is also handled by the LATEST fallback, which maps unknown versions 1.21
and newer (including the 26.x.x series) to `v1_21_R5` automatically.

If you are running a very new 26.1.x patch release and experience unexpected behavior, update to the latest
Disenchantment release first, then file a bug report with the output of `/disenchantment diagnostic all`.

---

### Can I disenchant or shatter a stack of items?

No — the operation only runs on a **single** source item (stack size 1). Every enchantable item in vanilla is
unstackable, so this never affects normal play. It only matters if another plugin makes enchanted items stackable: in
that case a stacked source produces no anvil result. Split the stack to one item to disenchant or shatter it.

---

### Nothing happens at some anvils: special anvils

If `special-anvils.mode` is `MARKED` or `BLOCK_BELOW`, only designated anvils disenchant and shatter; every other anvil
still repairs and renames normally. Check `/disenchantment status` for the mode and see
[Restrict disenchanting to special anvils](HOW_TO.md#restrict-disenchanting-to-special-anvils). Common causes:

- The anvil was placed before the mode was switched on (in `MARKED` mode only anvils placed from the special item
  count; place a new one with `/disenchantment give anvil <player>`).
- The player lacks `disenchantment.anvil.special`, so the item was placed as a normal anvil.
- The anvil was placed with `/setblock`, WorldEdit or a schematic: marks are not copied. Use `BLOCK_BELOW` for
  built structures.
- The anvil is opened virtually (a command or another plugin's menu): set `allow-virtual-anvils: true` if that is wanted.

Turn on `special-anvils.hint-message` to tell players when this is the reason. A falling special anvil keeps its mark
when it lands, best effort: if a different plugin changes the falling block, the mark can be lost.

### The grindstone does not accept my book

Enable `grindstone.enabled: true` first. The vanilla grindstone only accepts damageable or enchanted items in its slots, so
a plain book is refused; the plugin places it for you when the **top slot already holds an enchanted item** with something
extractable (not just curses) and you click the bottom slot with the book on the cursor or shift-click the book. Check
that the player has `disenchantment.grindstone.extract`, the world is not disabled in `disenchantment.worlds`, and the
book has no custom name, lore or data (a "tagged" book is never used). Number-key swaps and dragging are not intercepted.

---

### Do players get XP back on the grindstone?

Not for plugin operations. Vanilla refunds experience orbs when you take a grindstone result; extraction and transfer take
over the result click, so they **cost** levels (`grindstone.repair`, `grindstone.transfer.repair`) instead. Normal
grindstone use is unchanged and still refunds XP.

---

### Grindstone transfer does nothing

Transfer needs `grindstone.transfer.enabled: true`, an enchanted item in the **top** slot and a **different** material in
the bottom slot (two items of the same kind are a vanilla repair; books use extraction). Only enchantments the target can
take and that conflict with nothing on it move, and an enchantment the target already has at the same or a higher level
is skipped, so a result can be empty. Run with `logging.level: DEBUG` to see the reason (`GRINDSTONE` lines).
