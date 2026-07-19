package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.config;
import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Provides typed access to the plugin's configuration values.
 * Offers getters and setters for all configurable options including
 * disenchantment settings, shatterment settings, event priorities,
 * anvil sound/repair parameters, and world/material/enchantment states.
 */
public class Config {
    private static boolean batchMode = false;

    /**
     * Starts a batch configuration update. While batch mode is active, calls to
     * {@code save()} are suppressed. Use {@link #commitBatch()} to persist all
     * pending changes in a single write, avoiding repeated disk I/O.
     */
    public static void beginBatch() {
        batchMode = true;
    }

    /**
     * Ends a batch configuration update and persists all pending changes to disk.
     * Must be paired with a preceding {@link #beginBatch()} call.
     */
    public static void commitBatch() {
        batchMode = false;
        plugin.saveConfig();
    }

    private static void save() {
        if (!batchMode) plugin.saveConfig();
    }

    /**
     * Abstraction over the feature-specific configuration methods, used by commands
     * and GUIs that operate on either disenchantment or shatterment without knowing which.
     */
    public interface FeatureConfig {
        boolean isEnabled();

        List<World> getDisabledWorlds();

        boolean setDisabledWorlds(List<World> worlds);

        HashMap<String, EnchantmentStateType> getEnchantmentStates();

        boolean setEnchantmentStates(HashMap<String, EnchantmentStateType> enchantmentStates);
    }

    /**
     * Returns a {@link FeatureConfig} view delegating to the configuration
     * section for the specified feature.
     *
     * @param feature the anvil feature to get configuration for
     * @return a feature-specific configuration view
     */
    public static FeatureConfig forFeature(AnvilFeature feature) {
        return switch (feature) {
            case DISENCHANTMENT -> new FeatureConfig() {
                public boolean isEnabled() {
                    return Disenchantment.isEnabled();
                }

                public List<World> getDisabledWorlds() {
                    return Disenchantment.getDisabledWorlds();
                }

                public boolean setDisabledWorlds(List<World> worlds) {
                    return Disenchantment.setDisabledWorlds(worlds);
                }

                public HashMap<String, EnchantmentStateType> getEnchantmentStates() {
                    return Disenchantment.getEnchantmentStates();
                }

                public boolean setEnchantmentStates(HashMap<String, EnchantmentStateType> enchantmentStates) {
                    return Disenchantment.setEnchantmentStates(enchantmentStates);
                }
            };
            case SHATTERMENT -> new FeatureConfig() {
                public boolean isEnabled() {
                    return Shatterment.isEnabled();
                }

                public List<World> getDisabledWorlds() {
                    return Shatterment.getDisabledWorlds();
                }

                public boolean setDisabledWorlds(List<World> worlds) {
                    return Shatterment.setDisabledWorlds(worlds);
                }

                public HashMap<String, EnchantmentStateType> getEnchantmentStates() {
                    return Shatterment.getEnchantmentStates();
                }

                public boolean setEnchantmentStates(HashMap<String, EnchantmentStateType> enchantmentStates) {
                    return Shatterment.setEnchantmentStates(enchantmentStates);
                }
            };
        };
    }

    /**
     * Clears the in-memory enchantment state caches for both disenchantment and shatterment.
     * Must be called after a config reload so that subsequent reads pick up fresh values.
     */
    public static void invalidateCaches() {
        Disenchantment.ENCHANTMENT_STATES_CACHE = null;
        Shatterment.ENCHANTMENT_STATES_CACHE = null;
    }

    /**
     * Checks whether the plugin is globally enabled.
     *
     * @return {@code true} if the plugin is enabled
     */
    public static boolean isPluginEnabled() {
        return config.getBoolean(ConfigKeys.ENABLED.getKey());
    }

    /**
     * Sets the global plugin enabled state and persists the change.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     * @return {@code true} if the persisted value matches the requested state
     */
    public static boolean setPluginEnabled(boolean enabled) {
        config.set(ConfigKeys.ENABLED.getKey(), enabled);
        save();

        return isPluginEnabled() == enabled;
    }

    /**
     * Gets the list of available locale identifiers.
     *
     * @return list of locale codes
     */
    public static List<String> getLocales() {
        return config.getStringList(ConfigKeys.LOCALES.getKey());
    }

    /**
     * Gets the currently configured locale identifier.
     *
     * @return the active locale code
     */
    public static String getLocale() {
        return config.getString(ConfigKeys.LOCALE.getKey());
    }

    /**
     * Gets the configured enchantment groups, shared by both disenchantment and shatterment.
     * Each entry maps a group name to the list of enchantment keys (lowercase) it contains.
     *
     * @return map of group name to its member enchantment keys
     */
    public static Map<String, List<String>> getEnchantmentGroups() {
        var section = config.getConfigurationSection(ConfigKeys.ENCHANTMENT_GROUPS.getKey());
        if (section == null) return new HashMap<>();
        Map<String, List<String>> groups = new HashMap<>();
        for (String key : section.getKeys(false)) {
            groups.put(key.toLowerCase(), section.getStringList(key).stream().map(String::toLowerCase).toList());
        }
        return groups;
    }

    /**
     * Provides access to configurable Bukkit event priority settings for each
     * anvil event handler. Falls back to {@link EventPriority#HIGHEST} if the
     * configured value is invalid.
     */
    public static class EventPriorities {
        /**
         * Gets the event priority for the disenchant prepare-anvil event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getDisenchantEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_DISENCHANT.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }

        /**
         * Gets the event priority for the disenchant inventory-click event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getDisenchantClickEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_DISENCHANT_CLICK.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }

        /**
         * Gets the event priority for the shatter prepare-anvil event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getShatterEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_SHATTER.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }

        /**
         * Gets the event priority for the shatter inventory-click event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getShatterClickEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_SHATTER_CLICK.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }
    }

    /**
     * Logging configuration for the plugin. Controls console verbosity and whether
     * crash reports are persisted to disk under {@code plugins/Disenchantment/logs/}.
     */
    public static class Logging {
        /**
         * Gets the configured log verbosity level.
         * Falls back to {@link LogLevelType#INFO} if the configured value is invalid.
         *
         * @return the active {@link LogLevelType}
         */
        public static LogLevelType getLevel() {
            try {
                return LogLevelType.valueOf(config.getString(ConfigKeys.LOGGING_LEVEL.getKey(), "INFO").toUpperCase());
            } catch (IllegalArgumentException e) {
                return LogLevelType.INFO;
            }
        }

        /**
         * Returns whether crash reports should be written to disk in addition to the console.
         *
         * @return {@code true} if file-based crash reporting is enabled
         */
        public static boolean isSaveReportsEnabled() {
            return config.getBoolean(ConfigKeys.LOGGING_SAVE_REPORTS.getKey(), true);
        }

        public static boolean isOperationsEnabled() {
            return config.getBoolean(ConfigKeys.LOGGING_OPERATIONS.getKey(), false);
        }

        public static String getDiscordWebhook() {
            return config.getString(ConfigKeys.LOGGING_DISCORD_WEBHOOK.getKey(), "");
        }
    }

    /**
     * Configuration for the optional LuckPerms-based XP cost multiplier.
     */
    public static class CostMultiplier {
        /**
         * Returns whether LuckPerms meta should be queried for a per-player cost multiplier.
         * When {@code false}, LuckPerms is never queried and only the permission-node
         * discount fallback applies.
         *
         * @return {@code true} if LuckPerms-based cost multiplier is enabled
         */
        public static boolean isLuckPermsEnabled() {
            return config.getBoolean(ConfigKeys.COST_MULTIPLIER_LUCKPERMS.getKey(), false);
        }
    }

    /**
     * Configuration for the optional per-player anvil-operation cooldown.
     */
    public static class Cooldown {
        /**
         * Gets the configured cooldown duration in seconds. 0 disables cooldowns entirely.
         *
         * @return the cooldown duration in seconds
         */
        public static int getSeconds() {
            return config.getInt(ConfigKeys.COOLDOWN_SECONDS.getKey(), 0);
        }

        /**
         * Returns whether cooldowns should be delegated to CMI when it is installed.
         *
         * @return {@code true} if the CMI cooldown delegate is enabled
         */
        public static boolean isUseCmiEnabled() {
            return config.getBoolean(ConfigKeys.COOLDOWN_USE_CMI.getKey(), false);
        }

        /**
         * Returns whether cooldowns should be delegated to EssentialsX when it is installed.
         *
         * @return {@code true} if the EssentialsX cooldown delegate is enabled
         */
        public static boolean isUseEssentialsEnabled() {
            return config.getBoolean(ConfigKeys.COOLDOWN_USE_ESSENTIALS.getKey(), false);
        }
    }

    /**
     * Configuration section for disenchantment (extracting enchantments from items to books).
     * Includes settings for enabled state, disabled worlds/materials, enchantment states,
     * and anvil sound/repair parameters.
     */
    public static class Disenchantment {
        private static HashMap<String, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;

        /**
         * Checks whether disenchantment is enabled.
         *
         * @return {@code true} if disenchantment is enabled
         */
        public static boolean isEnabled() {
            return config.getBoolean(ConfigKeys.DISENCHANTMENT_ENABLED.getKey());
        }

        /**
         * Sets whether disenchantment is enabled and persists the change.
         *
         * @param enabled {@code true} to enable, {@code false} to disable
         * @return {@code true} if the persisted value matches the requested state
         */
        public static boolean setEnabled(boolean enabled) {
            config.set(ConfigKeys.DISENCHANTMENT_ENABLED.getKey(), enabled);
            save();

            return isEnabled() == enabled;
        }

        /**
         * Gets the list of worlds where disenchantment is disabled.
         *
         * @return list of disabled {@link World} instances
         */
        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).filter(java.util.Objects::nonNull).toList());
        }

        /**
         * Sets the list of worlds where disenchantment is disabled and persists the change.
         *
         * @param worlds the worlds to disable disenchantment in
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            save();

            return getDisabledWorlds().equals(worlds);
        }

        /**
         * Gets the list of materials that cannot be disenchanted.
         *
         * @return list of disabled {@link Material} types
         */
        public static List<Material> getDisabledMaterials() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey()).stream().map(Material::getMaterial).filter(java.util.Objects::nonNull).toList());
        }

        /**
         * Sets the list of materials that cannot be disenchanted and persists the change.
         *
         * @param materials the materials to disable
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey(), materials.stream().map(Material::name).toList());
            save();

            return getDisabledMaterials().equals(materials);
        }

        /**
         * Gets the per-enchantment state overrides for disenchantment. Results are cached.
         *
         * @return map of enchantment key to its {@link EnchantmentStateType}
         */
        public static HashMap<String, EnchantmentStateType> getEnchantmentStates() {
            if (ENCHANTMENT_STATES_CACHE != null) return ENCHANTMENT_STATES_CACHE;

            List<String> list = config.getStringList(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES.getKey());
            HashMap<String, EnchantmentStateType> enchantmentStates = new HashMap<>();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                String key;
                EnchantmentStateType state;

                if (split.length == 2) {
                    key = split[0];
                    state = EnchantmentStateType.getStateByName(split[1]);
                } else if (split.length == 3) {
                    // "minecraft:sharpness:keep" — namespace prefix, drop it
                    key = split[1];
                    state = EnchantmentStateType.getStateByName(split[2]);
                } else {
                    continue;
                }

                enchantmentStates.put(key, state);
            }

            ENCHANTMENT_STATES_CACHE = enchantmentStates;

            return enchantmentStates;
        }

        /**
         * Sets the per-enchantment state overrides for disenchantment and persists the change.
         *
         * @param enchantmentStates map of enchantment key to its desired state
         * @return {@code true} if the persisted value matches the requested map
         */
        public static boolean setEnchantmentStates(HashMap<String, EnchantmentStateType> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<String, EnchantmentStateType> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
            }

            config.set(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES.getKey(), list);
            save();

            ENCHANTMENT_STATES_CACHE = new HashMap<>(enchantmentStates);

            return getEnchantmentStates().equals(enchantmentStates);
        }

        /**
         * Gets the per-enchantment success chance overrides for disenchantment.
         * Enchantments not present in this map always succeed (chance 1.0).
         *
         * @return map of enchantment key to its success chance (0.0-1.0)
         */
        public static Map<String, Double> getEnchantmentChances() {
            var section = config.getConfigurationSection(ConfigKeys.DISENCHANTMENT_ENCHANTMENT_CHANCES.getKey());
            if (section == null) return new HashMap<>();
            Map<String, Double> chances = new HashMap<>();
            for (String key : section.getKeys(false)) {
                chances.put(key.toLowerCase(), section.getDouble(key));
            }
            return chances;
        }

        /**
         * Gets the success chance for a specific enchantment key. Missing keys default to 1.0 (always succeeds).
         *
         * @param key the enchantment key (namespaced or short form)
         * @return the configured chance, clamped to [0.0, 1.0]
         */
        public static double getEnchantmentChance(String key) {
            if (key == null) return 1.0;
            String shortKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
            Map<String, Double> chances = getEnchantmentChances();
            Double chance = chances.get(shortKey.toLowerCase());
            if (chance == null) chance = chances.get(key.toLowerCase()); // also match full namespaced key
            if (chance == null) return 1.0;
            return Math.max(0.0, Math.min(1.0, chance));
        }

        /**
         * Sets the success chance for a specific enchantment key and persists the change.
         *
         * @param key    the enchantment key
         * @param chance the desired chance (0.0-1.0)
         */
        public static void setEnchantmentChance(String key, double chance) {
            config.set(ConfigKeys.DISENCHANTMENT_ENCHANTMENT_CHANCES.getKey() + "." + key.toLowerCase(), Math.max(0.0, Math.min(1.0, chance)));
            save();
        }

        /**
         * Economy settings for disenchantment operations.
         */
        public static class Economy {
            /**
             * Checks whether economy integration is enabled for disenchantment.
             *
             * @return {@code true} if economy cost is active
             */
            public static boolean isEnabled() {
                return config.getBoolean(ConfigKeys.DISENCHANTMENT_ECONOMY_ENABLED.getKey());
            }

            /**
             * Sets whether economy integration is enabled for disenchantment.
             *
             * @param enabled {@code true} to enable
             * @return {@code true} if the persisted value matches
             */
            public static boolean setEnabled(boolean enabled) {
                config.set(ConfigKeys.DISENCHANTMENT_ECONOMY_ENABLED.getKey(), enabled);
                save();

                return isEnabled() == enabled;
            }

            /**
             * Gets the economy cost per disenchantment operation.
             *
             * @return the configured cost
             */
            public static double getCost() {
                return config.getDouble(ConfigKeys.DISENCHANTMENT_ECONOMY_COST.getKey());
            }

            /**
             * Sets the economy cost per disenchantment operation.
             *
             * @param cost the desired cost
             * @return {@code true} if the persisted value matches
             */
            public static boolean setCost(double cost) {
                config.set(ConfigKeys.DISENCHANTMENT_ECONOMY_COST.getKey(), cost);
                save();

                return getCost() == cost;
            }

            /**
             * Checks whether the cost message is shown to the player before the operation.
             *
             * @return {@code true} if the cost display is enabled
             */
            public static boolean isShowCostEnabled() {
                return config.getBoolean(ConfigKeys.DISENCHANTMENT_ECONOMY_SHOW_COST.getKey(), true);
            }

            /**
             * Sets whether the cost message is shown to the player before the operation.
             *
             * @param enabled {@code true} to show cost
             * @return {@code true} if the persisted value matches
             */
            public static boolean setShowCostEnabled(boolean enabled) {
                config.set(ConfigKeys.DISENCHANTMENT_ECONOMY_SHOW_COST.getKey(), enabled);
                save();

                return isShowCostEnabled() == enabled;
            }

            /**
             * Checks whether a charge confirmation message is sent after the operation.
             *
             * @return {@code true} if the charge message is enabled
             */
            public static boolean isChargeMessageEnabled() {
                return config.getBoolean(ConfigKeys.DISENCHANTMENT_ECONOMY_CHARGE_MESSAGE.getKey(), true);
            }

            /**
             * Sets whether a charge confirmation message is sent after the operation.
             *
             * @param enabled {@code true} to send the message
             * @return {@code true} if the persisted value matches
             */
            public static boolean setChargeMessageEnabled(boolean enabled) {
                config.set(ConfigKeys.DISENCHANTMENT_ECONOMY_CHARGE_MESSAGE.getKey(), enabled);
                save();

                return isChargeMessageEnabled() == enabled;
            }
        }

        /**
         * Anvil-related settings for disenchantment operations.
         */
        public static class Anvil {
            /**
             * Checks whether the "Too Expensive!" anvil screen bypass is enabled for disenchantment.
             *
             * @return {@code true} if the bypass is enabled
             */
            public static boolean isBypassTooExpensiveEnabled() {
                return config.getBoolean(ConfigKeys.DISENCHANTMENT_ANVIL_BYPASS_TOO_EXPENSIVE.getKey());
            }

            /**
             * Sound settings for the disenchantment anvil interaction.
             */
            public static class Sound {
                /**
                 * Checks whether the disenchantment anvil sound is enabled.
                 *
                 * @return {@code true} if the sound is enabled
                 */
                public static boolean isEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED.getKey());
                }

                /**
                 * Sets whether the disenchantment anvil sound is enabled.
                 *
                 * @param enabled {@code true} to enable the sound
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setEnabled(boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED.getKey(), enabled);
                    save();

                    return isEnabled() == enabled;
                }

                /**
                 * Gets the disenchantment anvil sound volume.
                 *
                 * @return the configured volume
                 */
                public static Double getVolume() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey());
                }

                /**
                 * Sets the disenchantment anvil sound volume and persists the change.
                 *
                 * @param volume the desired volume
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setVolume(double volume) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey(), volume);
                    save();

                    return getVolume().equals(volume);
                }

                /**
                 * Gets the disenchantment anvil sound pitch.
                 *
                 * @return the configured pitch
                 */
                public static Double getPitch() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey());
                }

                /**
                 * Sets the disenchantment anvil sound pitch and persists the change.
                 *
                 * @param pitch the desired pitch
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setPitch(double pitch) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey(), pitch);
                    save();

                    return getPitch().equals(pitch);
                }
            }

            /**
             * Repair cost settings for disenchantment anvil operations.
             */
            public static class Repair {
                /**
                 * Checks whether repair cost reset is enabled for disenchantment.
                 *
                 * @return {@code true} if repair cost reset is enabled
                 */
                public static boolean isResetEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED.getKey());
                }

                /**
                 * Sets whether repair cost reset is enabled for disenchantment.
                 *
                 * @param enabled {@code true} to enable reset
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setResetEnabled(boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED.getKey(), enabled);
                    save();

                    return isResetEnabled() == enabled;
                }

                /**
                 * Checks whether repair cost is enabled for disenchantment.
                 *
                 * @return {@code true} if repair cost is enabled
                 */
                public static boolean isCostEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED.getKey());
                }

                /**
                 * Sets whether repair cost is enabled for disenchantment.
                 *
                 * @param enabled {@code true} to enable cost
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setCostEnabled(boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED.getKey(), enabled);
                    save();

                    return isCostEnabled() == enabled;
                }

                /**
                 * Gets the base repair cost for disenchantment.
                 *
                 * @return the configured base cost
                 */
                public static Double getBaseCost() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE.getKey());
                }

                /**
                 * Sets the base repair cost for disenchantment and persists the change.
                 *
                 * @param cost the desired base cost
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setBaseCost(double cost) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE.getKey(), cost);
                    save();

                    return getBaseCost().equals(cost);
                }

                /**
                 * Gets the repair cost multiplier for disenchantment.
                 *
                 * @return the configured cost multiplier
                 */
                public static Double getCostMultiplier() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER.getKey());
                }

                /**
                 * Sets the repair cost multiplier for disenchantment and persists the change.
                 *
                 * @param multiplier the desired cost multiplier
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setCostMultiplier(double multiplier) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER.getKey(), multiplier);
                    save();

                    return getCostMultiplier().equals(multiplier);
                }

                /**
                 * Gets the per-enchantment XP cost overrides for disenchantment.
                 *
                 * @return map of enchantment key to its XP cost override
                 */
                public static Map<String, Integer> getEnchantmentCosts() {
                    var section = config.getConfigurationSection(ConfigKeys.DISENCHANTMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS.getKey());
                    if (section == null) return new HashMap<>();
                    Map<String, Integer> costs = new HashMap<>();
                    for (String key : section.getKeys(false)) {
                        // Two supported shapes: "sharpness: 5" (XP-only, legacy) or
                        // "mending: { xp: 10, economy: 500 }" (XP + economy override).
                        var sub = section.getConfigurationSection(key);
                        if (sub != null) {
                            if (sub.contains("xp")) costs.put(key, sub.getInt("xp"));
                        } else {
                            costs.put(key, section.getInt(key));
                        }
                    }
                    return costs;
                }

                /**
                 * Gets the per-enchantment economy cost overrides for disenchantment, parsed from
                 * the map-shaped entries of {@code enchantment-costs} (e.g. {@code mending: { economy: 500 }}).
                 * Enchantments without a map-shaped entry are absent from this map.
                 *
                 * @return map of enchantment key to its economy cost override
                 */
                public static Map<String, Double> getEnchantmentEconomyCosts() {
                    var section = config.getConfigurationSection(ConfigKeys.DISENCHANTMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS.getKey());
                    if (section == null) return new HashMap<>();
                    Map<String, Double> costs = new HashMap<>();
                    for (String key : section.getKeys(false)) {
                        var sub = section.getConfigurationSection(key);
                        if (sub != null && sub.contains("economy")) {
                            costs.put(key, sub.getDouble("economy"));
                        }
                    }
                    return costs;
                }

                /**
                 * Gets the maximum XP cost cap for disenchantment. {@code -1} means no cap (default).
                 *
                 * @return the configured max cost, or -1 if uncapped
                 */
                public static int getMaxCost() {
                    return config.getInt(ConfigKeys.DISENCHANTMENT_REPAIR_MAX_COST.getKey(), -1);
                }

                /**
                 * Sets the maximum XP cost cap for disenchantment and persists the change.
                 *
                 * @param maxCost the desired max cost, or -1 for no cap
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setMaxCost(int maxCost) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_MAX_COST.getKey(), maxCost);
                    save();

                    return getMaxCost() == maxCost;
                }
            }
        }
    }

    /**
     * Configuration section for shatterment (splitting enchanted books).
     * Includes settings for enabled state, disabled worlds, enchantment states,
     * and anvil sound/repair parameters.
     */
    public static class Shatterment {
        private static HashMap<String, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;

        /**
         * Checks whether shatterment is enabled.
         *
         * @return {@code true} if shatterment is enabled
         */
        public static boolean isEnabled() {
            return config.getBoolean(ConfigKeys.SHATTERMENT_ENABLED.getKey());
        }

        /**
         * Sets whether shatterment is enabled and persists the change.
         *
         * @param enabled {@code true} to enable, {@code false} to disable
         * @return {@code true} if the persisted value matches the requested state
         */
        public static boolean setEnabled(boolean enabled) {
            config.set(ConfigKeys.SHATTERMENT_ENABLED.getKey(), enabled);
            save();

            return isEnabled() == enabled;
        }

        /**
         * Gets the list of worlds where shatterment is disabled.
         *
         * @return list of disabled {@link World} instances
         */
        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).filter(java.util.Objects::nonNull).toList());
        }

        /**
         * Sets the list of worlds where shatterment is disabled and persists the change.
         *
         * @param worlds the worlds to disable shatterment in
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            save();

            return getDisabledWorlds().equals(worlds);
        }

        /**
         * Gets the list of materials that cannot be shattered. Since the item losing enchantments
         * during shatterment is always the {@code ENCHANTED_BOOK} in slot 0, this list is checked
         * against that item's material — mirroring how disenchantment checks the donating item.
         *
         * @return list of disabled {@link Material} types
         */
        public static List<Material> getDisabledMaterials() {
            return new ArrayList<>(config.getStringList(ConfigKeys.SHATTERMENT_DISABLED_MATERIALS.getKey()).stream().map(Material::getMaterial).filter(java.util.Objects::nonNull).toList());
        }

        /**
         * Sets the list of materials that cannot be shattered and persists the change.
         *
         * @param materials the materials to disable
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.SHATTERMENT_DISABLED_MATERIALS.getKey(), materials.stream().map(Material::name).toList());
            save();

            return getDisabledMaterials().equals(materials);
        }

        /**
         * Gets the number of enchantments to split off per shatter operation. The underlying
         * YAML value may be a plain integer (fixed count) or a map with {@code min}/{@code max}
         * keys (random range). Legacy plain-integer configs parse as a fixed range ({@code min == max}).
         *
         * @return the configured {@link SplitCountRange}, defaulting to a fixed range of 1
         */
        public static SplitCountRange getSplitCount() {
            var section = config.getConfigurationSection(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey());
            if (section != null && section.contains("min") && section.contains("max")) {
                return new SplitCountRange(section.getInt("min"), section.getInt("max"));
            }

            int value = config.getInt(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey(), 1);
            return new SplitCountRange(value, value);
        }

        /**
         * Sets the split count and persists the change. Fixed ranges ({@code min == max}) are
         * written as a plain integer to keep existing configs looking unchanged; ranges with
         * distinct bounds are written as a {@code min}/{@code max} map.
         *
         * @param range the desired split count range
         */
        public static void setSplitCount(SplitCountRange range) {
            if (range.isFixed()) {
                config.set(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey(), range.min());
            } else {
                config.set(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey() + ".min", range.min());
                config.set(ConfigKeys.SHATTERMENT_SPLIT_COUNT.getKey() + ".max", range.max());
            }
            save();
        }

        /**
         * Gets the per-enchantment state overrides for shatterment. Results are cached.
         *
         * @return map of enchantment key to its {@link EnchantmentStateType}
         */
        public static HashMap<String, EnchantmentStateType> getEnchantmentStates() {
            if (ENCHANTMENT_STATES_CACHE != null) return ENCHANTMENT_STATES_CACHE;

            List<String> list = config.getStringList(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES.getKey());
            HashMap<String, EnchantmentStateType> enchantmentStates = new HashMap<>();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                String key;
                EnchantmentStateType state;

                if (split.length == 2) {
                    key = split[0];
                    state = EnchantmentStateType.getStateByName(split[1]);
                } else if (split.length == 3) {
                    // "minecraft:sharpness:keep" — namespace prefix, drop it
                    key = split[1];
                    state = EnchantmentStateType.getStateByName(split[2]);
                } else {
                    continue;
                }

                enchantmentStates.put(key, state);
            }

            ENCHANTMENT_STATES_CACHE = enchantmentStates;

            return enchantmentStates;
        }

        /**
         * Sets the per-enchantment state overrides for shatterment and persists the change.
         *
         * @param enchantmentStates map of enchantment key to its desired state
         * @return {@code true} if the persisted value matches the requested map
         */
        public static boolean setEnchantmentStates(HashMap<String, EnchantmentStateType> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<String, EnchantmentStateType> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
            }

            config.set(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES.getKey(), list);
            save();

            ENCHANTMENT_STATES_CACHE = new HashMap<>(enchantmentStates);

            return getEnchantmentStates().equals(enchantmentStates);
        }

        /**
         * Gets the per-enchantment success chance overrides for shatterment.
         * Enchantments not present in this map always succeed (chance 1.0).
         *
         * @return map of enchantment key to its success chance (0.0-1.0)
         */
        public static Map<String, Double> getEnchantmentChances() {
            var section = config.getConfigurationSection(ConfigKeys.SHATTERMENT_ENCHANTMENT_CHANCES.getKey());
            if (section == null) return new HashMap<>();
            Map<String, Double> chances = new HashMap<>();
            for (String key : section.getKeys(false)) {
                chances.put(key.toLowerCase(), section.getDouble(key));
            }
            return chances;
        }

        /**
         * Gets the success chance for a specific enchantment key. Missing keys default to 1.0 (always succeeds).
         *
         * @param key the enchantment key (namespaced or short form)
         * @return the configured chance, clamped to [0.0, 1.0]
         */
        public static double getEnchantmentChance(String key) {
            if (key == null) return 1.0;
            String shortKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
            Map<String, Double> chances = getEnchantmentChances();
            Double chance = chances.get(shortKey.toLowerCase());
            if (chance == null) chance = chances.get(key.toLowerCase()); // also match full namespaced key
            if (chance == null) return 1.0;
            return Math.max(0.0, Math.min(1.0, chance));
        }

        /**
         * Sets the success chance for a specific enchantment key and persists the change.
         *
         * @param key    the enchantment key
         * @param chance the desired chance (0.0-1.0)
         */
        public static void setEnchantmentChance(String key, double chance) {
            config.set(ConfigKeys.SHATTERMENT_ENCHANTMENT_CHANCES.getKey() + "." + key.toLowerCase(), Math.max(0.0, Math.min(1.0, chance)));
            save();
        }

        /**
         * Economy settings for shatterment operations.
         */
        public static class Economy {
            /**
             * Checks whether economy integration is enabled for shatterment.
             *
             * @return {@code true} if economy cost is active
             */
            public static boolean isEnabled() {
                return config.getBoolean(ConfigKeys.SHATTERMENT_ECONOMY_ENABLED.getKey());
            }

            /**
             * Sets whether economy integration is enabled for shatterment.
             *
             * @param enabled {@code true} to enable
             * @return {@code true} if the persisted value matches
             */
            public static boolean setEnabled(boolean enabled) {
                config.set(ConfigKeys.SHATTERMENT_ECONOMY_ENABLED.getKey(), enabled);
                save();

                return isEnabled() == enabled;
            }

            /**
             * Gets the economy cost per shatterment operation.
             *
             * @return the configured cost
             */
            public static double getCost() {
                return config.getDouble(ConfigKeys.SHATTERMENT_ECONOMY_COST.getKey());
            }

            /**
             * Sets the economy cost per shatterment operation.
             *
             * @param cost the desired cost
             * @return {@code true} if the persisted value matches
             */
            public static boolean setCost(double cost) {
                config.set(ConfigKeys.SHATTERMENT_ECONOMY_COST.getKey(), cost);
                save();

                return getCost() == cost;
            }

            /**
             * Checks whether the cost message is shown to the player before the operation.
             *
             * @return {@code true} if the cost display is enabled
             */
            public static boolean isShowCostEnabled() {
                return config.getBoolean(ConfigKeys.SHATTERMENT_ECONOMY_SHOW_COST.getKey(), true);
            }

            /**
             * Sets whether the cost message is shown to the player before the operation.
             *
             * @param enabled {@code true} to show cost
             * @return {@code true} if the persisted value matches
             */
            public static boolean setShowCostEnabled(boolean enabled) {
                config.set(ConfigKeys.SHATTERMENT_ECONOMY_SHOW_COST.getKey(), enabled);
                save();

                return isShowCostEnabled() == enabled;
            }

            /**
             * Checks whether a charge confirmation message is sent after the operation.
             *
             * @return {@code true} if the charge message is enabled
             */
            public static boolean isChargeMessageEnabled() {
                return config.getBoolean(ConfigKeys.SHATTERMENT_ECONOMY_CHARGE_MESSAGE.getKey(), true);
            }

            /**
             * Sets whether a charge confirmation message is sent after the operation.
             *
             * @param enabled {@code true} to send the message
             * @return {@code true} if the persisted value matches
             */
            public static boolean setChargeMessageEnabled(boolean enabled) {
                config.set(ConfigKeys.SHATTERMENT_ECONOMY_CHARGE_MESSAGE.getKey(), enabled);
                save();

                return isChargeMessageEnabled() == enabled;
            }
        }

        /**
         * Anvil-related settings for shatterment operations.
         */
        public static class Anvil {
            /**
             * Checks whether the "Too Expensive!" anvil screen bypass is enabled for shatterment.
             *
             * @return {@code true} if the bypass is enabled
             */
            public static boolean isBypassTooExpensiveEnabled() {
                return config.getBoolean(ConfigKeys.SHATTERMENT_ANVIL_BYPASS_TOO_EXPENSIVE.getKey());
            }

            /**
             * Sound settings for the shatterment anvil interaction.
             */
            public static class Sound {
                /**
                 * Checks whether the shatterment anvil sound is enabled.
                 *
                 * @return {@code true} if the sound is enabled
                 */
                public static boolean isEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED.getKey());
                }

                /**
                 * Sets whether the shatterment anvil sound is enabled.
                 *
                 * @param enabled {@code true} to enable the sound
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setEnabled(boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED.getKey(), enabled);
                    save();

                    return isEnabled() == enabled;
                }

                /**
                 * Gets the shatterment anvil sound volume.
                 *
                 * @return the configured volume
                 */
                public static Double getVolume() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey());
                }

                /**
                 * Sets the shatterment anvil sound volume and persists the change.
                 *
                 * @param volume the desired volume
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setVolume(double volume) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey(), volume);
                    save();

                    return getVolume().equals(volume);
                }

                /**
                 * Gets the shatterment anvil sound pitch.
                 *
                 * @return the configured pitch
                 */
                public static Double getPitch() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey());
                }

                /**
                 * Sets the shatterment anvil sound pitch and persists the change.
                 *
                 * @param pitch the desired pitch
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setPitch(double pitch) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey(), pitch);
                    save();

                    return getPitch().equals(pitch);
                }
            }

            /**
             * Repair cost settings for shatterment anvil operations.
             */
            public static class Repair {
                /**
                 * Checks whether repair cost reset is enabled for shatterment.
                 *
                 * @return {@code true} if repair cost reset is enabled
                 */
                public static boolean isResetEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED.getKey());
                }

                /**
                 * Sets whether repair cost reset is enabled for shatterment.
                 *
                 * @param enabled {@code true} to enable reset
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setResetEnabled(boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED.getKey(), enabled);
                    save();

                    return isResetEnabled() == enabled;
                }

                /**
                 * Checks whether repair cost is enabled for shatterment.
                 *
                 * @return {@code true} if repair cost is enabled
                 */
                public static boolean isCostEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED.getKey());
                }

                /**
                 * Sets whether repair cost is enabled for shatterment.
                 *
                 * @param enabled {@code true} to enable cost
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setCostEnabled(boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED.getKey(), enabled);
                    save();

                    return isCostEnabled() == enabled;
                }

                /**
                 * Gets the base repair cost for shatterment.
                 *
                 * @return the configured base cost
                 */
                public static Double getBaseCost() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE.getKey());
                }

                /**
                 * Sets the base repair cost for shatterment and persists the change.
                 *
                 * @param cost the desired base cost
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setBaseCost(double cost) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE.getKey(), cost);
                    save();

                    return getBaseCost().equals(cost);
                }

                /**
                 * Gets the repair cost multiplier for shatterment.
                 *
                 * @return the configured cost multiplier
                 */
                public static Double getCostMultiplier() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER.getKey());
                }

                /**
                 * Sets the repair cost multiplier for shatterment and persists the change.
                 *
                 * @param multiplier the desired cost multiplier
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setCostMultiplier(double multiplier) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER.getKey(), multiplier);
                    save();

                    return getCostMultiplier().equals(multiplier);
                }

                /**
                 * Gets the per-enchantment XP cost overrides for shatterment.
                 *
                 * @return map of enchantment key to its XP cost override
                 */
                public static Map<String, Integer> getEnchantmentCosts() {
                    var section = config.getConfigurationSection(ConfigKeys.SHATTERMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS.getKey());
                    if (section == null) return new HashMap<>();
                    Map<String, Integer> costs = new HashMap<>();
                    for (String key : section.getKeys(false)) {
                        var sub = section.getConfigurationSection(key);
                        if (sub != null) {
                            if (sub.contains("xp")) costs.put(key, sub.getInt("xp"));
                        } else {
                            costs.put(key, section.getInt(key));
                        }
                    }
                    return costs;
                }

                /**
                 * Gets the per-enchantment economy cost overrides for shatterment, parsed from
                 * the map-shaped entries of {@code enchantment-costs} (e.g. {@code mending: { economy: 500 }}).
                 * Enchantments without a map-shaped entry are absent from this map.
                 *
                 * @return map of enchantment key to its economy cost override
                 */
                public static Map<String, Double> getEnchantmentEconomyCosts() {
                    var section = config.getConfigurationSection(ConfigKeys.SHATTERMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS.getKey());
                    if (section == null) return new HashMap<>();
                    Map<String, Double> costs = new HashMap<>();
                    for (String key : section.getKeys(false)) {
                        var sub = section.getConfigurationSection(key);
                        if (sub != null && sub.contains("economy")) {
                            costs.put(key, sub.getDouble("economy"));
                        }
                    }
                    return costs;
                }

                /**
                 * Gets the maximum XP cost cap for shatterment. {@code -1} means no cap (default).
                 *
                 * @return the configured max cost, or -1 if uncapped
                 */
                public static int getMaxCost() {
                    return config.getInt(ConfigKeys.SHATTERMENT_REPAIR_MAX_COST.getKey(), -1);
                }

                /**
                 * Sets the maximum XP cost cap for shatterment and persists the change.
                 *
                 * @param maxCost the desired max cost, or -1 for no cap
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setMaxCost(int maxCost) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_MAX_COST.getKey(), maxCost);
                    save();

                    return getMaxCost() == maxCost;
                }
            }
        }
    }
}
