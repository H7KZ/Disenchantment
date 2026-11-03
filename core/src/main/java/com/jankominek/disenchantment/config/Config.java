package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.*;

/**
 * Provides typed access to the plugin's configuration values.
 * Offers getters and setters for all configurable options including
 * disenchantment settings, shatterment settings, event priorities,
 * anvil sound/repair parameters, and world/material/enchantment states.
 */
public class Config {
    private static boolean batchMode = false;
    private static boolean readOnly = false;

    /**
     * Whether config writes are suppressed. True while the bundled defaults are in use because
     * {@code config.yml} failed to parse, so that in-game changes can never overwrite the user's broken file.
     *
     * @return {@code true} if config saves are currently a no-op
     */
    public static boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Enables or disables read-only mode. Set by {@code ConfigUtils.setupConfig()} on every load.
     *
     * @param value {@code true} to suppress writes to {@code config.yml}
     */
    public static void setReadOnly(boolean value) {
        readOnly = value;
    }

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
        if (!readOnly) plugin.saveConfig();
    }

    private static void save() {
        if (!batchMode && !readOnly) plugin.saveConfig();
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
     * Clears the in-memory caches (enchantment states, chances, XP costs and economy costs) for both
     * disenchantment and shatterment. Must be called after a config reload so that subsequent reads
     * pick up fresh values.
     */
    public static void invalidateCaches() {
        Disenchantment.ENCHANTMENT_STATES_CACHE = null;
        Shatterment.ENCHANTMENT_STATES_CACHE = null;
        Disenchantment.ENCHANTMENT_CHANCES_CACHE = null;
        Shatterment.ENCHANTMENT_CHANCES_CACHE = null;
        Disenchantment.REPAIR_COSTS_CACHE = null;
        Shatterment.REPAIR_COSTS_CACHE = null;
        Disenchantment.REPAIR_ECONOMY_COSTS_CACHE = null;
        Shatterment.REPAIR_ECONOMY_COSTS_CACHE = null;
    }

    /**
     * Logs startup warnings for misconfigured restriction lists across both features: an
     * unrecognised {@code mode} value (which silently falls back to {@link RestrictionMode#DENYLIST})
     * and an {@link RestrictionMode#ALLOWLIST} paired with an empty {@code list} (which blocks the
     * feature entirely). Intended to be called once after the config is loaded.
     */
    public static void validateRestrictions() {
        warnRestriction("disenchantment.worlds", ConfigKeys.DISENCHANTMENT_WORLDS_MODE, ConfigKeys.DISENCHANTMENT_WORLDS_LIST);
        warnRestriction("disenchantment.materials", ConfigKeys.DISENCHANTMENT_MATERIALS_MODE, ConfigKeys.DISENCHANTMENT_MATERIALS_LIST);
        warnRestriction("shatterment.worlds", ConfigKeys.SHATTERMENT_WORLDS_MODE, ConfigKeys.SHATTERMENT_WORLDS_LIST);
        warnRestriction("shatterment.materials", ConfigKeys.SHATTERMENT_MATERIALS_MODE, ConfigKeys.SHATTERMENT_MATERIALS_LIST);
    }

    private static void warnRestriction(String label, ConfigKeys modeKey, ConfigKeys listKey) {
        String rawMode = config.getString(modeKey.getKey());

        if (rawMode != null && !rawMode.isBlank()) {
            boolean recognised = false;
            for (RestrictionMode m : RestrictionMode.values()) {
                if (m.name().equalsIgnoreCase(rawMode.trim())) recognised = true;
            }
            if (!recognised) {
                logger.warning("Config: unknown mode '" + rawMode + "' for " + label + ".mode — defaulting to DENYLIST");
            }
        }

        RestrictionMode mode = RestrictionMode.fromConfig(rawMode);
        if (mode == RestrictionMode.ALLOWLIST && config.getStringList(listKey.getKey()).isEmpty()) {
            logger.warning("Config: " + label + ".mode is ALLOWLIST but " + label + ".list is empty — this blocks the feature entirely (nothing is allowed).");
        }
    }

    /**
     * Checks whether the plugin is globally enabled.
     *
     * @return {@code true} if the plugin is enabled
     */
    public static boolean isPluginEnabled() {
        return ConfigValues.bool(ConfigKeys.ENABLED);
    }

    /**
     * Checks whether the automatic SpigotMC update check is enabled ({@code update-checker}).
     * Read on every scheduled check, so a reload takes effect without a restart.
     *
     * @return {@code true} if update checking is enabled (default)
     */
    public static boolean isUpdateCheckerEnabled() {
        return ConfigValues.bool(ConfigKeys.UPDATE_CHECKER);
    }

    /**
     * Gets how many days of operation records are kept in {@code stats.db}
     * ({@code logging.operations-retention-days}). Records older than this are pruned on startup.
     *
     * @return the retention in days; {@code 0} (default) keeps records forever. Negative values read as {@code 0}
     */
    public static int getOperationsRetentionDays() {
        return ConfigValues.integer(ConfigKeys.LOGGING_OPERATIONS_RETENTION_DAYS);
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
        Object raw = ConfigValues.raw(ConfigKeys.LOCALES);
        if (!(raw instanceof java.util.Collection) && ConfigValues.scalar(raw) == null) return new ArrayList<>(List.of("cs", "en"));

        // Locale codes become file names, so anything that is not a plain code is dropped.
        List<String> locales = new ArrayList<>();
        for (String locale : ConfigValues.toStringList(raw)) {
            if (locale.matches(ConfigSpec.LOCALE_PATTERN) && !locales.contains(locale)) locales.add(locale);
        }
        if (locales.isEmpty() && !(raw instanceof java.util.Collection)) return new ArrayList<>(List.of("cs", "en"));
        return locales;
    }

    /**
     * Gets the currently configured locale identifier.
     *
     * @return the active locale code
     */
    public static String getLocale() {
        return ConfigValues.string(ConfigKeys.LOCALE);
    }

    /**
     * Gets the configured enchantment groups, shared by both disenchantment and shatterment.
     * Each entry maps a group name to the list of enchantment keys (lowercase) it contains.
     *
     * @return map of group name to its member enchantment keys
     */
    public static Map<String, List<String>> getEnchantmentGroups() {
        return ConfigValues.parseGroups(ConfigValues.raw(ConfigKeys.ENCHANTMENT_GROUPS), null);
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
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_DISENCHANT, EventPriority.class);
        }

        /**
         * Gets the event priority for the disenchant inventory-click event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getDisenchantClickEvent() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_DISENCHANT_CLICK, EventPriority.class);
        }

        /**
         * Gets the event priority for the shatter prepare-anvil event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getShatterEvent() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_SHATTER, EventPriority.class);
        }

        /**
         * Gets the event priority for the shatter inventory-click event.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getShatterClickEvent() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_SHATTER_CLICK, EventPriority.class);
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
            return ConfigValues.enumeration(ConfigKeys.LOGGING_LEVEL, LogLevelType.class);
        }

        /**
         * Returns whether crash reports should be written to disk in addition to the console.
         *
         * @return {@code true} if file-based crash reporting is enabled
         */
        public static boolean isSaveReportsEnabled() {
            return ConfigValues.bool(ConfigKeys.LOGGING_SAVE_REPORTS);
        }

        /**
         * Returns whether operation records should be persisted to the SQLite stats database.
         *
         * @return {@code true} if operation logging is enabled
         */
        public static boolean isOperationsEnabled() {
            return ConfigValues.bool(ConfigKeys.LOGGING_OPERATIONS);
        }

        /**
         * Convenience alias for {@link Config#getOperationsRetentionDays()}.
         *
         * @return the operation-record retention in days; {@code 0} keeps records forever
         */
        public static int getOperationsRetentionDays() {
            return Config.getOperationsRetentionDays();
        }

        /**
         * Gets the configured Discord webhook URL for crash-report notifications.
         * Returns an empty string when no webhook is set.
         *
         * @return the Discord webhook URL, or an empty string if not configured
         */
        public static String getDiscordWebhook() {
            return ConfigValues.string(ConfigKeys.LOGGING_DISCORD_WEBHOOK);
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
            return ConfigValues.bool(ConfigKeys.COST_MULTIPLIER_LUCKPERMS);
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
            return ConfigValues.integer(ConfigKeys.COOLDOWN_SECONDS);
        }

        /**
         * Returns whether cooldowns should be delegated to CMI when it is installed.
         *
         * @return {@code true} if the CMI cooldown delegate is enabled
         */
        public static boolean isUseCmiEnabled() {
            return ConfigValues.bool(ConfigKeys.COOLDOWN_USE_CMI);
        }

        /**
         * Returns whether cooldowns should be delegated to EssentialsX when it is installed.
         *
         * @return {@code true} if the EssentialsX cooldown delegate is enabled
         */
        public static boolean isUseEssentialsEnabled() {
            return ConfigValues.bool(ConfigKeys.COOLDOWN_USE_ESSENTIALS);
        }
    }

    /**
     * Configuration section for disenchantment (extracting enchantments from items to books).
     * Includes settings for enabled state, disabled worlds/materials, enchantment states,
     * and anvil sound/repair parameters.
     */
    public static class Disenchantment {
        private static HashMap<String, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;
        private static Map<String, Double> ENCHANTMENT_CHANCES_CACHE = null;
        private static Map<String, Integer> REPAIR_COSTS_CACHE = null;
        private static Map<String, Double> REPAIR_ECONOMY_COSTS_CACHE = null;

        /**
         * Checks whether disenchantment is enabled.
         *
         * @return {@code true} if disenchantment is enabled
         */
        public static boolean isEnabled() {
            return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_ENABLED);
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
         * Gets the configured world restriction mode for disenchantment.
         *
         * @return the {@link RestrictionMode} (defaults to {@link RestrictionMode#DENYLIST})
         */
        public static RestrictionMode getWorldsMode() {
            return RestrictionMode.fromConfig(config.getString(ConfigKeys.DISENCHANTMENT_WORLDS_MODE.getKey()));
        }

        /**
         * Sets the world restriction mode for disenchantment and persists the change.
         *
         * @param mode the mode to apply
         * @return {@code true} if the persisted value matches the requested mode
         */
        public static boolean setWorldsMode(RestrictionMode mode) {
            config.set(ConfigKeys.DISENCHANTMENT_WORLDS_MODE.getKey(), mode.name());
            save();

            return getWorldsMode() == mode;
        }

        /**
         * Gets the configured world restriction list for disenchantment. Interpretation depends
         * on {@link #getWorldsMode()}: a DENYLIST blocks these worlds, an ALLOWLIST permits only
         * these worlds.
         *
         * @return list of listed {@link World} instances
         */
        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(ConfigValues.toStringList(ConfigValues.raw(ConfigKeys.DISENCHANTMENT_WORLDS_LIST)).stream().map(Bukkit::getWorld).filter(java.util.Objects::nonNull).distinct().toList());
        }

        /**
         * Sets the world restriction list for disenchantment and persists the change.
         *
         * @param worlds the worlds to list
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.DISENCHANTMENT_WORLDS_LIST.getKey(), ConfigValues.keepUnresolved(worlds.stream().map(World::getName).toList(), ConfigValues.raw(ConfigKeys.DISENCHANTMENT_WORLDS_LIST), name -> Bukkit.getWorld(name) != null));
            save();

            return getDisabledWorlds().equals(worlds);
        }

        /**
         * Returns whether disenchantment is restricted (blocked) in the given world, taking the
         * configured {@link #getWorldsMode() mode} into account. Bypass permissions are handled
         * separately by the callers.
         *
         * @param world the world to check
         * @return {@code true} if disenchantment must be blocked in that world
         */
        public static boolean isWorldRestricted(World world) {
            return getWorldsMode().isRestricted(getDisabledWorlds().contains(world));
        }

        /**
         * Gets the configured material restriction mode for disenchantment.
         *
         * @return the {@link RestrictionMode} (defaults to {@link RestrictionMode#DENYLIST})
         */
        public static RestrictionMode getMaterialsMode() {
            return RestrictionMode.fromConfig(config.getString(ConfigKeys.DISENCHANTMENT_MATERIALS_MODE.getKey()));
        }

        /**
         * Sets the material restriction mode for disenchantment and persists the change.
         *
         * @param mode the mode to apply
         * @return {@code true} if the persisted value matches the requested mode
         */
        public static boolean setMaterialsMode(RestrictionMode mode) {
            config.set(ConfigKeys.DISENCHANTMENT_MATERIALS_MODE.getKey(), mode.name());
            save();

            return getMaterialsMode() == mode;
        }

        /**
         * Gets the configured material restriction list for disenchantment. Interpretation depends
         * on {@link #getMaterialsMode()}: a DENYLIST blocks these materials, an ALLOWLIST permits
         * only these materials.
         *
         * @return list of listed {@link Material} types
         */
        public static List<Material> getDisabledMaterials() {
            return ConfigValues.parseMaterials(ConfigValues.raw(ConfigKeys.DISENCHANTMENT_MATERIALS_LIST), null);
        }

        /**
         * Sets the material restriction list for disenchantment and persists the change.
         *
         * @param materials the materials to list
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.DISENCHANTMENT_MATERIALS_LIST.getKey(), ConfigValues.keepUnresolved(materials.stream().map(Material::name).toList(), ConfigValues.raw(ConfigKeys.DISENCHANTMENT_MATERIALS_LIST), name -> ConfigValues.matchMaterial(name) != null));
            save();

            return getDisabledMaterials().equals(materials);
        }

        /**
         * Returns whether the given material is restricted (blocked) from disenchantment, taking
         * the configured {@link #getMaterialsMode() mode} into account. Bypass permissions are
         * handled separately by the callers.
         *
         * @param material the material to check
         * @return {@code true} if disenchantment must be blocked for that material
         */
        public static boolean isMaterialRestricted(Material material) {
            return getMaterialsMode().isRestricted(getDisabledMaterials().contains(material));
        }

        /**
         * Gets the per-enchantment state overrides for disenchantment. Results are cached.
         *
         * @return map of enchantment key to its {@link EnchantmentStateType}
         */
        public static HashMap<String, EnchantmentStateType> getEnchantmentStates() {
            if (ENCHANTMENT_STATES_CACHE != null) return ENCHANTMENT_STATES_CACHE;

            HashMap<String, EnchantmentStateType> enchantmentStates = ConfigValues.parseEnchantmentStates(ConfigValues.raw(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES), null);

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
            if (ENCHANTMENT_CHANCES_CACHE != null) return ENCHANTMENT_CHANCES_CACHE;

            Map<String, Double> chances = ConfigValues.parseChances(ConfigValues.raw(ConfigKeys.DISENCHANTMENT_ENCHANTMENT_CHANCES), null);

            ENCHANTMENT_CHANCES_CACHE = Collections.unmodifiableMap(chances);
            return ENCHANTMENT_CHANCES_CACHE;
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
            Double chance = chances.get(shortKey.toLowerCase(java.util.Locale.ROOT));
            if (chance == null) chance = chances.get(key.toLowerCase(java.util.Locale.ROOT)); // also match full namespaced key
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
            ENCHANTMENT_CHANCES_CACHE = null;
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
                return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_ECONOMY_ENABLED);
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
                return ConfigValues.decimal(ConfigKeys.DISENCHANTMENT_ECONOMY_COST);
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
                return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_ECONOMY_SHOW_COST);
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
                return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_ECONOMY_CHARGE_MESSAGE);
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
                return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_ANVIL_BYPASS_TOO_EXPENSIVE);
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
                    return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED);
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
                    return ConfigValues.decimal(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME);
                }

                /**
                 * Sets the disenchantment anvil sound volume and persists the change.
                 *
                 * @param volume the desired volume
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setVolume(double volume) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey(), ConfigSpec.of(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME).resolve(volume));
                    save();

                    return getVolume().equals(volume);
                }

                /**
                 * Gets the disenchantment anvil sound pitch.
                 *
                 * @return the configured pitch
                 */
                public static Double getPitch() {
                    return ConfigValues.decimal(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH);
                }

                /**
                 * Sets the disenchantment anvil sound pitch and persists the change.
                 *
                 * @param pitch the desired pitch
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setPitch(double pitch) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey(), ConfigSpec.of(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH).resolve(pitch));
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
                    return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED);
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
                    return ConfigValues.bool(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED);
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
                    return ConfigValues.decimal(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE);
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
                    return ConfigValues.decimal(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER);
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
                    if (REPAIR_COSTS_CACHE != null) return REPAIR_COSTS_CACHE;

                    Map<String, Integer> costs = ConfigValues.parseXpCosts(ConfigValues.raw(ConfigKeys.DISENCHANTMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS), null);
                    REPAIR_COSTS_CACHE = Collections.unmodifiableMap(costs);
                    return REPAIR_COSTS_CACHE;
                }

                /**
                 * Gets the per-enchantment economy cost overrides for disenchantment, parsed from
                 * the map-shaped entries of {@code enchantment-costs} (e.g. {@code mending: { economy: 500 }}).
                 * Enchantments without a map-shaped entry are absent from this map.
                 *
                 * @return map of enchantment key to its economy cost override
                 */
                public static Map<String, Double> getEnchantmentEconomyCosts() {
                    if (REPAIR_ECONOMY_COSTS_CACHE != null) return REPAIR_ECONOMY_COSTS_CACHE;

                    Map<String, Double> costs = ConfigValues.parseEconomyCosts(ConfigValues.raw(ConfigKeys.DISENCHANTMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS), null);

                    REPAIR_ECONOMY_COSTS_CACHE = Collections.unmodifiableMap(costs);
                    return REPAIR_ECONOMY_COSTS_CACHE;
                }

                /**
                 * Gets the maximum XP cost cap for disenchantment. {@code -1} means no cap (default).
                 *
                 * @return the configured max cost, or -1 if uncapped
                 */
                public static int getMaxCost() {
                    return ConfigValues.integer(ConfigKeys.DISENCHANTMENT_REPAIR_MAX_COST);
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
        private static Map<String, Double> ENCHANTMENT_CHANCES_CACHE = null;
        private static Map<String, Integer> REPAIR_COSTS_CACHE = null;
        private static Map<String, Double> REPAIR_ECONOMY_COSTS_CACHE = null;

        /**
         * Checks whether shatterment is enabled.
         *
         * @return {@code true} if shatterment is enabled
         */
        public static boolean isEnabled() {
            return ConfigValues.bool(ConfigKeys.SHATTERMENT_ENABLED);
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
         * Gets the configured world restriction mode for shatterment.
         *
         * @return the {@link RestrictionMode} (defaults to {@link RestrictionMode#DENYLIST})
         */
        public static RestrictionMode getWorldsMode() {
            return RestrictionMode.fromConfig(config.getString(ConfigKeys.SHATTERMENT_WORLDS_MODE.getKey()));
        }

        /**
         * Sets the world restriction mode for shatterment and persists the change.
         *
         * @param mode the mode to apply
         * @return {@code true} if the persisted value matches the requested mode
         */
        public static boolean setWorldsMode(RestrictionMode mode) {
            config.set(ConfigKeys.SHATTERMENT_WORLDS_MODE.getKey(), mode.name());
            save();

            return getWorldsMode() == mode;
        }

        /**
         * Gets the configured world restriction list for shatterment. Interpretation depends on
         * {@link #getWorldsMode()}: a DENYLIST blocks these worlds, an ALLOWLIST permits only
         * these worlds.
         *
         * @return list of listed {@link World} instances
         */
        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(ConfigValues.toStringList(ConfigValues.raw(ConfigKeys.SHATTERMENT_WORLDS_LIST)).stream().map(Bukkit::getWorld).filter(java.util.Objects::nonNull).distinct().toList());
        }

        /**
         * Sets the world restriction list for shatterment and persists the change.
         *
         * @param worlds the worlds to list
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.SHATTERMENT_WORLDS_LIST.getKey(), ConfigValues.keepUnresolved(worlds.stream().map(World::getName).toList(), ConfigValues.raw(ConfigKeys.SHATTERMENT_WORLDS_LIST), name -> Bukkit.getWorld(name) != null));
            save();

            return getDisabledWorlds().equals(worlds);
        }

        /**
         * Returns whether shatterment is restricted (blocked) in the given world, taking the
         * configured {@link #getWorldsMode() mode} into account. Bypass permissions are handled
         * separately by the callers.
         *
         * @param world the world to check
         * @return {@code true} if shatterment must be blocked in that world
         */
        public static boolean isWorldRestricted(World world) {
            return getWorldsMode().isRestricted(getDisabledWorlds().contains(world));
        }

        /**
         * Gets the configured material restriction mode for shatterment.
         *
         * @return the {@link RestrictionMode} (defaults to {@link RestrictionMode#DENYLIST})
         */
        public static RestrictionMode getMaterialsMode() {
            return RestrictionMode.fromConfig(config.getString(ConfigKeys.SHATTERMENT_MATERIALS_MODE.getKey()));
        }

        /**
         * Sets the material restriction mode for shatterment and persists the change.
         *
         * @param mode the mode to apply
         * @return {@code true} if the persisted value matches the requested mode
         */
        public static boolean setMaterialsMode(RestrictionMode mode) {
            config.set(ConfigKeys.SHATTERMENT_MATERIALS_MODE.getKey(), mode.name());
            save();

            return getMaterialsMode() == mode;
        }

        /**
         * Gets the configured material restriction list for shatterment. Since the item losing
         * enchantments during shatterment is always the {@code ENCHANTED_BOOK} in slot 0, this
         * list is checked against that item's material. Interpretation depends on
         * {@link #getMaterialsMode()}: a DENYLIST blocks these materials, an ALLOWLIST permits
         * only these materials.
         *
         * @return list of listed {@link Material} types
         */
        public static List<Material> getDisabledMaterials() {
            return ConfigValues.parseMaterials(ConfigValues.raw(ConfigKeys.SHATTERMENT_MATERIALS_LIST), null);
        }

        /**
         * Sets the material restriction list for shatterment and persists the change.
         *
         * @param materials the materials to list
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.SHATTERMENT_MATERIALS_LIST.getKey(), ConfigValues.keepUnresolved(materials.stream().map(Material::name).toList(), ConfigValues.raw(ConfigKeys.SHATTERMENT_MATERIALS_LIST), name -> ConfigValues.matchMaterial(name) != null));
            save();

            return getDisabledMaterials().equals(materials);
        }

        /**
         * Returns whether the given material is restricted (blocked) from shatterment, taking the
         * configured {@link #getMaterialsMode() mode} into account. Bypass permissions are handled
         * separately by the callers.
         *
         * @param material the material to check
         * @return {@code true} if shatterment must be blocked for that material
         */
        public static boolean isMaterialRestricted(Material material) {
            return getMaterialsMode().isRestricted(getDisabledMaterials().contains(material));
        }

        /**
         * Gets the number of enchantments to split off per shatter operation. The underlying
         * YAML value may be a plain integer (fixed count) or a map with {@code min}/{@code max}
         * keys (random range). Legacy plain-integer configs parse as a fixed range ({@code min == max}).
         *
         * @return the configured {@link SplitCountRange}, defaulting to a fixed range of 1
         */
        public static SplitCountRange getSplitCount() {
            return ConfigValues.parseSplitCount(ConfigValues.raw(ConfigKeys.SHATTERMENT_SPLIT_COUNT));
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

            HashMap<String, EnchantmentStateType> enchantmentStates = ConfigValues.parseEnchantmentStates(ConfigValues.raw(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES), null);

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
            if (ENCHANTMENT_CHANCES_CACHE != null) return ENCHANTMENT_CHANCES_CACHE;

            Map<String, Double> chances = ConfigValues.parseChances(ConfigValues.raw(ConfigKeys.SHATTERMENT_ENCHANTMENT_CHANCES), null);

            ENCHANTMENT_CHANCES_CACHE = Collections.unmodifiableMap(chances);
            return ENCHANTMENT_CHANCES_CACHE;
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
            Double chance = chances.get(shortKey.toLowerCase(java.util.Locale.ROOT));
            if (chance == null) chance = chances.get(key.toLowerCase(java.util.Locale.ROOT)); // also match full namespaced key
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
            ENCHANTMENT_CHANCES_CACHE = null;
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
                return ConfigValues.bool(ConfigKeys.SHATTERMENT_ECONOMY_ENABLED);
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
                return ConfigValues.decimal(ConfigKeys.SHATTERMENT_ECONOMY_COST);
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
                return ConfigValues.bool(ConfigKeys.SHATTERMENT_ECONOMY_SHOW_COST);
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
                return ConfigValues.bool(ConfigKeys.SHATTERMENT_ECONOMY_CHARGE_MESSAGE);
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
                return ConfigValues.bool(ConfigKeys.SHATTERMENT_ANVIL_BYPASS_TOO_EXPENSIVE);
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
                    return ConfigValues.bool(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED);
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
                    return ConfigValues.decimal(ConfigKeys.SHATTERMENT_ANVIL_VOLUME);
                }

                /**
                 * Sets the shatterment anvil sound volume and persists the change.
                 *
                 * @param volume the desired volume
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setVolume(double volume) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey(), ConfigSpec.of(ConfigKeys.SHATTERMENT_ANVIL_VOLUME).resolve(volume));
                    save();

                    return getVolume().equals(volume);
                }

                /**
                 * Gets the shatterment anvil sound pitch.
                 *
                 * @return the configured pitch
                 */
                public static Double getPitch() {
                    return ConfigValues.decimal(ConfigKeys.SHATTERMENT_ANVIL_PITCH);
                }

                /**
                 * Sets the shatterment anvil sound pitch and persists the change.
                 *
                 * @param pitch the desired pitch
                 * @return {@code true} if the persisted value matches
                 */
                public static boolean setPitch(double pitch) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey(), ConfigSpec.of(ConfigKeys.SHATTERMENT_ANVIL_PITCH).resolve(pitch));
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
                    return ConfigValues.bool(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED);
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
                    return ConfigValues.bool(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED);
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
                    return ConfigValues.decimal(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE);
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
                    return ConfigValues.decimal(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER);
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
                    if (REPAIR_COSTS_CACHE != null) return REPAIR_COSTS_CACHE;

                    Map<String, Integer> costs = ConfigValues.parseXpCosts(ConfigValues.raw(ConfigKeys.SHATTERMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS), null);
                    REPAIR_COSTS_CACHE = Collections.unmodifiableMap(costs);
                    return REPAIR_COSTS_CACHE;
                }

                /**
                 * Gets the per-enchantment economy cost overrides for shatterment, parsed from
                 * the map-shaped entries of {@code enchantment-costs} (e.g. {@code mending: { economy: 500 }}).
                 * Enchantments without a map-shaped entry are absent from this map.
                 *
                 * @return map of enchantment key to its economy cost override
                 */
                public static Map<String, Double> getEnchantmentEconomyCosts() {
                    if (REPAIR_ECONOMY_COSTS_CACHE != null) return REPAIR_ECONOMY_COSTS_CACHE;

                    Map<String, Double> costs = ConfigValues.parseEconomyCosts(ConfigValues.raw(ConfigKeys.SHATTERMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS), null);

                    REPAIR_ECONOMY_COSTS_CACHE = Collections.unmodifiableMap(costs);
                    return REPAIR_ECONOMY_COSTS_CACHE;
                }

                /**
                 * Gets the maximum XP cost cap for shatterment. {@code -1} means no cap (default).
                 *
                 * @return the configured max cost, or -1 if uncapped
                 */
                public static int getMaxCost() {
                    return ConfigValues.integer(ConfigKeys.SHATTERMENT_REPAIR_MAX_COST);
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

    /**
     * Player feedback settings: success particles, the failure sound, the debounced
     * "not enough levels" message and the result-book preview lore. Read-only: edit
     * {@code config.yml} and reload.
     */
    public static class Feedback {
        /**
         * Checks whether the debounced "not enough levels" message is sent when a click is refused.
         *
         * @return {@code true} (default) if the message is enabled
         */
        public static boolean isNotEnoughXpMessageEnabled() {
            return ConfigValues.bool(ConfigKeys.FEEDBACK_NOT_ENOUGH_XP_MESSAGE);
        }

        /**
         * Checks whether the result book of a prepared disenchant/shatter lists what transfers and the cost.
         *
         * @return {@code true} (default) if the preview lore is enabled
         */
        public static boolean isPreviewLoreEnabled() {
            return ConfigValues.bool(ConfigKeys.FEEDBACK_PREVIEW_LORE);
        }

        /**
         * Success particle settings, spawned at the anvil when an operation completes.
         */
        public static class Particles {
            /**
             * Checks whether success particles are enabled.
             *
             * @return {@code true} (default) if particles are spawned
             */
            public static boolean isEnabled() {
                return ConfigValues.bool(ConfigKeys.FEEDBACK_PARTICLES_ENABLED);
            }

            /**
             * Gets the particle names spawned by a successful disenchant or shatter. Names that do not
             * exist on the running server are skipped, so legacy and current names can be listed together.
             *
             * @return the configured particle names (default {@code ENCHANT, ENCHANTMENT_TABLE})
             */
            public static List<String> getTypes() {
                return getNames(ConfigKeys.FEEDBACK_PARTICLES_TYPES, List.of("ENCHANT", "ENCHANTMENT_TABLE"));
            }

            /**
             * Gets the particle names spawned by a successful erase.
             *
             * @return the configured particle names (default {@code SMOKE, SMOKE_NORMAL})
             */
            public static List<String> getEraseTypes() {
                return getNames(ConfigKeys.FEEDBACK_PARTICLES_ERASE_TYPES, List.of("SMOKE", "SMOKE_NORMAL"));
            }

            /**
             * Gets how many particles of each type are spawned.
             *
             * @return the count, clamped to [1, 500]
             */
            public static int getCount() {
                return ConfigValues.integer(ConfigKeys.FEEDBACK_PARTICLES_COUNT);
            }

            /**
             * Gets the random spread of the particles around the spawn point, in blocks.
             *
             * @return the spread, never negative
             */
            public static double getSpread() {
                return ConfigValues.decimal(ConfigKeys.FEEDBACK_PARTICLES_SPREAD);
            }

            /**
             * Gets who sees the particles.
             *
             * @return the {@link ParticleScopeType} (defaults to {@link ParticleScopeType#PLAYER})
             */
            public static ParticleScopeType getScope() {
                return ConfigValues.enumeration(ConfigKeys.FEEDBACK_PARTICLES_SCOPE, ParticleScopeType.class);
            }

            private static List<String> getNames(ConfigKeys key, List<String> fallback) {
                Object raw = ConfigValues.raw(key);
                if (raw == null || !(raw instanceof java.util.Collection || ConfigValues.scalar(raw) != null)) return fallback;
                return ConfigValues.toStringList(raw);
            }
        }

        /**
         * Sound played to a player whose anvil click was refused.
         */
        public static class FailureSound {
            /**
             * Checks whether the failure sound is enabled.
             *
             * @return {@code true} (default) if the sound is played
             */
            public static boolean isEnabled() {
                return ConfigValues.bool(ConfigKeys.FEEDBACK_FAILURE_SOUND_ENABLED);
            }

            /**
             * Gets the vanilla sound key played on a refused click (a string, never a {@code Sound}
             * constant, so it works on every server version).
             *
             * @return the sound key (default {@code entity.villager.no})
             */
            public static String getSound() {
                return ConfigValues.string(ConfigKeys.FEEDBACK_FAILURE_SOUND_SOUND);
            }

            /**
             * Gets the failure sound volume.
             *
             * @return the configured volume
             */
            public static float getVolume() {
                return (float) ConfigValues.decimal(ConfigKeys.FEEDBACK_FAILURE_SOUND_VOLUME);
            }

            /**
             * Gets the failure sound pitch.
             *
             * @return the configured pitch
             */
            public static float getPitch() {
                return (float) ConfigValues.decimal(ConfigKeys.FEEDBACK_FAILURE_SOUND_PITCH);
            }
        }
    }

    /**
     * Configuration section for erase mode: an enchanted item plus a catalyst in the anvil yields the same
     * item without its erasable enchantments, and no book. Worlds, materials and enchantment states are
     * shared with disenchantment. Read-only: edit {@code config.yml} and reload.
     */
    public static class Erase {
        /**
         * Checks whether erase mode is enabled.
         *
         * @return {@code true} if erase mode is enabled (default {@code false})
         */
        public static boolean isEnabled() {
            return ConfigValues.bool(ConfigKeys.ERASE_ENABLED);
        }

        /**
         * Gets the configured catalyst materials, unparsed.
         *
         * @return the material names (default {@code ECHO_SHARD})
         */
        public static List<String> getCatalysts() {
            Object raw = ConfigValues.raw(ConfigKeys.ERASE_CATALYSTS);
            if (raw == null || !(raw instanceof java.util.Collection || ConfigValues.scalar(raw) != null)) return List.of("ECHO_SHARD");
            return ConfigValues.toStringList(raw);
        }

        /**
         * Checks whether one catalyst is consumed per erase.
         *
         * @return {@code true} (default) if the catalyst is used up
         */
        public static boolean isConsumeCatalyst() {
            return ConfigValues.bool(ConfigKeys.ERASE_CONSUME_CATALYST);
        }

        /**
         * Checks whether curses are erased too.
         *
         * @return {@code true} if curses are erased (default {@code false}: they stay on the item)
         */
        public static boolean isEraseCurses() {
            return ConfigValues.bool(ConfigKeys.ERASE_ERASE_CURSES);
        }

        /**
         * Gets the event priority of the erase prepare-anvil handler.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getPrepareEventPriority() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_ERASE, EventPriority.class);
        }

        /**
         * Gets the event priority of the erase inventory-click handler.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getClickEventPriority() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_ERASE_CLICK, EventPriority.class);
        }

        /**
         * Economy settings for erase operations.
         */
        public static class Economy {
            /**
             * Checks whether economy integration is enabled for erasing.
             *
             * @return {@code true} if economy cost is active
             */
            public static boolean isEnabled() {
                return ConfigValues.bool(ConfigKeys.ERASE_ECONOMY_ENABLED);
            }

            /**
             * Gets the economy cost per erase operation.
             *
             * @return the configured cost
             */
            public static double getCost() {
                return ConfigValues.decimal(ConfigKeys.ERASE_ECONOMY_COST);
            }

            /**
             * Checks whether the cost is shown to the player in the action bar before the operation.
             *
             * @return {@code true} if the cost display is enabled
             */
            public static boolean isShowCostEnabled() {
                return ConfigValues.bool(ConfigKeys.ERASE_ECONOMY_SHOW_COST);
            }

            /**
             * Checks whether a charge confirmation message is sent after the operation.
             *
             * @return {@code true} if the charge message is enabled
             */
            public static boolean isChargeMessageEnabled() {
                return ConfigValues.bool(ConfigKeys.ERASE_ECONOMY_CHARGE_MESSAGE);
            }
        }

        /**
         * Anvil-related settings for erase operations.
         */
        public static class Anvil {
            /**
             * Checks whether the "Too Expensive!" anvil screen bypass is enabled for erasing.
             *
             * @return {@code true} if the bypass is enabled
             */
            public static boolean isBypassTooExpensiveEnabled() {
                return ConfigValues.bool(ConfigKeys.ERASE_ANVIL_BYPASS_TOO_EXPENSIVE);
            }

            /**
             * Sound settings for the erase anvil interaction.
             */
            public static class Sound {
                /**
                 * Checks whether the erase anvil sound is enabled.
                 *
                 * @return {@code true} if the sound is enabled
                 */
                public static boolean isEnabled() {
                    return ConfigValues.bool(ConfigKeys.ERASE_ANVIL_SOUND_ENABLED);
                }

                /**
                 * Gets the erase anvil sound volume.
                 *
                 * @return the configured volume
                 */
                public static Double getVolume() {
                    return ConfigValues.decimal(ConfigKeys.ERASE_ANVIL_VOLUME);
                }

                /**
                 * Gets the erase anvil sound pitch.
                 *
                 * @return the configured pitch
                 */
                public static Double getPitch() {
                    return ConfigValues.decimal(ConfigKeys.ERASE_ANVIL_PITCH);
                }
            }

            /**
             * Repair cost settings for erase anvil operations.
             */
            public static class Repair {
                /**
                 * Checks whether the repair cost of the erased item is reset.
                 *
                 * @return {@code true} if repair cost reset is enabled
                 */
                public static boolean isResetEnabled() {
                    return ConfigValues.bool(ConfigKeys.ERASE_REPAIR_RESET_ENABLED);
                }

                /**
                 * Checks whether erasing costs experience levels.
                 *
                 * @return {@code true} if repair cost is enabled
                 */
                public static boolean isCostEnabled() {
                    return ConfigValues.bool(ConfigKeys.ERASE_REPAIR_COST_ENABLED);
                }

                /**
                 * Gets the base repair cost for erasing.
                 *
                 * @return the configured base cost
                 */
                public static Double getBaseCost() {
                    return ConfigValues.decimal(ConfigKeys.ERASE_REPAIR_COST_BASE);
                }

                /**
                 * Gets the per-level cost multiplier for erasing.
                 *
                 * @return the configured cost multiplier
                 */
                public static Double getCostMultiplier() {
                    return ConfigValues.decimal(ConfigKeys.ERASE_REPAIR_COST_MULTIPLIER);
                }

                /**
                 * Gets the per-enchantment XP cost overrides for erasing (not cached: it is read once per anvil update).
                 *
                 * @return map of enchantment key to its XP cost override
                 */
                public static Map<String, Integer> getEnchantmentCosts() {
                    return ConfigValues.parseXpCosts(ConfigValues.raw(ConfigKeys.ERASE_ANVIL_REPAIR_ENCHANTMENT_COSTS), null);
                }

                /**
                 * Gets the per-enchantment economy cost overrides for erasing, parsed from the map-shaped
                 * entries of {@code enchantment-costs}.
                 *
                 * @return map of enchantment key to its economy cost override
                 */
                public static Map<String, Double> getEnchantmentEconomyCosts() {
                    return ConfigValues.parseEconomyCosts(ConfigValues.raw(ConfigKeys.ERASE_ANVIL_REPAIR_ENCHANTMENT_COSTS), null);
                }

                /**
                 * Gets the maximum XP cost cap for erasing. {@code -1} means no cap (default).
                 *
                 * @return the configured max cost, or -1 if uncapped
                 */
                public static int getMaxCost() {
                    return ConfigValues.integer(ConfigKeys.ERASE_REPAIR_MAX_COST);
                }
            }
        }
    }

    /**
     * Configuration of the special anvils feature: which anvils may perform plugin operations, how
     * the special anvil item looks, and its optional crafting recipe.
     */
    public static class SpecialAnvils {
        /**
         * Default material for {@code block-below} when the configured one is unusable.
         */
        public static final Material DEFAULT_BLOCK_BELOW = Material.LAPIS_BLOCK;

        /**
         * Gets the configured anvil restriction mode. An unknown value falls back to
         * {@link SpecialAnvilMode#ALL}; see {@link #validate()} for the matching startup warning.
         *
         * @return the active {@link SpecialAnvilMode}
         */
        public static SpecialAnvilMode getMode() {
            return SpecialAnvilMode.fromConfig(config.getString(ConfigKeys.SPECIAL_ANVILS_MODE.getKey()));
        }

        /**
         * Gets the material an anvil must stand on in {@link SpecialAnvilMode#BLOCK_BELOW} mode.
         * An unknown or non-block value falls back to {@link #DEFAULT_BLOCK_BELOW}.
         *
         * @return the required block-below material
         */
        public static Material getBlockBelow() {
            Material material = parseBlockBelow(config.getString(ConfigKeys.SPECIAL_ANVILS_BLOCK_BELOW.getKey()));
            return material != null ? material : DEFAULT_BLOCK_BELOW;
        }

        static Material parseBlockBelow(String name) {
            if (name == null || name.isBlank()) return null;

            Material material = ConfigValues.matchMaterial(name);
            return material != null && material.isBlock() ? material : null;
        }

        /**
         * Returns whether anvils opened without a world location (virtual anvils, e.g. from
         * {@code Player#openAnvil} or another plugin's menu) count as special.
         *
         * @return {@code true} if virtual anvils may perform plugin operations
         */
        public static boolean isAllowVirtualAnvils() {
            return ConfigValues.bool(ConfigKeys.SPECIAL_ANVILS_ALLOW_VIRTUAL_ANVILS);
        }

        /**
         * Returns whether a broken special anvil drops the special item instead of a plain anvil.
         *
         * @return {@code true} if broken special anvils drop the special item (default)
         */
        public static boolean isDropSpecialItem() {
            return ConfigValues.bool(ConfigKeys.SPECIAL_ANVILS_DROP_SPECIAL_ITEM);
        }

        /**
         * Returns whether a player who tries to disenchant at a non-special anvil is told about it.
         *
         * @return {@code true} if the hint message is enabled
         */
        public static boolean isHintMessageEnabled() {
            return ConfigValues.bool(ConfigKeys.SPECIAL_ANVILS_HINT_MESSAGE);
        }

        /**
         * Gets the raw display name of the special anvil item (with {@code &} colour codes).
         *
         * @return the configured item name
         */
        public static String getItemName() {
            return ConfigValues.string(ConfigKeys.SPECIAL_ANVILS_ITEM_NAME);
        }

        /**
         * Gets the raw lore lines of the special anvil item (with {@code &} colour codes).
         *
         * @return the configured item lore, possibly empty
         */
        public static List<String> getItemLore() {
            return config.getStringList(ConfigKeys.SPECIAL_ANVILS_ITEM_LORE.getKey());
        }

        /**
         * Returns whether the special anvil crafting recipe is registered.
         *
         * @return {@code true} if the recipe is enabled
         */
        public static boolean isRecipeEnabled() {
            return ConfigValues.bool(ConfigKeys.SPECIAL_ANVILS_RECIPE_ENABLED);
        }

        /**
         * Gets the raw shaped-recipe rows.
         *
         * @return the configured shape rows
         */
        public static List<String> getRecipeShape() {
            return config.getStringList(ConfigKeys.SPECIAL_ANVILS_RECIPE_SHAPE.getKey());
        }

        /**
         * Gets the raw recipe ingredients: single-character key mapped to a material name. The
         * pseudo-ingredient {@code ANVIL} always stands for any of the three anvil damage states.
         *
         * @return map of shape character to configured material name, possibly empty
         */
        public static Map<Character, String> getRecipeIngredients() {
            Map<Character, String> ingredients = new java.util.LinkedHashMap<>();
            var section = config.getConfigurationSection(ConfigKeys.SPECIAL_ANVILS_RECIPE_INGREDIENTS.getKey());
            if (section == null) return ingredients;

            for (String key : section.getKeys(false)) {
                String value = ConfigValues.scalar(section.get(key));
                if (key.length() == 1 && value != null) ingredients.put(key.charAt(0), value);
            }

            return ingredients;
        }
    }

    /**
     * Configuration section for grindstone mode: extracting enchantments onto a plain book and
     * transferring them between two different items. Worlds, materials, enchantment states and
     * chances are shared with the {@code disenchantment} section; everything else lives under
     * {@code grindstone}. Values are read straight from the config (not cached) because they are
     * only read on grindstone slot changes and clicks.
     */
    public static class Grindstone {
        /**
         * Checks whether grindstone extraction (enchanted item + plain book) is enabled.
         *
         * @return {@code true} if the feature is enabled
         */
        public static boolean isEnabled() {
            return ConfigValues.bool(ConfigKeys.GRINDSTONE_ENABLED);
        }

        /**
         * Checks whether curses are extracted too. By default curses stay on the source item,
         * matching the vanilla grindstone.
         *
         * @return {@code true} if curses are transferable
         */
        public static boolean isCursesEnabled() {
            return ConfigValues.bool(ConfigKeys.GRINDSTONE_CURSES);
        }

        /**
         * Gets the configured priority of the grindstone prepare event handler.
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getPrepareEventPriority() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_GRINDSTONE, EventPriority.class);
        }

        /**
         * Gets the configured priority of the grindstone click handler (book placement and result click).
         *
         * @return the configured {@link EventPriority}, defaulting to HIGHEST
         */
        public static EventPriority getClickEventPriority() {
            return ConfigValues.enumeration(ConfigKeys.EVENT_PRIORITIES_GRINDSTONE_CLICK, EventPriority.class);
        }

        /**
         * Sound settings shared by both grindstone operations.
         */
        public static class Sound {
            /**
             * Checks whether the grindstone sound is enabled.
             *
             * @return {@code true} if the sound is enabled
             */
            public static boolean isEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_SOUND_ENABLED);
            }

            /**
             * Gets the grindstone sound volume.
             *
             * @return the configured volume
             */
            public static Double getVolume() {
                return ConfigValues.decimal(ConfigKeys.GRINDSTONE_SOUND_VOLUME);
            }

            /**
             * Gets the grindstone sound pitch.
             *
             * @return the configured pitch
             */
            public static Double getPitch() {
                return ConfigValues.decimal(ConfigKeys.GRINDSTONE_SOUND_PITCH);
            }
        }

        /**
         * XP cost settings of the extraction operation ({@code grindstone.repair}).
         */
        public static class Repair {
            /**
             * @return {@code true} if the item's anvil repair cost is reset after the operation
             */
            public static boolean isResetEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_REPAIR_RESET_ENABLED);
            }

            /**
             * @return {@code true} if the operation costs experience levels
             */
            public static boolean isCostEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_REPAIR_COST_ENABLED);
            }

            /**
             * @return the configured base cost
             */
            public static Double getBaseCost() {
                return ConfigValues.decimal(ConfigKeys.GRINDSTONE_REPAIR_COST_BASE);
            }

            /**
             * @return the configured per-enchantment multiplier
             */
            public static Double getCostMultiplier() {
                return ConfigValues.decimal(ConfigKeys.GRINDSTONE_REPAIR_COST_MULTIPLIER);
            }

            /**
             * @return map of enchantment key to its fixed XP cost override
             */
            public static Map<String, Integer> getEnchantmentCosts() {
                return readEnchantmentCosts(ConfigKeys.GRINDSTONE_REPAIR_ENCHANTMENT_COSTS);
            }

            /**
             * @return map of enchantment key to its fixed economy cost override
             */
            public static Map<String, Double> getEnchantmentEconomyCosts() {
                return readEnchantmentEconomyCosts(ConfigKeys.GRINDSTONE_REPAIR_ENCHANTMENT_COSTS);
            }

            /**
             * @return the XP cost cap, or -1 for no cap
             */
            public static int getMaxCost() {
                return ConfigValues.integer(ConfigKeys.GRINDSTONE_REPAIR_MAX_COST);
            }
        }

        /**
         * Economy settings of the extraction operation ({@code grindstone.economy}).
         */
        public static class Economy {
            /**
             * @return {@code true} if extraction charges money
             */
            public static boolean isEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_ECONOMY_ENABLED);
            }

            /**
             * @return the economy cost of one extraction
             */
            public static double getCost() {
                return ConfigValues.decimal(ConfigKeys.GRINDSTONE_ECONOMY_COST);
            }

            /**
             * @return {@code true} if the cost is shown in the action bar
             */
            public static boolean isShowCostEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_ECONOMY_SHOW_COST);
            }

            /**
             * @return {@code true} if a charge confirmation message is sent
             */
            public static boolean isChargeMessageEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_ECONOMY_CHARGE_MESSAGE);
            }
        }

        /**
         * Item-to-item enchantment transfer on the grindstone ({@code grindstone.transfer}).
         */
        public static class Transfer {
            /**
             * @return {@code true} if item-to-item transfer is enabled
             */
            public static boolean isEnabled() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_ENABLED);
            }

            /**
             * @return {@code true} if enchantments may keep a level above the enchantment's vanilla maximum
             */
            public static boolean isAllowOverMax() {
                return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_ALLOW_OVER_MAX);
            }

            /**
             * @return the maximum number of enchantments one transfer may move (at least 1)
             */
            public static int getMaxEnchantments() {
                return ConfigValues.integer(ConfigKeys.GRINDSTONE_TRANSFER_MAX_ENCHANTMENTS);
            }

            /**
             * XP cost settings of the transfer operation ({@code grindstone.transfer.repair}).
             */
            public static class Repair {
                /**
                 * @return {@code true} if the target's anvil repair cost is reset after the operation
                 */
                public static boolean isResetEnabled() {
                    return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_RESET_ENABLED);
                }

                /**
                 * @return {@code true} if the operation costs experience levels
                 */
                public static boolean isCostEnabled() {
                    return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_COST_ENABLED);
                }

                /**
                 * @return the configured base cost
                 */
                public static Double getBaseCost() {
                    return ConfigValues.decimal(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_COST_BASE);
                }

                /**
                 * @return the configured per-enchantment multiplier
                 */
                public static Double getCostMultiplier() {
                    return ConfigValues.decimal(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_COST_MULTIPLIER);
                }

                /**
                 * @return map of enchantment key to its fixed XP cost override
                 */
                public static Map<String, Integer> getEnchantmentCosts() {
                    return readEnchantmentCosts(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_ENCHANTMENT_COSTS);
                }

                /**
                 * @return map of enchantment key to its fixed economy cost override
                 */
                public static Map<String, Double> getEnchantmentEconomyCosts() {
                    return readEnchantmentEconomyCosts(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_ENCHANTMENT_COSTS);
                }

                /**
                 * @return the XP cost cap, or -1 for no cap
                 */
                public static int getMaxCost() {
                    return ConfigValues.integer(ConfigKeys.GRINDSTONE_TRANSFER_REPAIR_MAX_COST);
                }
            }

            /**
             * Economy settings of the transfer operation ({@code grindstone.transfer.economy}).
             */
            public static class Economy {
                /**
                 * @return {@code true} if transfer charges money
                 */
                public static boolean isEnabled() {
                    return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_ENABLED);
                }

                /**
                 * @return the economy cost of one transfer
                 */
                public static double getCost() {
                    return ConfigValues.decimal(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_COST);
                }

                /**
                 * @return {@code true} if the cost is shown in the action bar
                 */
                public static boolean isShowCostEnabled() {
                    return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_SHOW_COST);
                }

                /**
                 * @return {@code true} if a charge confirmation message is sent
                 */
                public static boolean isChargeMessageEnabled() {
                    return ConfigValues.bool(ConfigKeys.GRINDSTONE_TRANSFER_ECONOMY_CHARGE_MESSAGE);
                }
            }
        }

        // Same two shapes as the anvil sections: "sharpness: 5" (XP only) or "mending: { xp: 10, economy: 500 }".
        private static Map<String, Integer> readEnchantmentCosts(ConfigKeys key) {
            return ConfigValues.parseXpCosts(ConfigValues.raw(key), null);
        }

        private static Map<String, Double> readEnchantmentEconomyCosts(ConfigKeys key) {
            return ConfigValues.parseEconomyCosts(ConfigValues.raw(key), null);
        }
    }

    /**
     * Configuration section for the Bedrock fallback: a chest-menu anvil for clients (Geyser/Bedrock) that
     * cannot see custom anvil results. Every getter is total: whatever the YAML holds, it neither throws nor
     * returns something that would make the fallback open for everybody by accident (booleans fall back to
     * {@code false}, list entries that name nothing are skipped).
     */
    public static class Bedrock {
        /**
         * The detection order used when {@code bedrock.detect} is missing or unusable.
         */
        public static final List<BedrockDetectMethod> DEFAULT_DETECT = List.of(
                BedrockDetectMethod.FLOODGATE, BedrockDetectMethod.GEYSER, BedrockDetectMethod.UUID, BedrockDetectMethod.PREFIX);

        /**
         * The name prefixes used when {@code bedrock.prefixes} is missing or unusable.
         */
        public static final List<String> DEFAULT_PREFIXES = List.of(".", "*");

        /**
         * Checks whether the {@code /disenchantment anvil} command and the sneak-click trigger work at all.
         *
         * @return {@code true} if the fallback is enabled (default {@code false})
         */
        public static boolean isFallbackEnabled() {
            return ConfigValues.bool(ConfigKeys.BEDROCK_FALLBACK_ENABLED);
        }

        /**
         * Checks whether players who are not detected as Bedrock players may use the command too.
         *
         * @return {@code true} if everyone may use it (default {@code false}: Bedrock players only)
         */
        public static boolean isAllowJavaPlayers() {
            return ConfigValues.bool(ConfigKeys.BEDROCK_ALLOW_JAVA_PLAYERS);
        }

        /**
         * Checks whether a Bedrock player who sneaks and right-clicks an anvil gets the chest menu instead of
         * the vanilla anvil.
         *
         * @return {@code true} if the sneak-click opens the menu (default {@code false})
         */
        public static boolean isOpenOnSneakClick() {
            return ConfigValues.bool(ConfigKeys.BEDROCK_OPEN_ON_SNEAK_CLICK);
        }

        /**
         * Gets the ordered detection methods. A missing key or a value that is neither a list nor a single
         * name gives {@link #DEFAULT_DETECT}; entries that name no method are skipped, so an explicit empty
         * list (or a list of only unknown names) turns detection off.
         *
         * @return the methods in the order they are tried, without duplicates
         */
        public static List<BedrockDetectMethod> getDetect() {
            return parseDetect(ConfigValues.raw(ConfigKeys.BEDROCK_DETECT), null);
        }

        /**
         * Parses a raw {@code bedrock.detect} value.
         *
         * @param raw     any value
         * @param unknown receives the entries that name no method, or {@code null}
         * @return the methods in order, without duplicates
         */
        public static List<BedrockDetectMethod> parseDetect(Object raw, List<String> unknown) {
            if (raw == null) return DEFAULT_DETECT;
            if (!(raw instanceof java.util.Collection<?>) && ConfigValues.scalar(raw) == null) return DEFAULT_DETECT;

            List<BedrockDetectMethod> methods = new ArrayList<>();
            for (String entry : ConfigValues.toStringList(raw)) {
                BedrockDetectMethod method = BedrockDetectMethod.match(entry);
                if (method == null) {
                    if (unknown != null) unknown.add(entry);
                } else if (!methods.contains(method)) {
                    methods.add(method);
                }
            }

            return methods;
        }

        /**
         * Gets the Bedrock name prefixes. A missing key or a value that is neither a list nor a single text
         * gives {@link #DEFAULT_PREFIXES}; blank entries are skipped (an empty prefix would match everybody).
         *
         * @return the prefixes, without duplicates
         */
        public static List<String> getPrefixes() {
            Object raw = ConfigValues.raw(ConfigKeys.BEDROCK_PREFIXES);
            if (raw == null) return DEFAULT_PREFIXES;
            if (!(raw instanceof java.util.Collection<?>) && ConfigValues.scalar(raw) == null) return DEFAULT_PREFIXES;

            List<String> prefixes = new ArrayList<>();
            for (String prefix : ConfigValues.toStringList(raw)) {
                if (!prefixes.contains(prefix)) prefixes.add(prefix);
            }

            return prefixes;
        }
    }
}
