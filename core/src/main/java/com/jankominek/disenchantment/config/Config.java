package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.EnchantmentStateType;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.config;
import static com.jankominek.disenchantment.Disenchantment.plugin;

public class Config {
    public static Boolean isPluginEnabled() {
        return config.getBoolean(ConfigKeys.ENABLED.getKey());
    }

    public static Boolean setPluginEnabled(Boolean enabled) {
        config.set(ConfigKeys.ENABLED.getKey(), enabled);
        plugin.saveConfig();

        return isPluginEnabled() == enabled;
    }

    public static class Disenchantment {
        private static HashMap<Enchantment, EnchantmentStateType> STATE_CACHE = null;

        public static Boolean isEnabled() {
            return config.getBoolean(ConfigKeys.DISENCHANTMENT_ENABLED.getKey());
        }

        public static Boolean setEnabled(Boolean enabled) {
            config.set(ConfigKeys.DISENCHANTMENT_ENABLED.getKey(), enabled);
            plugin.saveConfig();

            return isEnabled() == enabled;
        }

        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).toList());
        }

        public static Boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            plugin.saveConfig();

            return getDisabledWorlds().equals(worlds);
        }

        public static List<Material> getDisabledMaterials() {
            return new ArrayList<>(config.getStringList(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey()).stream().map(Material::getMaterial).toList());
        }

        public static Boolean setDisabledMaterials(List<Material> materials) {
            config.set(ConfigKeys.DISENCHANTMENT_DISABLED_MATERIALS.getKey(), materials.stream().map(Material::name).toList());
            plugin.saveConfig();

            return getDisabledMaterials().equals(materials);
        }

        public static HashMap<Enchantment, EnchantmentStateType> getEnchantmentStates() {
            if (STATE_CACHE != null) return STATE_CACHE;

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

            STATE_CACHE = enchantmentStates;

            return enchantmentStates;
        }

        public static Boolean setEnchantmentStates(HashMap<Enchantment, EnchantmentStateType> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().getKey().getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
            }

            config.set(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES.getKey(), list);
            plugin.saveConfig();

            STATE_CACHE = new HashMap<>(enchantmentStates);

            return getEnchantmentStates().equals(enchantmentStates);
        }

        public static class Anvil {
            public static class Sound {
                public static Boolean isEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED.getKey());
                }

                public static Boolean setEnabled(Boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_SOUND_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isEnabled() == enabled;
                }

                public static Double getVolume() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey());
                }

                public static Boolean setVolume(Double volume) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_VOLUME.getKey(), volume);
                    plugin.saveConfig();

                    return getVolume().equals(volume);
                }

                public static Double getPitch() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey());
                }

                public static Boolean setPitch(Double pitch) {
                    config.set(ConfigKeys.DISENCHANTMENT_ANVIL_PITCH.getKey(), pitch);
                    plugin.saveConfig();

                    return getPitch().equals(pitch);
                }
            }

            public static class Repair {
                public static Boolean isResetEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED.getKey());
                }

                public static Boolean setResetEnabled(Boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_RESET_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isResetEnabled() == enabled;
                }

                public static Boolean isCostEnabled() {
                    return config.getBoolean(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED.getKey());
                }

                public static Boolean setCostEnabled(Boolean enabled) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isCostEnabled() == enabled;
                }

                public static Double getBaseCost() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE.getKey());
                }

                public static Boolean setBaseCost(Double cost) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_BASE.getKey(), cost);
                    plugin.saveConfig();

                    return getBaseCost().equals(cost);
                }

                public static Double getCostMultiplier() {
                    return config.getDouble(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER.getKey());
                }

                public static Boolean setCostMultiplier(Double multiplier) {
                    config.set(ConfigKeys.DISENCHANTMENT_REPAIR_COST_MULTIPLIER.getKey(), multiplier);
                    plugin.saveConfig();

                    return getCostMultiplier().equals(multiplier);
                }
            }
        }
    }

    public static class Shatterment {
        private static HashMap<Enchantment, EnchantmentStateType> STATE_CACHE = null;

        public static Boolean isEnabled() {
            return config.getBoolean(ConfigKeys.SHATTERMENT_ENABLED.getKey());
        }

        public static Boolean setEnabled(Boolean enabled) {
            config.set(ConfigKeys.SHATTERMENT_ENABLED.getKey(), enabled);
            plugin.saveConfig();

            return isEnabled() == enabled;
        }

        public static List<World> getDisabledWorlds() {
            return new ArrayList<>(config.getStringList(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey()).stream().map(Bukkit::getWorld).toList());
        }

        public static Boolean setDisabledWorlds(List<World> worlds) {
            config.set(ConfigKeys.SHATTERMENT_DISABLED_WORLDS.getKey(), worlds.stream().map(World::getName).toList());
            plugin.saveConfig();

            return getDisabledWorlds().equals(worlds);
        }

        public static HashMap<Enchantment, EnchantmentStateType> getEnchantmentStates() {
            if (STATE_CACHE != null) return STATE_CACHE;

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

            STATE_CACHE = enchantmentStates;

            return enchantmentStates;
        }

        public static Boolean setEnchantmentStates(HashMap<Enchantment, EnchantmentStateType> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<Enchantment, EnchantmentStateType> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().getKey().getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
            }

            config.set(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES.getKey(), list);
            plugin.saveConfig();

            STATE_CACHE = new HashMap<>(enchantmentStates);

            return getEnchantmentStates().equals(enchantmentStates);
        }

        public static class Anvil {
            public static class Sound {
                public static Boolean isEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED.getKey());
                }

                public static Boolean setEnabled(Boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_SOUND_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isEnabled() == enabled;
                }

                public static Double getVolume() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey());
                }

                public static Boolean setVolume(Double volume) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_VOLUME.getKey(), volume);
                    plugin.saveConfig();

                    return getVolume().equals(volume);
                }

                public static Double getPitch() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey());
                }

                public static Boolean setPitch(Double pitch) {
                    config.set(ConfigKeys.SHATTERMENT_ANVIL_PITCH.getKey(), pitch);
                    plugin.saveConfig();

                    return getPitch().equals(pitch);
                }
            }

            public static class Repair {
                public static Boolean isResetEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED.getKey());
                }

                public static Boolean setResetEnabled(Boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_RESET_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isResetEnabled() == enabled;
                }

                public static Boolean isCostEnabled() {
                    return config.getBoolean(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED.getKey());
                }

                public static Boolean setCostEnabled(Boolean enabled) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_ENABLED.getKey(), enabled);
                    plugin.saveConfig();

                    return isCostEnabled() == enabled;
                }

                public static Double getBaseCost() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE.getKey());
                }

                public static Boolean setBaseCost(Double cost) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_BASE.getKey(), cost);
                    plugin.saveConfig();

                    return getBaseCost().equals(cost);
                }

                public static Double getCostMultiplier() {
                    return config.getDouble(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER.getKey());
                }

                public static Boolean setCostMultiplier(Double multiplier) {
                    config.set(ConfigKeys.SHATTERMENT_REPAIR_COST_MULTIPLIER.getKey(), multiplier);
                    plugin.saveConfig();

                    return getCostMultiplier().equals(multiplier);
                }
            }
        }
    }
}
