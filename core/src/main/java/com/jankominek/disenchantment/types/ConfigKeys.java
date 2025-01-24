package com.jankominek.disenchantment.types;

public enum ConfigKeys {
    ENABLED("enabled"),

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

    // i18n messages
    I18N_ENABLED("i18n.enabled"),
    I18N_PREFIX("i18n.prefix"),
    I18N_PERMISSION("i18n.permission"),
    I18N_STATES_ENABLED("i18n.states.enabled"),
    I18N_STATES_KEEP("i18n.states.keep"),
    I18N_STATES_DELETE("i18n.states.delete"),
    I18N_STATES_DISABLED("i18n.states.disabled"),
    ;

    private final String key;

    ConfigKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
