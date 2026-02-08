package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
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
        plugin.saveConfig();

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
            plugin.saveConfig();

            return isEnabled() == enabled;
        }

        /**
         * Gets the list of worlds where disenchantment is disabled.
         *
         * @return list of disabled {@link World} instances
         */
        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).toList());
        }

        /**
         * Sets the list of worlds where disenchantment is disabled and persists the change.
         *
         * @param worlds the worlds to disable disenchantment in
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            plugin.saveConfig();

            return getDisabledWorlds().equals(worlds);
        }

        /**
         * Gets the list of materials that cannot be disenchanted.
         *
         * @return list of disabled {@link Material} types
         */
        public static List<Material> getDisabledMaterials() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey()).stream().map(Material::getMaterial).toList());
        }

        /**
         * Sets the list of materials that cannot be disenchanted and persists the change.
         *
         * @param materials the materials to disable
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey(), materials.stream().map(Material::name).toList());
            plugin.saveConfig();

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

                if (split.length != 2) continue;

                String key = split[0];
                EnchantmentStateType state = EnchantmentStateType.getStateByName(split[1]);

                enchantmentStates.put(
                        key,
                        state
                );
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
            plugin.saveConfig();

            ENCHANTMENT_STATES_CACHE = new HashMap<>(enchantmentStates);

            return getEnchantmentStates().equals(enchantmentStates);
        }

        /**
         * Anvil-related settings for disenchantment operations.
         */
        public static class Anvil {
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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

                    return getCostMultiplier().equals(multiplier);
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
            plugin.saveConfig();

            return isEnabled() == enabled;
        }

        /**
         * Gets the list of worlds where shatterment is disabled.
         *
         * @return list of disabled {@link World} instances
         */
        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).toList());
        }

        /**
         * Sets the list of worlds where shatterment is disabled and persists the change.
         *
         * @param worlds the worlds to disable shatterment in
         * @return {@code true} if the persisted value matches the requested list
         */
        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            plugin.saveConfig();

            return getDisabledWorlds().equals(worlds);
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

            List<Enchantment> enchantments = EnchantmentUtils.getRegisteredEnchantments();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String key = split[0];
                EnchantmentStateType state = EnchantmentStateType.getStateByName(split[1]);

                enchantmentStates.put(
                        key,
                        state
                );
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
            plugin.saveConfig();

            ENCHANTMENT_STATES_CACHE = new HashMap<>(enchantmentStates);

            return getEnchantmentStates().equals(enchantmentStates);
        }

        /**
         * Anvil-related settings for shatterment operations.
         */
        public static class Anvil {
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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

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
                    plugin.saveConfig();

                    return getCostMultiplier().equals(multiplier);
                }
            }
        }
    }
}
