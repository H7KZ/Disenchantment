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

    public static class Disenchantment {
        private static HashMap<Enchantment, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;

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

        public static HashMap<Enchantment, EnchantmentStateType> getEnchantmentStates() {
            if (ENCHANTMENT_STATES_CACHE != null) return ENCHANTMENT_STATES_CACHE;

            List<String> list = config.getStringList(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES.getKey());
            HashMap<Enchantment, EnchantmentStateType> enchantmentStates = new HashMap<>();

            List<Enchantment> enchantments = EnchantmentUtils.getRegisteredEnchantments();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String enchantmentName = split[0];
                EnchantmentStateType state = EnchantmentStateType.getStateByName(split[1]);

                Enchantment enchantment = enchantments.stream()
                        .filter(e -> e.getKey().getKey().equalsIgnoreCase(enchantmentName))
                        .findFirst().orElse(null);

                // It is possible that the user miss-wrote an enchantment if he edited config.yml by hand.
                if (enchantment == null) continue;

                enchantmentStates.put(
                        enchantment,
                        state
                );
            }

            ENCHANTMENT_STATES_CACHE = enchantmentStates;

            return enchantmentStates;
        }

        public static boolean setEnchantmentStates(HashMap<Enchantment, EnchantmentStateType> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().getKey().getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
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
        private static HashMap<Enchantment, EnchantmentStateType> ENCHANTMENT_STATES_CACHE = null;

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

        public static HashMap<Enchantment, EnchantmentStateType> getEnchantmentStates() {
            if (ENCHANTMENT_STATES_CACHE != null) return ENCHANTMENT_STATES_CACHE;

            List<String> list = config.getStringList(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES.getKey());
            HashMap<Enchantment, EnchantmentStateType> enchantmentStates = new HashMap<>();

            List<Enchantment> enchantments = EnchantmentUtils.getRegisteredEnchantments();

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String enchantmentName = split[0];
                EnchantmentStateType state = EnchantmentStateType.getStateByName(split[1]);

                Enchantment enchantment = enchantments.stream()
                        .filter(e -> e.getKey().getKey().equalsIgnoreCase(enchantmentName))
                        .findFirst().orElse(null);

                // It is possible that the user miss-wrote an enchantment if he edited config.yml by hand.
                if (enchantment == null) continue;

                enchantmentStates.put(
                        enchantment,
                        state
                );
            }

            ENCHANTMENT_STATES_CACHE = enchantmentStates;

            return enchantmentStates;
        }

        public static boolean setEnchantmentStates(HashMap<Enchantment, EnchantmentStateType> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().getKey().getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
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

    public static EventPriority getDisenchantClickEventPriority() {
        return EventPriority.valueOf(config.getString(ConfigKeys.PRIORITY_DISENCHANT_CLICK_EVENT.getKey(), "HIGHEST").toUpperCase());
    }

    public static boolean setDisenchantClickEventPriority(EventPriority priority) {
        config.set(ConfigKeys.PRIORITY_DISENCHANT_CLICK_EVENT.getKey(), priority.name());
        plugin.saveConfig();

        return getDisenchantClickEventPriority() == priority;
    }

    public static EventPriority getDisenchantEventPriority() {
        return EventPriority.valueOf(config.getString(ConfigKeys.PRIORITY_DISENCHANT_EVENT.getKey(), "HIGHEST").toUpperCase());
    }

    public static boolean setDisenchantEventPriority(EventPriority priority) {
        config.set(ConfigKeys.PRIORITY_DISENCHANT_EVENT.getKey(), priority.name());
        plugin.saveConfig();

        return getDisenchantEventPriority() == priority;
    }

    public static EventPriority getGuiClickEventPriority() {
        return EventPriority.valueOf(config.getString(ConfigKeys.PRIORITY_GUI_CLICK_EVENT.getKey(), "NORMAL").toUpperCase());
    }

    public static boolean setGuiClickEventPriority(EventPriority priority) {
        config.set(ConfigKeys.PRIORITY_GUI_CLICK_EVENT.getKey(), priority.name());
        plugin.saveConfig();

        return getGuiClickEventPriority() == priority;
    }

    public static EventPriority getShatterClickEventPriority() {
        return EventPriority.valueOf(config.getString(ConfigKeys.PRIORITY_SHATTER_CLICK_EVENT.getKey(), "HIGHEST").toUpperCase());
    }

    public static boolean setShatterClickEventPriority(EventPriority priority) {
        config.set(ConfigKeys.PRIORITY_SHATTER_CLICK_EVENT.getKey(), priority.name());
        plugin.saveConfig();

        return getShatterClickEventPriority() == priority;
    }

    public static EventPriority getShatterEventPriority() {
        return EventPriority.valueOf(config.getString(ConfigKeys.PRIORITY_SHATTER_EVENT.getKey(), "HIGHEST").toUpperCase());
    }

    public static boolean setShatterEventPriority(EventPriority priority) {
        config.set(ConfigKeys.PRIORITY_SHATTER_EVENT.getKey(), priority.name());
        plugin.saveConfig();

        return getShatterEventPriority() == priority;
    }
}
