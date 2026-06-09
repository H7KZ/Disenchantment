<!-- generated-by: gsd-doc-writer -->
# Frequently Asked Questions

Common issues and questions about Disenchantment.

## Table of Contents

- [Why can't non-op players use the plugin?](#why-cant-non-op-players-use-the-plugin)
- [The anvil shows a result but nothing happens when I click](#the-anvil-shows-a-result-but-nothing-happens-when-i-click)
- [The book I get has no enchantments / is blank](#the-book-i-get-has-no-enchantments--is-blank)
- [Disenchanting removes the enchantment but doesn't give a book](#disenchanting-removes-the-enchantment-but-doesnt-give-a-book)
- [I want to disable only the shattering feature, not disenchanting](#i-want-to-disable-only-the-shattering-feature-not-disenchanting)
- [Does the plugin work with custom enchantments from other plugins?](#does-the-plugin-work-with-custom-enchantments-from-other-plugins)
- [How do I reload the config without restarting?](#how-do-i-reload-the-config-without-restarting)
- [Can I require both XP and economy cost?](#can-i-require-both-xp-and-economy-cost)
- [What happens to the original item after disenchanting?](#what-happens-to-the-original-item-after-disenchanting)
- [Can players disenchant in Creative mode?](#can-players-disenchant-in-creative-mode)
- [Why isn't Disenchantment working with EcoEnchants?](#why-isnt-disenchantment-working-with-ecoenchants)
- [Why aren't UberEnchants working with Disenchantment?](#why-arent-uberenchants-working-with-disenchantment)
- [I'm using GeyserMC and there are issues.](#im-using-geysermc-and-there-are-issues-is-this-a-known-problem)
- [Economy is enabled but players aren't being charged.](#economy-is-enabled-but-players-arent-being-charged)
- [Do I need Vault for the plugin to work?](#do-i-need-vault-for-the-plugin-to-work)
- [Does Disenchantment support Minecraft 26.1.x?](#does-disenchantment-support-minecraft-261x)

---

### Why can't non-op players use the plugin?

This is usually a permission plugin override rather than a plugin bug.

The anvil usage permissions (`disenchantment.anvil.disenchant` and `disenchantment.anvil.shatter`) default to `true` for all players. Admin commands (`/disenchantment gui`, `/disenchantment toggle`, etc.) default to `op`.

If non-op players cannot use the anvil features, check whether your permission plugin (such as LuckPerms) has explicitly set one of these nodes to `false`. An explicit denial overrides the `true` default.

To grant anvil usage manually in LuckPerms:
```
/lp user <player> permission set disenchantment.anvil.all true
```

See [PERMISSIONS.md](PERMISSIONS.md#anvil-usage) for the full list of anvil permissions.

---

### The anvil shows a result but nothing happens when I click

The result appeared in the output slot, which means the plugin recognised the operation as valid. If nothing happens when the player clicks, the most common causes are:

1. **Not enough XP levels.** The anvil shows a cost — if the player does not have that many levels, the click is silently rejected (same as vanilla anvil behaviour).
2. **Economy cost not met.** If economy is enabled and the player cannot afford the currency cost, the operation is cancelled and they receive an insufficient-funds message. Check the action bar for the cost display.
3. **Permission denied.** The player's permission plugin may have set `disenchantment.anvil.disenchant` or `disenchantment.anvil.shatter` to `false`. Check with your permission plugin.

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

The difference between `keep` and `delete` from the player's perspective is identical — neither transfers the enchantment to the book. See [CONFIG.md — Enchantment States](CONFIG.md#feature-toggle-and-restrictions) for the full explanation.

---

### Disenchanting removes the enchantment but doesn't give a book

The enchantment is set to `delete` state. It is removed from the item and destroyed without being transferred to the book. To fix it:

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

> **Note:** The `/disenchantment toggle` command disables the entire plugin (both features). To target only one feature, edit the config directly.

---

### Does the plugin work with custom enchantments from other plugins?

**Natively supported plugins** (adapters built into Disenchantment — no extra configuration needed):

| Plugin | Support added in |
|---|---|
| AdvancedEnchantments | all versions |
| EcoEnchants | 6.4.0+ (no patched build required) |
| EnchantsSquared | all versions |
| UberEnchant | all versions (bug fixed in 6.1.8) |
| ExcellentEnchants | MC 1.21+ |
| Vane | MC 1.21+ |
| Zenchantments | MC 1.21+ |

Adapters activate automatically when the supported plugin is detected on the server. No config changes are needed.

**Unsupported plugins:** For custom enchantment plugins not listed above, Disenchantment handles only the vanilla enchantments on the item. Custom enchantments added by other plugins will be ignored (not transferred, not destroyed — they remain on the item unchanged). If you need support for a specific plugin, open a GitHub issue.

---

### How do I reload the config without restarting?

```
/disenchantment reload
```

This reloads all settings from `plugins/Disenchantment/config.yml` immediately. You do not need to restart the server for most changes.

> **Exception:** If you are enabling the Vault economy hook for the first time on a server where Vault was not loaded on startup, a full server restart is required.

---

### Can I require both XP and economy cost?

Yes. XP cost and economy cost are independent. Set a non-zero XP cost (via `base` and `multiply` in config) **and** enable economy (`economy.enabled: true`) at the same time. Players will need both sufficient levels and sufficient currency to complete an operation.

---

### What happens to the original item after disenchanting?

- All enchantments are removed from the item and transferred to the book (subject to enchantment state overrides — see [CONFIG.md](CONFIG.md#feature-toggle-and-restrictions)).
- The item's **durability is unchanged**.
- The item's **prior work penalty** (the repair cost counter that makes future anvil uses more expensive) is preserved by default. Set `disenchantment.anvil.repair.reset: true` to reset it to 0 after disenchanting.

---

### Can players disenchant in Creative mode?

Yes. Creative mode players can use the anvil features. However, neither the XP cost nor the economy cost is charged — consistent with how vanilla Minecraft handles Creative mode (XP is not consumed for any anvil operation in Creative).

---

### Why isn't Disenchantment working with EcoEnchants?

EcoEnchants V13.0.0 is fully supported as of **Disenchantment 6.4.0**. No patched build of EcoEnchants is required. Update Disenchantment to **6.4.0 or newer** and use any standard EcoEnchants release.

---

### Why aren't UberEnchants working with Disenchantment?

This was a confirmed bug caused by an event priority conflict between Disenchantment and UberEnchants. It was fixed in **Disenchantment v6.1.8**. Update to the [latest release](https://github.com/H7KZ/Disenchantment/releases/latest) to resolve the issue.

If you are on 6.1.8 or newer and still see problems, try adjusting the `event-priorities` settings in `config.yml`. See [CONFIG.md — Event Priorities](CONFIG.md#event-priorities).

---

### I'm using GeyserMC and there are issues. Is this a known problem?

As of **Disenchantment 6.4.0**, Geyser clients are handled gracefully. The `ClassCastException` that previously crashed anvil interactions for Bedrock players has been fixed in all NMS modules.

Bedrock players connected via Geyser can use the anvil features without errors. However, the anvil cost display (the level cost shown in the anvil UI) may appear inaccurate for Bedrock clients. This is because Geyser's proxy `InventoryView` does not support cost propagation via the `AnvilView` API. This is a known limitation of the Geyser proxy layer and cannot be worked around on the server side.

---

### Economy is enabled but players aren't being charged.

Check the following in order:

1. **Is Vault installed?** Disenchantment requires [Vault](https://www.spigotmc.org/resources/34315/) as a bridge to your economy plugin. Place the Vault JAR in your `plugins/` directory.

2. **Is an economy plugin installed?** Vault is only a bridge — it does not provide a currency itself. You also need a Vault-compatible economy plugin such as [EssentialsX](https://essentialsx.net/), [CMI](https://www.spigotmc.org/resources/3742/), or [PlayerPoints](https://www.spigotmc.org/resources/80590/).

3. **Did the server start with both plugins loaded?** Vault must load before Disenchantment. After restarting, check the server console for `Economy (Vault): hooked`. If you see `Economy (Vault): not available` or a warning about Vault not being found, the plugin or economy provider is missing or failed to load.

4. **Is economy enabled in config?** Economy is disabled by default. Confirm that `disenchantment.economy.enabled: true` and/or `shatterment.economy.enabled: true` is set in `plugins/Disenchantment/config.yml` (or use `/disenchantment disenchant:economy enabled true`). Save and reload.

5. **Is the player in Creative mode?** Economy costs are intentionally skipped for players in Creative mode, the same as XP costs are skipped by vanilla Minecraft.

See [INSTALLATION.md — Economy Integration](INSTALLATION.md#optional-economy-integration-vault) for the full setup guide.

---

### Do I need Vault for the plugin to work?

No. Vault is entirely optional. Economy integration is disabled by default and has zero effect if Vault is not installed. All disenchanting and book splitting features work without it.

---

### Does Disenchantment support Minecraft 26.1.x?

Yes. Explicit support entries exist for 26.1, 26.1.1, 26.1.2, and 26.1.3. All are routed to the `v1_21_R5` NMS module. Any 26.x.x version not yet listed explicitly is also handled by the LATEST fallback, which maps unknown versions 1.21 and newer (including the 26.x.x series) to `v1_21_R5` automatically.

If you are running a very new 26.1.x patch release and experience unexpected behavior, update to the latest Disenchantment release first, then file a bug report with the output of `/disenchantment diagnostic all`.
