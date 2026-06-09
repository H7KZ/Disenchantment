<!-- generated-by: gsd-doc-writer -->

# Installation

Step-by-step guide for installing Disenchantment on your Minecraft server.

## Table of Contents

- [Requirements](#requirements)
- [Download](#download)
- [Installation Steps](#installation-steps)
- [Optional: Economy Integration (Vault)](#optional-economy-integration-vault)
- [First Run Tips](#first-run-tips)

---

## Requirements

| Requirement     | Version                          |
|-----------------|----------------------------------|
| Java            | 21 or newer                      |
| Server software | Spigot, Paper, or Folia          |
| Minecraft       | 1.18 – 1.21.x (including 26.1.x) |

Disenchantment does not require any additional plugins to function. Vault is optional and only needed for economy cost
integration.

---

## Download

Get the latest `Disenchantment-<version>.jar` from any of these sources:

- **GitHub Releases
  ** — [github.com/H7KZ/Disenchantment/releases/latest](https://github.com/H7KZ/Disenchantment/releases/latest)
- **SpigotMC** — [spigotmc.org/resources/110741](https://www.spigotmc.org/resources/110741)
- **Modrinth** — [modrinth.com/plugin/disenchantment](https://modrinth.com/plugin/disenchantment)

Always download the latest release. Do not download from unofficial sources.

---

## Installation Steps

1. **Download** the `Disenchantment-<version>.jar` file from one of the sources above.

2. **Place** the JAR in your server's `plugins/` directory.

3. **Restart** the server (not `/reload` — a full restart ensures all hooks are registered correctly).

4. **Verify** the plugin loaded by checking the console for a line like:
   ```
   [Disenchantment] NMS module resolved: v1_21_R5
   ```
   If you see `INCOMPATIBLE` or an error at startup, check that your Minecraft version is supported and that you are
   running Java 21+.

5. **Configure** the plugin by editing `plugins/Disenchantment/config.yml`, or use the in-game GUI with
   `/disenchantment gui` (requires operator or the `disenchantment.command.gui` permission). See [CONFIG.md](CONFIG.md)
   for a full reference of every option.

---

## Optional: Economy Integration (Vault)

Disenchantment can optionally charge players an in-game currency cost per operation. This requires two additional
plugins:

### Step 1: Install Vault

Vault is a bridge plugin that connects Disenchantment to your economy plugin. Install it like any other plugin:

1. Download [Vault](https://www.spigotmc.org/resources/34315/) (the standard release — not VaultUnlocked, though
   VaultUnlocked is also supported).
2. Place the Vault JAR in your `plugins/` directory.

### Step 2: Install a compatible economy plugin

Vault itself does not provide a currency. You also need one of the following (or any other Vault-compatible economy
plugin):

- [EssentialsX](https://essentialsx.net/) — the most common choice
- [CMI](https://www.spigotmc.org/resources/3742/)
- [PlayerPoints](https://www.spigotmc.org/resources/80590/)

### Step 3: Enable economy in config

Economy is **disabled by default**. After installing Vault and your economy plugin, edit
`plugins/Disenchantment/config.yml` and set the relevant options:

```yaml
disenchantment:
  economy:
    enabled: true
    cost: 100.0

shatterment:
  economy:
    enabled: true
    cost: 100.0
```

Restart the server. On startup you should see `Economy (Vault): hooked` in the console. If you see
`Economy (Vault): not available`, check that both Vault and your economy plugin are installed and loading without
errors.

For a full description of all economy settings see
the [Economy Integration section in CONFIG.md](CONFIG.md#economy-integration).

---

## First Run Tips

- The default configuration enables both disenchanting and book splitting for all players in all worlds. No changes are
  required to get started.
- Anvil usage permissions (`disenchantment.anvil.disenchant` and `disenchantment.anvil.shatter`) default to `true` for
  all players.
- Admin commands (`/disenchantment gui`, `/disenchantment toggle`, etc.) default to `op`. Grant the relevant
  `disenchantment.command.*` permissions via LuckPerms or another permission plugin if you want non-op admins to manage
  the plugin.
- If you use a custom enchantment plugin (AdvancedEnchantments, EcoEnchants, EnchantsSquared, UberEnchant,
  ExcellentEnchants, Vane, or Zenchantments), no additional configuration is needed — the adapter activates
  automatically when the supported plugin is detected on the server.
- If something is not working as expected, run `/disenchantment diagnostic` in-game to copy a diagnostic report, or
  `/disenchantment diagnostic save` to write it to a file. See [FAQ.md](FAQ.md) for common issues.
