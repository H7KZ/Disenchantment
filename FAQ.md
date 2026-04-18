# Frequently Asked Questions

This FAQ addresses common issues and questions regarding the Disenchantment plugin.

## Table of Contents

- [Why can't non-op players use the plugin?](#why-cant-players-who-arent-server-operators-non-ops-use-the-plugin)
- [Why isn't Disenchantment working with EcoEnchants?](#why-isnt-disenchantment-working-with-ecoenchants)
- [Why aren't UberEnchants working with Disenchantment?](#why-arent-uberenchants-working-with-disenchantment)
- [I'm using GeyserMC and the plugin has issues.](#im-using-geysermc-and-the-plugin-has-issues-is-this-a-known-problem)
- [Economy is enabled but players aren't being charged.](#economy-is-enabled-but-players-arent-being-charged)
- [Do I need Vault for the plugin to work?](#do-i-need-vault-for-the-plugin-to-work)
- [Does Disenchantment support Minecraft 26.1.x?](#does-disenchantment-support-minecraft-261x)

---

### Why can't players who aren't server operators (non-ops) use the plugin?

This is intended behavior. The anvil usage permissions (`disenchantment.anvil.disenchant` and
`disenchantment.anvil.shatter`) default to `true` for all players. However, admin commands require `op` by default.

If anvil usage is not working for non-op players, ensure no other permission plugin is overriding these defaults. We
recommend using [LuckPerms](https://luckperms.net/) to manage permissions.

See the full list of permission nodes in [PERMISSIONS.md](PERMISSIONS.md).

---

### Why isn't Disenchantment working with EcoEnchants?

EcoEnchants V13.0.0 is fully supported natively as of **Disenchantment 6.4.1**. No patched build of EcoEnchants is
required. Simply update Disenchantment to version **`6.4.1`** or newer and use any standard release of EcoEnchants.

---

### Why aren't UberEnchants working with Disenchantment?

This was a confirmed bug caused by a conflict in event priorities between the Disenchantment and UberEnchants plugins.

This issue was fixed in version **`v6.1.8`**. Please update to
the [latest release](https://github.com/H7KZ/Disenchantment/releases/latest) to resolve the problem.

---

### Economy is enabled but players aren't being charged.

Check the following in order:

1. **Is Vault installed?** Disenchantment requires [Vault](https://www.spigotmc.org/resources/34315/) as a bridge to
   your economy plugin. Install the Vault JAR in your `plugins/` folder.

2. **Is an economy plugin installed?** Vault itself does not provide an economy — it is a bridge. You also need a
   Vault-compatible economy plugin such as [EssentialsX](https://essentialsx.net/),
   [CMI](https://www.spigotmc.org/resources/3742/), or [PlayerPoints](https://www.spigotmc.org/resources/80590/).

3. **Did the server start after both plugins were installed?** Vault must load before Disenchantment. Check your
   server console for the line `Economy (Vault): hooked` on startup. If you see `Economy (Vault): not available`
   or `Economy is enabled in config but Vault/economy plugin not found!`, Vault or your economy plugin is missing.

4. **Is `economy.enabled` set to `true`?** Economy is disabled by default. Set `disenchantment.economy.enabled: true`
   and/or `shatterment.economy.enabled: true` in `config.yml` and restart (or reload) the server.

5. **Is the player in Creative mode?** Economy costs are intentionally skipped for players in Creative mode,
   the same as XP costs.

---

### Do I need Vault for the plugin to work?

No. Vault is entirely optional. The economy integration is disabled by default and has zero effect if Vault is not
installed. All standard disenchanting and book splitting features work without it.

---

### I'm using GeyserMC and the plugin has issues. Is this a known problem?

As of **Disenchantment 6.4.1+**, Geyser clients are handled gracefully. The `ClassCastException` that previously
crashed anvil interactions for Bedrock players has been fixed in all v1_21_R* NMS modules.

Bedrock clients connected via Geyser will be able to use the anvil without errors. However, the anvil cost display
(repair cost shown in the UI) may be inaccurate for Bedrock clients, as Geyser's proxy `InventoryView` does not
support cost propagation via the `AnvilView` API. This is a known limitation of the Geyser proxy layer and cannot be
worked around on the server side.

---

### Does Disenchantment support Minecraft 26.1.x?

Yes. Minecraft 26.1.1 and later 26.1.x patch releases are supported. Explicit enum entries exist for 26.1, 26.1.1,
26.1.2, and 26.1.3 in `MinecraftVersion.java`, and all are routed to the `v1_21_R5` NMS module (the same module used
by LATEST). Any 26.x.x version that is not yet listed explicitly is also handled by the LATEST fallback.
