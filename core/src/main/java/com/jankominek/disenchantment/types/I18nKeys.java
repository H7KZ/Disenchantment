package com.jankominek.disenchantment.types;

/**
 * Enumerates all internationalization (i18n) key paths used in the plugin's
 * locale configuration files. Each constant maps to a dot-separated YAML path
 * for accessing localized strings including messages, command output, GUI labels,
 * and state display names.
 */
public enum I18nKeys {
    // i18n messages
    PREFIX("prefix"),

    MESSAGES_REQUIRES_PERMISSION("messages.requires-permission"),
    MESSAGES_INVALID_ARGUMENT("messages.invalid-argument"),
    MESSAGES_UNKNOWN_ENCHANTMENT("messages.unknown-enchantment"),
    MESSAGES_SPECIFY_ENCHANTMENT_STATE("messages.specify-enchantment-state"),
    MESSAGES_ENCHANTMENT_IS_ENABLED("messages.enchantment-is-enabled"),
    MESSAGES_ENCHANTMENT_IS_KEPT("messages.enchantment-is-kept"),
    MESSAGES_ENCHANTMENT_IS_DELETED("messages.enchantment-is-deleted"),
    MESSAGES_ENCHANTMENT_IS_DISABLED("messages.enchantment-is-disabled"),
    MESSAGES_MATERIAL_IS_ENABLED("messages.material-is-enabled"),
    MESSAGES_MATERIAL_IS_DISABLED("messages.material-is-disabled"),
    MESSAGES_SPECIFY_REPAIR_VALUE("messages.specify-repair-value"),
    MESSAGES_SPECIFY_VALID_INTEGER("messages.specify-valid-integer"),
    MESSAGES_SPECIFY_VALID_DOUBLE("messages.specify-valid-double"),
    MESSAGES_REPAIR_COST_IS_ENABLED("messages.repair-cost-is-enabled"),
    MESSAGES_REPAIR_COST_IS_DISABLED("messages.repair-cost-is-disabled"),
    MESSAGES_REPAIR_COST_RESET_IS_ENABLED("messages.repair-cost-reset-is-enabled"),
    MESSAGES_REPAIR_COST_RESET_IS_DISABLED("messages.repair-cost-reset-is-disabled"),
    MESSAGES_REPAIR_BASE_COST_IS_SET("messages.repair-base-cost-is-set"),
    MESSAGES_REPAIR_MULTIPLIER_IS_SET("messages.repair-multiplier-is-set"),
    MESSAGES_SPECIFY_SOUND_STATE("messages.specify-sound-state"),
    MESSAGES_SOUND_IS_ENABLED("messages.sound-is-enabled"),
    MESSAGES_SOUND_IS_DISABLED("messages.sound-is-disabled"),
    MESSAGES_SOUND_VOLUME_IS_SET("messages.sound-volume-is-set"),
    MESSAGES_SOUND_PITCH_IS_SET("messages.sound-pitch-is-set"),
    MESSAGES_WORLD_NOT_FOUND("messages.world-not-found"),
    MESSAGES_WORLD_IS_ENABLED("messages.world-is-enabled"),
    MESSAGES_WORLD_IS_DISABLED("messages.world-is-disabled"),

    STATES_ENABLE("states.enabled"),
    STATES_KEEP("states.keep"),
    STATES_DELETE("states.delete"),
    STATES_DISABLE("states.disable"),

    COMMANDS_HELP_TITLE("commands.help.title"),
    COMMANDS_HELP_PAGES_1("commands.help.pages.1"),
    COMMANDS_HELP_PAGES_2("commands.help.pages.2"),
    COMMANDS_HELP_PAGES_3("commands.help.pages.3"),
    COMMANDS_HELP_PAGES_4("commands.help.pages.4"),
    COMMANDS_HELP_PAGES_5("commands.help.pages.5"),
    COMMANDS_HELP_PAGES_6("commands.help.pages.6"),
    COMMANDS_HELP_PAGES_7("commands.help.pages.7"),
    COMMANDS_HELP_PAGES_8("commands.help.pages.8"),

    COMMANDS_STATUS_TITLE("commands.status.title"),
    COMMANDS_STATUS_GLOBAL("commands.status.global"),
    COMMANDS_STATUS_DISENCHANTMENT("commands.status.disenchantment"),
    COMMANDS_STATUS_SHATTERMENT("commands.status.shatterment"),
    COMMANDS_STATUS_STATES_ENABLED("commands.status.states.enabled"),
    COMMANDS_STATUS_STATES_DISABLED("commands.status.states.disabled"),

    COMMANDS_ENCHANTMENTS_DISENCHANTMENT_TITLE("commands.enchantments.disenchantment.title"),
    COMMANDS_ENCHANTMENTS_DISENCHANTMENT_EMPTY("commands.enchantments.disenchantment.empty"),
    COMMANDS_ENCHANTMENTS_DISENCHANTMENT_ENCHANTMENT("commands.enchantments.disenchantment.enchantment"),
    COMMANDS_ENCHANTMENTS_SHATTERMENT_TITLE("commands.enchantments.shatterment.title"),
    COMMANDS_ENCHANTMENTS_SHATTERMENT_EMPTY("commands.enchantments.shatterment.empty"),
    COMMANDS_ENCHANTMENTS_SHATTERMENT_ENCHANTMENT("commands.enchantments.shatterment.enchantment"),

    COMMANDS_MATERIALS_TITLE("commands.materials.title"),
    COMMANDS_MATERIALS_EMPTY("commands.materials.empty"),
    COMMANDS_MATERIALS_MATERIAL("commands.materials.material"),
    COMMANDS_MATERIALS_STATES_DISABLED("commands.materials.states.disabled"),

    COMMANDS_REPAIR_DISENCHANTMENT_TITLE("commands.repair.disenchantment.title"),
    COMMANDS_REPAIR_DISENCHANTMENT_RESET("commands.repair.disenchantment.reset"),
    COMMANDS_REPAIR_DISENCHANTMENT_COST("commands.repair.disenchantment.cost"),
    COMMANDS_REPAIR_DISENCHANTMENT_BASE("commands.repair.disenchantment.base"),
    COMMANDS_REPAIR_DISENCHANTMENT_MULTIPLY("commands.repair.disenchantment.multiply"),
    COMMANDS_REPAIR_DISENCHANTMENT_STATES_ENABLED("commands.repair.disenchantment.states.enabled"),
    COMMANDS_REPAIR_DISENCHANTMENT_STATES_DISABLED("commands.repair.disenchantment.states.disabled"),

    COMMANDS_REPAIR_SHATTERMENT_TITLE("commands.repair.shatterment.title"),
    COMMANDS_REPAIR_SHATTERMENT_RESET("commands.repair.shatterment.reset"),
    COMMANDS_REPAIR_SHATTERMENT_COST("commands.repair.shatterment.cost"),
    COMMANDS_REPAIR_SHATTERMENT_BASE("commands.repair.shatterment.base"),
    COMMANDS_REPAIR_SHATTERMENT_MULTIPLY("commands.repair.shatterment.multiply"),
    COMMANDS_REPAIR_SHATTERMENT_STATES_ENABLED("commands.repair.shatterment.states.enabled"),
    COMMANDS_REPAIR_SHATTERMENT_STATES_DISABLED("commands.repair.shatterment.states.disabled"),

    COMMANDS_SOUND_DISENCHANTMENT_TITLE("commands.sound.disenchantment.title"),
    COMMANDS_SOUND_DISENCHANTMENT_SOUND("commands.sound.disenchantment.sound"),
    COMMANDS_SOUND_DISENCHANTMENT_VOLUME("commands.sound.disenchantment.volume"),
    COMMANDS_SOUND_DISENCHANTMENT_PITCH("commands.sound.disenchantment.pitch"),
    COMMANDS_SOUND_DISENCHANTMENT_STATES_ENABLED("commands.sound.disenchantment.states.enabled"),
    COMMANDS_SOUND_DISENCHANTMENT_STATES_DISABLED("commands.sound.disenchantment.states.disabled"),

    COMMANDS_SOUND_SHATTERMENT_TITLE("commands.sound.shatterment.title"),
    COMMANDS_SOUND_SHATTERMENT_SOUND("commands.sound.shatterment.sound"),
    COMMANDS_SOUND_SHATTERMENT_VOLUME("commands.sound.shatterment.volume"),
    COMMANDS_SOUND_SHATTERMENT_PITCH("commands.sound.shatterment.pitch"),
    COMMANDS_SOUND_SHATTERMENT_STATES_ENABLED("commands.sound.shatterment.states.enabled"),
    COMMANDS_SOUND_SHATTERMENT_STATES_DISABLED("commands.sound.shatterment.states.disabled"),

    COMMANDS_WORLDS_DISENCHANTMENT_TITLE("commands.worlds.disenchantment.title"),
    COMMANDS_WORLDS_DISENCHANTMENT_EMPTY("commands.worlds.disenchantment.empty"),
    COMMANDS_WORLDS_DISENCHANTMENT_WORLD("commands.worlds.disenchantment.world"),
    COMMANDS_WORLDS_DISENCHANTMENT_STATES_DISABLED("commands.worlds.disenchantment.states.disabled"),

    COMMANDS_WORLDS_SHATTERMENT_TITLE("commands.worlds.shatterment.title"),
    COMMANDS_WORLDS_SHATTERMENT_EMPTY("commands.worlds.shatterment.empty"),
    COMMANDS_WORLDS_SHATTERMENT_WORLD("commands.worlds.shatterment.world"),
    COMMANDS_WORLDS_SHATTERMENT_STATES_DISABLED("commands.worlds.shatterment.states.disabled"),

    GUI_BACK("gui.back"),
    GUI_PREVIOUS("gui.previous"),
    GUI_NEXT("gui.next"),

    GUI_NAVIGATION_INVENTORY("gui.navigation.inventory"),
    GUI_NAVIGATION_PLUGIN_TITLE("gui.navigation.plugin.title"),
    GUI_NAVIGATION_PLUGIN_LORE_ENABLED("gui.navigation.plugin.lore.enabled"),
    GUI_NAVIGATION_PLUGIN_LORE_DISABLED("gui.navigation.plugin.lore.disabled"),
    GUI_NAVIGATION_WORLDS_TITLE("gui.navigation.worlds.title"),
    GUI_NAVIGATION_WORLDS_LORE("gui.navigation.worlds.lore"),
    GUI_NAVIGATION_REPAIR_TITLE("gui.navigation.repair.title"),
    GUI_NAVIGATION_REPAIR_LORE("gui.navigation.repair.lore"),
    GUI_NAVIGATION_ENCHANTMENTS_TITLE("gui.navigation.enchantments.title"),
    GUI_NAVIGATION_ENCHANTMENTS_LORE("gui.navigation.enchantments.lore"),
    GUI_NAVIGATION_MATERIALS_TITLE("gui.navigation.materials.title"),
    GUI_NAVIGATION_MATERIALS_LORE("gui.navigation.materials.lore"),
    GUI_NAVIGATION_SOUND_TITLE("gui.navigation.sound.title"),
    GUI_NAVIGATION_SOUND_LORE("gui.navigation.sound.lore"),
    GUI_NAVIGATION_SPIGOT_TITLE("gui.navigation.spigot.title"),
    GUI_NAVIGATION_SPIGOT_LORE("gui.navigation.spigot.lore"),

    GUI_WORLDS_INVENTORY("gui.worlds.inventory"),
    GUI_WORLDS_LORE_DISENCHANTMENT_ENABLED("gui.worlds.lore.disenchantment.enabled"),
    GUI_WORLDS_LORE_DISENCHANTMENT_DISABLED("gui.worlds.lore.disenchantment.disabled"),
    GUI_WORLDS_LORE_SHATTERMENT_ENABLED("gui.worlds.lore.shatterment.enabled"),
    GUI_WORLDS_LORE_SHATTERMENT_DISABLED("gui.worlds.lore.shatterment.disabled"),
    GUI_WORLDS_HELP_TITLE("gui.worlds.help.title"),
    GUI_WORLDS_HELP_LORE("gui.worlds.help.lore"),

    GUI_REPAIR_DISENCHANTMENT_INVENTORY("gui.repair.disenchantment.inventory"),
    GUI_REPAIR_DISENCHANTMENT_COST_LORE_TITLE("gui.repair.disenchantment.cost.title"),
    GUI_REPAIR_DISENCHANTMENT_COST_LORE_ENABLED("gui.repair.disenchantment.cost.lore.enabled"),
    GUI_REPAIR_DISENCHANTMENT_COST_LORE_DISABLED("gui.repair.disenchantment.cost.lore.disabled"),
    GUI_REPAIR_DISENCHANTMENT_RESET_LORE_TITLE("gui.repair.disenchantment.reset.title"),
    GUI_REPAIR_DISENCHANTMENT_RESET_LORE_ENABLED("gui.repair.disenchantment.reset.lore.enabled"),
    GUI_REPAIR_DISENCHANTMENT_RESET_LORE_DISABLED("gui.repair.disenchantment.reset.lore.disabled"),
    GUI_REPAIR_DISENCHANTMENT_BASE_TITLE("gui.repair.disenchantment.base.title"),
    GUI_REPAIR_DISENCHANTMENT_BASE_LORE("gui.repair.disenchantment.base.lore"),
    GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_TITLE("gui.repair.disenchantment.multiplier.title"),
    GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_LORE("gui.repair.disenchantment.multiplier.lore"),

    GUI_REPAIR_SHATTERMENT_INVENTORY("gui.repair.shatterment.inventory"),
    GUI_REPAIR_SHATTERMENT_COST_LORE_TITLE("gui.repair.shatterment.cost.title"),
    GUI_REPAIR_SHATTERMENT_COST_LORE_ENABLED("gui.repair.shatterment.cost.lore.enabled"),
    GUI_REPAIR_SHATTERMENT_COST_LORE_DISABLED("gui.repair.shatterment.cost.lore.disabled"),
    GUI_REPAIR_SHATTERMENT_RESET_LORE_TITLE("gui.repair.shatterment.reset.title"),
    GUI_REPAIR_SHATTERMENT_RESET_LORE_ENABLED("gui.repair.shatterment.reset.lore.enabled"),
    GUI_REPAIR_SHATTERMENT_RESET_LORE_DISABLED("gui.repair.shatterment.reset.lore.disabled"),
    GUI_REPAIR_SHATTERMENT_BASE_TITLE("gui.repair.shatterment.base.title"),
    GUI_REPAIR_SHATTERMENT_BASE_LORE("gui.repair.shatterment.base.lore"),
    GUI_REPAIR_SHATTERMENT_MULTIPLIER_TITLE("gui.repair.shatterment.multiplier.title"),
    GUI_REPAIR_SHATTERMENT_MULTIPLIER_LORE("gui.repair.shatterment.multiplier.lore"),

    GUI_ENCHANTMENTS_INVENTORY("gui.enchantments.inventory"),
    GUI_ENCHANTMENTS_LORE_DISENCHANTMENT("gui.enchantments.lore.disenchantment"),
    GUI_ENCHANTMENTS_LORE_SHATTERMENT("gui.enchantments.lore.shatterment"),
    GUI_ENCHANTMENTS_HELP_TITLE("gui.enchantments.help.title"),
    GUI_ENCHANTMENTS_HELP_LORE("gui.enchantments.help.lore"),

    GUI_MATERIALS_INVENTORY("gui.materials.inventory"),
    GUI_MATERIALS_LORE_ENABLED("gui.materials.lore.enabled"),
    GUI_MATERIALS_LORE_DISABLED("gui.materials.lore.disabled"),
    GUI_MATERIALS_HELP_TITLE("gui.materials.help.title"),
    GUI_MATERIALS_HELP_LORE("gui.materials.help.lore"),

    GUI_SOUND_DISENCHANTMENT_INVENTORY("gui.sound.disenchantment.inventory"),
    GUI_SOUND_DISENCHANTMENT_TITLE("gui.sound.disenchantment.title"),
    GUI_SOUND_DISENCHANTMENT_LORE_ENABLED("gui.sound.disenchantment.lore.enabled"),
    GUI_SOUND_DISENCHANTMENT_LORE_DISABLED("gui.sound.disenchantment.lore.disabled"),
    GUI_SOUND_DISENCHANTMENT_VOLUME_TITLE("gui.sound.disenchantment.volume.title"),
    GUI_SOUND_DISENCHANTMENT_VOLUME_LORE("gui.sound.disenchantment.volume.lore"),
    GUI_SOUND_DISENCHANTMENT_PITCH_TITLE("gui.sound.disenchantment.pitch.title"),
    GUI_SOUND_DISENCHANTMENT_PITCH_LORE("gui.sound.disenchantment.pitch.lore"),

    GUI_SOUND_SHATTERMENT_INVENTORY("gui.sound.shatterment.inventory"),
    GUI_SOUND_SHATTERMENT_TITLE("gui.sound.shatterment.title"),
    GUI_SOUND_SHATTERMENT_LORE_ENABLED("gui.sound.shatterment.lore.enabled"),
    GUI_SOUND_SHATTERMENT_LORE_DISABLED("gui.sound.shatterment.lore.disabled"),
    GUI_SOUND_SHATTERMENT_VOLUME_TITLE("gui.sound.shatterment.volume.title"),
    GUI_SOUND_SHATTERMENT_VOLUME_LORE("gui.sound.shatterment.volume.lore"),
    GUI_SOUND_SHATTERMENT_PITCH_TITLE("gui.sound.shatterment.pitch.title"),
    GUI_SOUND_SHATTERMENT_PITCH_LORE("gui.sound.shatterment.pitch.lore"),
    ;

    private final String key;

    I18nKeys(String key) {
        this.key = key;
    }

    /**
     * Gets the dot-separated YAML path for this i18n key.
     *
     * @return the i18n key path
     */
    public String getKey() {
        return key;
    }
}
