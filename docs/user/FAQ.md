<!-- generated-by: gsd-doc-writer -->
# Frequently Asked Questions

Common issues and questions about Disenchantment.

## Table of Contents

- [Why can't non-op players use the plugin?](#why-cant-non-op-players-use-the-plugin)
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
