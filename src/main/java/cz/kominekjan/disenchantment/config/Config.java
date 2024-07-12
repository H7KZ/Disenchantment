package cz.kominekjan.disenchantment.config;

import cz.kominekjan.disenchantment.types.DisabledEnchantment;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

import static cz.kominekjan.disenchantment.Disenchantment.config;
import static cz.kominekjan.disenchantment.Disenchantment.plugin;

public class Config {
    public static Boolean getPluginEnabled() {
        return config.getBoolean(ConfigKeys.ENABLED.getKey());
    }

    public static Boolean getEnableLogging() {
        return config.getBoolean(ConfigKeys.ENABLE_LOGGING.getKey());
    }

    public static LoggingLevels getLoggingLevel() {
        String level = config.getString(ConfigKeys.LOGGING_LEVEL.getKey());
        return LoggingLevels.valueOf(level);
    }

    public static List<String> getDisabledWorlds() {
        return config.getStringList(ConfigKeys.DISABLED_WORLDS.getKey());
    }

    public static List<Material> getDisabledMaterials() {
        return config.getStringList(ConfigKeys.DISABLED_ITEMS.getKey()).stream().map(Material::getMaterial).toList();
    }

    public static List<DisabledEnchantment> getDisabledEnchantments() {
        List<Map<?, ?>> list = config.getMapList(ConfigKeys.DISABLED_ENCHANTMENTS.getKey());
        return list.stream().map(m -> new DisabledEnchantment((String) m.get("enchantment"), (Boolean) m.get("keep"))).toList();
    }

    public static Boolean getEnableAnvilSound() {
        return config.getBoolean(ConfigKeys.ENABLE_ANVIL_SOUND.getKey());
    }

    public static Integer getAnvilSoundVolume() {
        return config.getInt(ConfigKeys.ANVIL_VOLUME.getKey());
    }

    public static Float getAnvilSoundPitch() {
        return (float) config.getDouble(ConfigKeys.ANVIL_PITCH.getKey());
    }

    public static Boolean getEnableRepairReset() {
        return config.getBoolean(ConfigKeys.ENABLE_REPAIR_RESET.getKey());
    }

    public static Boolean getEnableRepairCost() {
        return config.getBoolean(ConfigKeys.ENABLE_REPAIR_COST.getKey());
    }

    public static Integer getBaseRepairCost() {
        return config.getInt(ConfigKeys.BASE_REPAIR_COST.getKey());
    }

    public static Double getRepairCostMultiplier() {
        return config.getDouble(ConfigKeys.COST_MULTIPLIER.getKey());
    }

    public static Boolean setPluginEnabled(Boolean enabled) {
        config.set(ConfigKeys.ENABLED.getKey(), enabled);
        plugin.saveConfig();

        return getPluginEnabled() == enabled;
    }

    public static Boolean setDisabledWorlds(List<String> worlds) {
        config.set(ConfigKeys.DISABLED_WORLDS.getKey(), worlds);
        plugin.saveConfig();

        return getDisabledWorlds().equals(worlds);
    }

    public static Boolean setDisabledMaterials(List<Material> materials) {
        config.set(ConfigKeys.DISABLED_ITEMS.getKey(), materials.stream().map(Material::name).toList());
        plugin.saveConfig();

        return getDisabledMaterials().equals(materials);
    }

    public static Boolean setDisabledEnchantments(List<DisabledEnchantment> enchantments) {
        config.set(ConfigKeys.DISABLED_ENCHANTMENTS.getKey(), enchantments.stream().map(e -> Map.of("enchantment", e.getEnchantmentKey(), "keep", e.doKeep())).toList());
        plugin.saveConfig();

        return getDisabledEnchantments().equals(enchantments);
    }

    public static Boolean setEnableAnvilSound(Boolean enabled) {
        config.set(ConfigKeys.ENABLE_ANVIL_SOUND.getKey(), enabled);
        plugin.saveConfig();

        return getEnableAnvilSound() == enabled;
    }

    public static Boolean setAnvilSoundVolume(Integer volume) {
        config.set(ConfigKeys.ANVIL_VOLUME.getKey(), volume);
        plugin.saveConfig();

        return config.getDouble(ConfigKeys.ANVIL_VOLUME.getKey()) == volume;
    }

    public static Boolean setAnvilSoundPitch(Float pitch) {
        config.set(ConfigKeys.ANVIL_PITCH.getKey(), pitch);
        plugin.saveConfig();

        return config.getDouble(ConfigKeys.ANVIL_PITCH.getKey()) == pitch;
    }

    public static Boolean setEnableRepairReset(Boolean enabled) {
        config.set(ConfigKeys.ENABLE_REPAIR_RESET.getKey(), enabled);
        plugin.saveConfig();

        return getEnableRepairReset() == enabled;
    }

    public static Boolean setEnableRepairCost(Boolean enabled) {
        config.set(ConfigKeys.ENABLE_REPAIR_COST.getKey(), enabled);
        plugin.saveConfig();

        return getEnableRepairCost() == enabled;
    }

    public static Boolean setBaseRepairCost(Integer cost) {
        config.set(ConfigKeys.BASE_REPAIR_COST.getKey(), cost);
        plugin.saveConfig();

        return config.getInt(ConfigKeys.BASE_REPAIR_COST.getKey()) == cost;
    }

    public static Boolean setRepairCostMultiplier(Double multiplier) {
        config.set(ConfigKeys.COST_MULTIPLIER.getKey(), multiplier);
        plugin.saveConfig();

        return config.getDouble(ConfigKeys.COST_MULTIPLIER.getKey()) == multiplier;
    }

    private enum ConfigKeys {
        ENABLED("enabled"),
        ENABLE_LOGGING("enable-logging"),
        LOGGING_LEVEL("logging-level"),
        DISABLED_WORLDS("disabled-worlds"),
        DISABLED_ITEMS("disabled-items"),
        DISABLED_ENCHANTMENTS("disabled-enchantments"),
        ENABLE_ANVIL_SOUND("enable-anvil-sound"),
        ANVIL_VOLUME("anvil-volume"),
        ANVIL_PITCH("anvil-pitch"),
        ENABLE_REPAIR_RESET("enable-repair-reset"),
        ENABLE_REPAIR_COST("enable-repair-cost"),
        BASE_REPAIR_COST("base"),
        COST_MULTIPLIER("multiply");

        private final String key;

        ConfigKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum LoggingLevels {
        INFO("INFO"),
        DEBUG("DEBUG");

        private final String level;

        LoggingLevels(String level) {
            this.level = level;
        }

        public String getLevel() {
            return level;
        }
    }
}
