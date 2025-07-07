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

public class Config {
    public static boolean isPluginEnabled() {
        return config.getBoolean(ConfigKeys.ENABLED.getKey());
    }

    public static boolean setPluginEnabled(boolean enabled) {
        config.set(ConfigKeys.ENABLED.getKey(), enabled);
        plugin.saveConfig();

        return isPluginEnabled() == enabled;
    }

    public static List<String> getLocales() {
        return config.getStringList(ConfigKeys.LOCALES.getKey());
    }

    public static String getLocale() {
        return config.getString(ConfigKeys.LOCALE.getKey());
    }

    public static class EventPriorities {
        public static EventPriority getDisenchantEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_DISENCHANT.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }

        public static EventPriority getDisenchantClickEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_DISENCHANT_CLICK.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }

        public static EventPriority getShatterEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_SHATTER.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }

        public static EventPriority getShatterClickEvent() {
            try {
                return EventPriority.valueOf(config.getString(ConfigKeys.EVENT_PRIORITIES_SHATTER_CLICK.getKey(), "HIGHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                return EventPriority.HIGHEST;
            }
        }
    }

    public static class Disenchantment {
        private static HashMap<String, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;

        public static boolean isEnabled() {
            return config.getBoolean(ConfigKeys.DISENCHANTMENT_ENABLED.getKey());
        }

        public static boolean setEnabled(boolean enabled) {
            config.set(ConfigKeys.DISENCHANTMENT_ENABLED.getKey(), enabled);
            plugin.saveConfig();

            return isEnabled() == enabled;
        }

        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).toList());
        }

        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            plugin.saveConfig();

            return getDisabledWorlds().equals(worlds);
        }

        public static List<Material> getDisabledMaterials() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey()).stream().map(Material::getMaterial).toList());
        }

        public static boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey(), materials.stream().map(Material::name).toList());
            plugin.saveConfig();

            return getDisabledMaterials().equals(materials);
        }

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

        public static class Anvil {
            public static class Sound {
                public static boolean isEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED.getKey());
                }

                public static boolean setEnabled(boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isEnabled() == enabled;
                }

                public static Double getVolume() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey());
                }

                public static boolean setVolume(double volume) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey(), volume);
                    plugin.saveConfig();

                    return getVolume().equals(volume);
                }

                public static Double getPitch() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey());
                }

                public static boolean setPitch(double pitch) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey(), pitch);
                    plugin.saveConfig();

                    return getPitch().equals(pitch);
                }
            }

            public static class Repair {
                public static boolean isResetEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED.getKey());
                }

                public static boolean setResetEnabled(boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isResetEnabled() == enabled;
                }

                public static boolean isCostEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED.getKey());
                }

                public static boolean setCostEnabled(boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isCostEnabled() == enabled;
                }

                public static Double getBaseCost() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE.getKey());
                }

                public static boolean setBaseCost(double cost) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE.getKey(), cost);
                    plugin.saveConfig();

                    return getBaseCost().equals(cost);
                }

                public static Double getCostMultiplier() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER.getKey());
                }

                public static boolean setCostMultiplier(double multiplier) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER.getKey(), multiplier);
                    plugin.saveConfig();

                    return getCostMultiplier().equals(multiplier);
                }
            }
        }
    }

    public static class Shatterment {
        private static HashMap<String, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;

        public static boolean isEnabled() {
            return config.getBoolean(ConfigKeys.SHATTERMENT_ENABLED.getKey());
        }

        public static boolean setEnabled(boolean enabled) {
            config.set(ConfigKeys.SHATTERMENT_ENABLED.getKey(), enabled);
            plugin.saveConfig();

            return isEnabled() == enabled;
        }

        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).toList());
        }

        public static boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            plugin.saveConfig();

            return getDisabledWorlds().equals(worlds);
        }

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

        public static class Anvil {
            public static class Sound {
                public static boolean isEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED.getKey());
                }

                public static boolean setEnabled(boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isEnabled() == enabled;
                }

                public static Double getVolume() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey());
                }

                public static boolean setVolume(double volume) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey(), volume);
                    plugin.saveConfig();

                    return getVolume().equals(volume);
                }

                public static Double getPitch() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey());
                }

                public static boolean setPitch(double pitch) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey(), pitch);
                    plugin.saveConfig();

                    return getPitch().equals(pitch);
                }
            }

            public static class Repair {
                public static boolean isResetEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED.getKey());
                }

                public static boolean setResetEnabled(boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isResetEnabled() == enabled;
                }

                public static boolean isCostEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED.getKey());
                }

                public static boolean setCostEnabled(boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isCostEnabled() == enabled;
                }

                public static Double getBaseCost() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE.getKey());
                }

                public static boolean setBaseCost(double cost) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE.getKey(), cost);
                    plugin.saveConfig();

                    return getBaseCost().equals(cost);
                }

                public static Double getCostMultiplier() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER.getKey());
                }

                public static boolean setCostMultiplier(double multiplier) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER.getKey(), multiplier);
                    plugin.saveConfig();

                    return getCostMultiplier().equals(multiplier);
                }
            }
        }
    }
}
