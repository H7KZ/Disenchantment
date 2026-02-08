package com.jankominek.disenchantment.types;

/**
 * Enumerates all configuration key paths used in the plugin's {@code config.yml}.
 * Each constant maps to a dot-separated YAML path for accessing plugin settings
 * including global toggles, event priorities, disenchantment/shatterment options,
 * anvil sound parameters, and repair cost settings.
 */
public enum ConfigKeys {
    LOCALES("locales"),

    ENABLED("enabled"),

    LOCALE("locale"),

    // Event priority settings
    EVENT_PRIORITIES_DISENCHANT_CLICK("event-priorities.disenchant-click"),
    EVENT_PRIORITIES_DISENCHANT("event-priorities.disenchant"),
    EVENT_PRIORITIES_SHATTER_CLICK("event-priorities.shatter-click"),
    EVENT_PRIORITIES_SHATTER("event-priorities.shatter"),

    // Disenchantment settings
    DISENCHANTMENT_ENABLED("disenchantment.enabled"),
    DISENCHANTMENT_DISABLED_WORLDS("disenchantment.disabled-worlds"),
    DISENCHANTMENT_DISABLED_MATERIALS("disenchantment.disabled-materials"),
    DISENCHANTMENT_ENCHANTMENTS_STATES("disenchantment.enchantments-states"),

    DISENCHANTMENT_ANVIL_SOUND_ENABLED("disenchantment.anvil.sound.enabled"),
    DISENCHANTMENT_ANVIL_VOLUME("disenchantment.anvil.sound.volume"),
    DISENCHANTMENT_ANVIL_PITCH("disenchantment.anvil.sound.pitch"),

    DISENCHANTMENT_REPAIR_RESET_ENABLED("disenchantment.anvil.repair.reset"),
    DISENCHANTMENT_REPAIR_COST_ENABLED("disenchantment.anvil.repair.cost"),
    DISENCHANTMENT_REPAIR_COST_BASE("disenchantment.anvil.repair.base"),
    DISENCHANTMENT_REPAIR_COST_MULTIPLIER("disenchantment.anvil.repair.multiply"),

    // Shatterment settings
    SHATTERMENT_ENABLED("shatterment.enabled"),
    SHATTERMENT_DISABLED_WORLDS("shatterment.disabled-worlds"),
    SHATTERMENT_ENCHANTMENTS_STATES("shatterment.enchantments-states"),

    SHATTERMENT_ANVIL_SOUND_ENABLED("shatterment.anvil.sound.enabled"),
    SHATTERMENT_ANVIL_VOLUME("shatterment.anvil.sound.volume"),
    SHATTERMENT_ANVIL_PITCH("shatterment.anvil.sound.pitch"),

    SHATTERMENT_REPAIR_RESET_ENABLED("shatterment.anvil.repair.reset"),
    SHATTERMENT_REPAIR_COST_ENABLED("shatterment.anvil.repair.cost"),
    SHATTERMENT_REPAIR_COST_BASE("shatterment.anvil.repair.base"),
    SHATTERMENT_REPAIR_COST_MULTIPLIER("shatterment.anvil.repair.multiply"),
    ;

    private final String key;

    ConfigKeys(String key) {
        this.key = key;
    }

    /**
     * Gets the dot-separated YAML path for this configuration key.
     *
     * @return the configuration key path
     */
    public String getKey() {
        return key;
    }
}
