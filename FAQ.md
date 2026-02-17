# Frequently Asked Questions

This FAQ addresses common issues and questions regarding the Disenchantment plugin.

## Table of Contents

- [Why can't non-op players use the plugin?](#why-cant-players-who-arent-server-operators-non-ops-use-the-plugin)
- [Why isn't Disenchantment working with EcoEnchants?](#why-isnt-disenchantment-working-with-ecoenchants)
- [Why aren't UberEnchants working with Disenchantment?](#why-arent-uberenchants-working-with-disenchantment)
- [I'm using GeyserMC and the plugin has issues.](#im-using-geysermc-and-the-plugin-has-issues-is-this-a-known-problem)

---

### Why can't players who aren't server operators (non-ops) use the plugin?

This is intended behavior. The anvil usage permissions (`disenchantment.anvil.disenchant` and
`disenchantment.anvil.shatter`) default to `true` for all players. However, admin commands require `op` by default.

If anvil usage is not working for non-op players, ensure no other permission plugin is overriding these defaults. We
recommend using [LuckPerms](https://luckperms.net/) to manage permissions.

See the full list of permission nodes in [PERMISSIONS.md](PERMISSIONS.md).

---

### Why isn't Disenchantment working with EcoEnchants?

This is a known compatibility issue caused by how **EcoEnchants** handles anvil events, which conflicts with *
*Disenchantment**. The enchanted book appears in the anvil for a moment before vanishing.

To fix this, you need to update **Disenchantment** and use a modified version of **EcoEnchants**.

**Easiest Solution (Recommended)**

1. Update the **Disenchantment** plugin to version **`6.2.2`** or newer.
2. Download the pre-compiled, fixed version of **EcoEnchants** (v12.24.0) from
   the [Disenchantment v6.2.2 release page](https://github.com/H7KZ/Disenchantment/releases/tag/v6.2.2).

You must use both the updated Disenchantment plugin and the provided EcoEnchants `.jar` file for the fix to work.

**Alternative for Advanced Users**

If you're comfortable with compiling Java projects, you can build the plugin yourself:

1. Make sure your **Disenchantment** plugin is updated to **`v6.2.2`** or newer.
2. Clone or download the **EcoEnchants** source code.
3. Incorporate the changes from [EcoEnchants Pull Request #417](https://github.com/Auxilor/EcoEnchants/pull/417).
4. Compile the plugin to create your own `.jar` file.

---

### Why aren't UberEnchants working with Disenchantment?

This was a confirmed bug caused by a conflict in event priorities between the Disenchantment and UberEnchants plugins.

This issue was fixed in version **`v6.1.8`**. Please update to
the [latest release](https://github.com/H7KZ/Disenchantment/releases/latest) to resolve the problem.

---

### I'm using GeyserMC and the plugin has issues. Is this a known problem?

Yes, servers running GeyserMC can sometimes experience issues with plugins that modify anvil mechanics, especially for
players connecting from Bedrock Edition. This is often caused by Geyser itself rather than a bug in Disenchantment.

While a guaranteed fix is not available, you can try installing one of the following plugins designed to patch
anvil-related issues on Geyser servers:

- [Geyser-Anvil-Fix](https://github.com/ssquadteam/Geyser-Anvil-Fix)
- [CustomAnvilGUI](https://www.spigotmc.org/resources/customanvilgui.116411/)
- [Geyser Recipe Fix](https://modrinth.com/plugin/geyser-recipe-fix)

**Note:** As reported by some users, these fixes may not work in all cases.
