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
    I18N_MESSAGES_UNKNOWN_ENCHANTMENT("i18n.messages.unknown-enchantment"),
    I18N_MESSAGES_SPECIFY_ENCHANTMENT_STATE("i18n.messages.specify-enchantment-state"),
    I18N_MESSAGES_ENCHANTMENT_IS_ENABLED("i18n.messages.enchantment-is-enabled"),
    I18N_MESSAGES_ENCHANTMENT_IS_KEPT("i18n.messages.enchantment-is-kept"),
    I18N_MESSAGES_ENCHANTMENT_IS_DELETED("i18n.messages.enchantment-is-deleted"),
    I18N_MESSAGES_ENCHANTMENT_IS_DISABLED("i18n.messages.enchantment-is-disabled"),
    I18N_MESSAGES_MATERIAL_IS_ENABLED("i18n.messages.material-is-enabled"),
    I18N_MESSAGES_MATERIAL_IS_DISABLED("i18n.messages.material-is-disabled"),
    I18N_MESSAGES_SPECIFY_REPAIR_VALUE("i18n.messages.specify-repair-value"),
    I18N_MESSAGES_SPECIFY_VALID_INTEGER("i18n.messages.specify-valid-integer"),
    I18N_MESSAGES_SPECIFY_VALID_DOUBLE("i18n.messages.specify-valid-double"),
    I18N_MESSAGES_REPAIR_COST_IS_ENABLED("i18n.messages.repair-cost-is-enabled"),
    I18N_MESSAGES_REPAIR_COST_IS_DISABLED("i18n.messages.repair-cost-is-disabled"),
    I18N_MESSAGES_REPAIR_COST_RESET_IS_ENABLED("i18n.messages.repair-cost-reset-is-enabled"),
    I18N_MESSAGES_REPAIR_COST_RESET_IS_DISABLED("i18n.messages.repair-cost-reset-is-disabled"),
    I18N_MESSAGES_REPAIR_BASE_COST_IS_SET("i18n.messages.repair-base-cost-is-set"),
    I18N_MESSAGES_REPAIR_MULTIPLIER_IS_SET("i18n.messages.repair-multiplier-is-set"),

    I18N_STATES_ENABLED("i18n.states.enabled"),
    I18N_STATES_KEEP("i18n.states.keep"),
    I18N_STATES_DELETE("i18n.states.delete"),
    I18N_STATES_DISABLE("i18n.states.disable"),

    I18N_COMMANDS_HELP_TITLE("i18n.commands.help.title"),
    I18N_COMMANDS_HELP_PAGES_1("i18n.commands.help.pages.1"),
    I18N_COMMANDS_HELP_PAGES_2("i18n.commands.help.pages.2"),
    I18N_COMMANDS_HELP_PAGES_3("i18n.commands.help.pages.3"),
    I18N_COMMANDS_HELP_PAGES_4("i18n.commands.help.pages.4"),
    I18N_COMMANDS_HELP_PAGES_5("i18n.commands.help.pages.5"),
    I18N_COMMANDS_HELP_PAGES_6("i18n.commands.help.pages.6"),
    I18N_COMMANDS_HELP_PAGES_7("i18n.commands.help.pages.7"),
    I18N_COMMANDS_HELP_PAGES_8("i18n.commands.help.pages.8"),

    I18N_COMMANDS_STATUS_TITLE("i18n.commands.status.title"),
    I18N_COMMANDS_STATUS_GLOBAL("i18n.commands.status.global"),
    I18N_COMMANDS_STATUS_DISENCHANTMENT("i18n.commands.status.disenchantment"),
    I18N_COMMANDS_STATUS_SHATTERMENT("i18n.commands.status.shatterment"),
    I18N_COMMANDS_STATUS_STATES_ENABLED("i18n.commands.status.states.enabled"),
    I18N_COMMANDS_STATUS_STATES_DISABLED("i18n.commands.status.states.disabled"),

    I18N_COMMANDS_ENCHANTMENTS_DISENCHANTMENT_TITLE("i18n.commands.enchantments.disenchantment.title"),
    I18N_COMMANDS_ENCHANTMENTS_DISENCHANTMENT_EMPTY("i18n.commands.enchantments.disenchantment.empty"),
    I18N_COMMANDS_ENCHANTMENTS_DISENCHANTMENT_ENCHANTMENT("i18n.commands.enchantments.disenchantment.enchantment"),
    I18N_COMMANDS_ENCHANTMENTS_SHATTERMENT_TITLE("i18n.commands.enchantments.shatterment.title"),
    I18N_COMMANDS_ENCHANTMENTS_SHATTERMENT_EMPTY("i18n.commands.enchantments.shatterment.empty"),
    I18N_COMMANDS_ENCHANTMENTS_SHATTERMENT_ENCHANTMENT("i18n.commands.enchantments.shatterment.enchantment"),

    I18N_COMMANDS_MATERIALS_TITLE("i18n.commands.materials.title"),
    I18N_COMMANDS_MATERIALS_EMPTY("i18n.commands.materials.empty"),
    I18N_COMMANDS_MATERIALS_MATERIAL("i18n.commands.materials.material"),
    I18N_COMMANDS_MATERIALS_STATES_DISABLED("i18n.commands.materials.states.disabled"),

    I18N_COMMANDS_REPAIR_DISENCHANTMENT_TITLE("i18n.commands.repair.disenchantment.title"),
    I18N_COMMANDS_REPAIR_DISENCHANTMENT_RESET("i18n.commands.repair.disenchantment.reset"),
    I18N_COMMANDS_REPAIR_DISENCHANTMENT_COST("i18n.commands.repair.disenchantment.cost"),
    I18N_COMMANDS_REPAIR_DISENCHANTMENT_BASE("i18n.commands.repair.disenchantment.base"),
    I18N_COMMANDS_REPAIR_DISENCHANTMENT_MULTIPLY("i18n.commands.repair.disenchantment.multiply"),
    I18N_COMMANDS_REPAIR_DISENCHANTMENT_STATES_ENABLED("i18n.commands.repair.disenchantment.states.enabled"),
    I18N_COMMANDS_REPAIR_DISENCHANTMENT_STATES_DISABLED("i18n.commands.repair.disenchantment.states.disabled"),

    I18N_COMMANDS_REPAIR_SHATTERMENT_TITLE("i18n.commands.repair.shatterment.title"),
    I18N_COMMANDS_REPAIR_SHATTERMENT_RESET("i18n.commands.repair.shatterment.reset"),
    I18N_COMMANDS_REPAIR_SHATTERMENT_COST("i18n.commands.repair.shatterment.cost"),
    I18N_COMMANDS_REPAIR_SHATTERMENT_BASE("i18n.commands.repair.shatterment.base"),
    I18N_COMMANDS_REPAIR_SHATTERMENT_MULTIPLY("i18n.commands.repair.shatterment.multiply"),
    I18N_COMMANDS_REPAIR_SHATTERMENT_STATES_ENABLED("i18n.commands.repair.shatterment.states.enabled"),
    I18N_COMMANDS_REPAIR_SHATTERMENT_STATES_DISABLED("i18n.commands.repair.shatterment.states.disabled"),

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
