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
    I18N_PREFIX("i18n.prefix"),

    I18N_MESSAGES_REQUIRES_PERMISSION("i18n.messages.requires-permission"),
    I18N_MESSAGES_INVALID_ARGUMENT("i18n.messages.invalid-argument"),

    I18N_STATES_ENABLED("i18n.states.enabled"),
    I18N_STATES_KEEP("i18n.states.keep"),
    I18N_STATES_DELETE("i18n.states.delete"),
    I18N_STATES_DISABLED("i18n.states.disabled"),

    I18N_GUI_BACK("i18n.gui.back"),
    I18N_GUI_PREVIOUS("i18n.gui.previous"),
    I18N_GUI_NEXT("i18n.gui.next"),

    I18N_GUI_NAVIGATION_INVENTORY("i18n.gui.navigation.inventory"),
    I18N_GUI_NAVIGATION_PLUGIN_TITLE("i18n.gui.navigation.plugin.title"),
    I18N_GUI_NAVIGATION_PLUGIN_LORE_ENABLED("i18n.gui.navigation.plugin.lore.enabled"),
    I18N_GUI_NAVIGATION_PLUGIN_LORE_DISABLED("i18n.gui.navigation.plugin.lore.disabled"),
    I18N_GUI_NAVIGATION_WORLDS_TITLE("i18n.gui.navigation.worlds.title"),
    I18N_GUI_NAVIGATION_WORLDS_LORE("i18n.gui.navigation.worlds.lore"),
    I18N_GUI_NAVIGATION_REPAIR_TITLE("i18n.gui.navigation.repair.title"),
    I18N_GUI_NAVIGATION_REPAIR_LORE("i18n.gui.navigation.repair.lore"),
    I18N_GUI_NAVIGATION_ENCHANTMENTS_TITLE("i18n.gui.navigation.enchantments.title"),
    I18N_GUI_NAVIGATION_ENCHANTMENTS_LORE("i18n.gui.navigation.enchantments.lore"),
    I18N_GUI_NAVIGATION_MATERIALS_TITLE("i18n.gui.navigation.materials.title"),
    I18N_GUI_NAVIGATION_MATERIALS_LORE("i18n.gui.navigation.materials.lore"),
    I18N_GUI_NAVIGATION_SOUND_TITLE("i18n.gui.navigation.sound.title"),
    I18N_GUI_NAVIGATION_SOUND_LORE("i18n.gui.navigation.sound.lore"),
    I18N_GUI_NAVIGATION_SPIGOT_TITLE("i18n.gui.navigation.spigot.title"),
    I18N_GUI_NAVIGATION_SPIGOT_LORE("i18n.gui.navigation.spigot.lore"),

    I18N_GUI_WORLDS_INVENTORY("i18n.gui.worlds.inventory"),
    I18N_GUI_WORLDS_LORE_DISENCHANTMENT_ENABLED("i18n.gui.worlds.lore.disenchantment.enabled"),
    I18N_GUI_WORLDS_LORE_DISENCHANTMENT_DISABLED("i18n.gui.worlds.lore.disenchantment.disabled"),
    I18N_GUI_WORLDS_LORE_SHATTERMENT_ENABLED("i18n.gui.worlds.lore.shatterment.enabled"),
    I18N_GUI_WORLDS_LORE_SHATTERMENT_DISABLED("i18n.gui.worlds.lore.shatterment.disabled"),
    I18N_GUI_WORLDS_HELP_TITLE("i18n.gui.worlds.help.title"),
    I18N_GUI_WORLDS_HELP_LORE("i18n.gui.worlds.help.lore"),

    I18N_GUI_REPAIR_DISENCHANTMENT_INVENTORY("i18n.gui.repair.disenchantment.inventory"),
    I18N_GUI_REPAIR_DISENCHANTMENT_COST_LORE_TITLE("i18n.gui.repair.disenchantment.cost.lore.title"),
    I18N_GUI_REPAIR_DISENCHANTMENT_COST_LORE_ENABLED("i18n.gui.repair.disenchantment.cost.lore.enabled"),
    I18N_GUI_REPAIR_DISENCHANTMENT_COST_LORE_DISABLED("i18n.gui.repair.disenchantment.cost.lore.disabled"),
    I18N_GUI_REPAIR_DISENCHANTMENT_RESET_LORE_TITLE("i18n.gui.repair.disenchantment.reset.lore.title"),
    I18N_GUI_REPAIR_DISENCHANTMENT_RESET_LORE_ENABLED("i18n.gui.repair.disenchantment.reset.lore.enabled"),
    I18N_GUI_REPAIR_DISENCHANTMENT_RESET_LORE_DISABLED("i18n.gui.repair.disenchantment.reset.lore.disabled"),
    I18N_GUI_REPAIR_DISENCHANTMENT_BASE_TITLE("i18n.gui.repair.disenchantment.base.title"),
    I18N_GUI_REPAIR_DISENCHANTMENT_BASE_LORE("i18n.gui.repair.disenchantment.base.lore"),
    I18N_GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_TITLE("i18n.gui.repair.disenchantment.multiplier.title"),
    I18N_GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_LORE("i18n.gui.repair.disenchantment.multiplier.lore"),

    I18N_GUI_REPAIR_SHATTERMENT_INVENTORY("i18n.gui.repair.shatterment.inventory"),
    I18N_GUI_REPAIR_SHATTERMENT_COST_LORE_TITLE("i18n.gui.repair.shatterment.cost.lore.title"),
    I18N_GUI_REPAIR_SHATTERMENT_COST_LORE_ENABLED("i18n.gui.repair.shatterment.cost.lore.enabled"),
    I18N_GUI_REPAIR_SHATTERMENT_COST_LORE_DISABLED("i18n.gui.repair.shatterment.cost.lore.disabled"),
    I18N_GUI_REPAIR_SHATTERMENT_RESET_LORE_TITLE("i18n.gui.repair.shatterment.reset.lore.title"),
    I18N_GUI_REPAIR_SHATTERMENT_RESET_LORE_ENABLED("i18n.gui.repair.shatterment.reset.lore.enabled"),
    I18N_GUI_REPAIR_SHATTERMENT_RESET_LORE_DISABLED("i18n.gui.repair.shatterment.reset.lore.disabled"),
    I18N_GUI_REPAIR_SHATTERMENT_BASE_TITLE("i18n.gui.repair.shatterment.base.title"),
    I18N_GUI_REPAIR_SHATTERMENT_BASE_LORE("i18n.gui.repair.shatterment.base.lore"),
    I18N_GUI_REPAIR_SHATTERMENT_MULTIPLIER_TITLE("i18n.gui.repair.shatterment.multiplier.title"),
    I18N_GUI_REPAIR_SHATTERMENT_MULTIPLIER_LORE("i18n.gui.repair.shatterment.multiplier.lore"),

    I18N_GUI_ENCHANTMENTS_INVENTORY("i18n.gui.enchantments.inventory"),
    I18N_GUI_ENCHANTMENTS_LORE_DISENCHANTMENT("i18n.gui.enchantments.lore.disenchantment"),
    I18N_GUI_ENCHANTMENTS_LORE_SHATTERMENT("i18n.gui.enchantments.lore.shatterment"),
    I18N_GUI_ENCHANTMENTS_HELP_TITLE("i18n.gui.enchantments.help.title"),
    I18N_GUI_ENCHANTMENTS_HELP_LORE("i18n.gui.enchantments.help.lore"),

    I18N_GUI_MATERIALS_INVENTORY("i18n.gui.materials.inventory"),
    I18N_GUI_MATERIALS_LORE_ENABLED("i18n.gui.materials.lore.enabled"),
    I18N_GUI_MATERIALS_LORE_DISABLED("i18n.gui.materials.lore.disabled"),
    I18N_GUI_MATERIALS_HELP_TITLE("i18n.gui.materials.help.title"),
    I18N_GUI_MATERIALS_HELP_LORE("i18n.gui.materials.help.lore"),

    I18N_GUI_SOUND_DISENCHANTMENT_INVENTORY("i18n.gui.sound.disenchantment.inventory"),
    I18N_GUI_SOUND_DISENCHANTMENT_TITLE("i18n.gui.sound.disenchantment.title"),
    I18N_GUI_SOUND_DISENCHANTMENT_LORE_ENABLED("i18n.gui.sound.disenchantment.lore.enabled"),
    I18N_GUI_SOUND_DISENCHANTMENT_LORE_DISABLED("i18n.gui.sound.disenchantment.lore.disabled"),
    I18N_GUI_SOUND_DISENCHANTMENT_VOLUME_TITLE("i18n.gui.sound.disenchantment.volume.title"),
    I18N_GUI_SOUND_DISENCHANTMENT_VOLUME_LORE("i18n.gui.sound.disenchantment.volume.lore"),
    I18N_GUI_SOUND_DISENCHANTMENT_PITCH_TITLE("i18n.gui.sound.disenchantment.pitch.title"),
    I18N_GUI_SOUND_DISENCHANTMENT_PITCH_LORE("i18n.gui.sound.disenchantment.pitch.lore"),

    I18N_GUI_SOUND_SHATTERMENT_INVENTORY("i18n.gui.sound.shatterment.inventory"),
    I18N_GUI_SOUND_SHATTERMENT_TITLE("i18n.gui.sound.shatterment.title"),
    I18N_GUI_SOUND_SHATTERMENT_LORE_ENABLED("i18n.gui.sound.shatterment.lore.enabled"),
    I18N_GUI_SOUND_SHATTERMENT_LORE_DISABLED("i18n.gui.sound.shatterment.lore.disabled"),
    I18N_GUI_SOUND_SHATTERMENT_VOLUME_TITLE("i18n.gui.sound.shatterment.volume.title"),
    I18N_GUI_SOUND_SHATTERMENT_VOLUME_LORE("i18n.gui.sound.shatterment.volume.lore"),
    I18N_GUI_SOUND_SHATTERMENT_PITCH_TITLE("i18n.gui.sound.shatterment.pitch.title"),
    I18N_GUI_SOUND_SHATTERMENT_PITCH_LORE("i18n.gui.sound.shatterment.pitch.lore"),


    ;

    private final String key;

    ConfigKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
