package cz.kominekjan.disenchantment.config;

import cz.kominekjan.disenchantment.types.EnchantmentState;
import cz.kominekjan.disenchantment.types.LoggingLevel;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;

public class Config {
    public static Boolean isPluginEnabled() {
        return config.getBoolean(ConfigKeys.ENABLED.getKey());
    }

    public static Boolean setPluginEnabled(Boolean enabled) {
        config.set(ConfigKeys.ENABLED.getKey(), enabled);
        plugin.saveConfig();

        return isPluginEnabled() == enabled;
    }

    public static Boolean isLoggingEnabled() {
        return config.getBoolean(ConfigKeys.ENABLE_LOGGING.getKey());
    }

    public static Boolean setLoggingEnabled(Boolean enabled) {
        config.set(ConfigKeys.ENABLE_LOGGING.getKey(), enabled);
        plugin.saveConfig();

        return isLoggingEnabled() == enabled;
    }

    public static LoggingLevel getLoggingLevel() {
        return LoggingLevel.valueOf(config.getString(ConfigKeys.LOGGING_LEVEL.getKey()));
    }

    public static Boolean setLoggingLevel(LoggingLevel level) {
        config.set(ConfigKeys.LOGGING_LEVEL.getKey(), level.name());
        plugin.saveConfig();

        return getLoggingLevel() == level;
    }

    public static class Disenchantment {
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

        public static HashMap<Enchantment, EnchantmentState> getEnchantmentStates() {
            List<String> list = config.getStringList(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES.getKey());
            HashMap<Enchantment, EnchantmentState> enchantmentStates = new HashMap<>();

            List<Enchantment> enchantments = new ArrayList<>(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().toList());

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String enchantment = split[0];
                EnchantmentState state = EnchantmentState.getStateByName(split[1]);

                enchantmentStates.put(
                        enchantments.stream().filter(e -> e.getKey().getKey().equalsIgnoreCase(enchantment)).findFirst().orElse(null),
                        state
                );
            }

            return enchantmentStates;
        }

        public static Boolean setEnchantmentStates(HashMap<Enchantment, EnchantmentState> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<Enchantment, EnchantmentState> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().getKey().getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
            }

            config.set(ConfigKeys.DISENCHANTMENT_ENCHANTMENTS_STATES.getKey(), list);
            plugin.saveConfig();

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

        public static HashMap<Enchantment, EnchantmentState> getEnchantmentStates() {
            List<String> list = config.getStringList(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES.getKey());
            HashMap<Enchantment, EnchantmentState> enchantmentStates = new HashMap<>();

            List<Enchantment> enchantments = new ArrayList<>(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().toList());

            for (String enchantmentState : list) {
                String[] split = enchantmentState.split(":");

                if (split.length != 2) continue;

                String enchantment = split[0];
                EnchantmentState state = EnchantmentState.getStateByName(split[1]);

                enchantmentStates.put(
                        enchantments.stream().filter(e -> e.getKey().getKey().equalsIgnoreCase(enchantment)).findFirst().orElse(null),
                        state
                );
            }

            return enchantmentStates;
        }

        public static Boolean setEnchantmentStates(HashMap<Enchantment, EnchantmentState> enchantmentStates) {
            List<String> list = new ArrayList<>();

            for (Map.Entry<Enchantment, EnchantmentState> entry : enchantmentStates.entrySet()) {
                list.add(entry.getKey().getKey().getKey().toLowerCase() + ":" + entry.getValue().getConfigName().toLowerCase());
            }

            config.set(ConfigKeys.SHATTERMENT_ENCHANTMENTS_STATES.getKey(), list);
            plugin.saveConfig();

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
