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

    UPDATE_CHECKER("update-checker"),

    LOCALE("locale"),

    // Logging settings
    LOGGING_LEVEL("logging.level"),
    LOGGING_SAVE_REPORTS("logging.save-reports"),
    LOGGING_OPERATIONS("logging.operations"),
    LOGGING_OPERATIONS_RETENTION_DAYS("logging.operations-retention-days"),
    LOGGING_DISCORD_WEBHOOK("logging.discord-webhook"),

    // Event priority settings
    EVENT_PRIORITIES_DISENCHANT_CLICK("event-priorities.disenchant-click"),
    EVENT_PRIORITIES_DISENCHANT("event-priorities.disenchant"),
    EVENT_PRIORITIES_SHATTER_CLICK("event-priorities.shatter-click"),
    EVENT_PRIORITIES_SHATTER("event-priorities.shatter"),

    // Disenchantment settings
    DISENCHANTMENT_ENABLED("disenchantment.enabled"),
    DISENCHANTMENT_WORLDS_MODE("disenchantment.worlds.mode"),
    DISENCHANTMENT_WORLDS_LIST("disenchantment.worlds.list"),
    DISENCHANTMENT_MATERIALS_MODE("disenchantment.materials.mode"),
    DISENCHANTMENT_MATERIALS_LIST("disenchantment.materials.list"),
    DISENCHANTMENT_ENCHANTMENTS_STATES("disenchantment.enchantments-states"),

    DISENCHANTMENT_ANVIL_SOUND_ENABLED("disenchantment.anvil.sound.enabled"),
    DISENCHANTMENT_ANVIL_VOLUME("disenchantment.anvil.sound.volume"),
    DISENCHANTMENT_ANVIL_PITCH("disenchantment.anvil.sound.pitch"),

    DISENCHANTMENT_REPAIR_RESET_ENABLED("disenchantment.anvil.repair.reset"),
    DISENCHANTMENT_REPAIR_COST_ENABLED("disenchantment.anvil.repair.cost"),
    DISENCHANTMENT_REPAIR_COST_BASE("disenchantment.anvil.repair.base"),
    DISENCHANTMENT_REPAIR_COST_MULTIPLIER("disenchantment.anvil.repair.multiply"),
    DISENCHANTMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS("disenchantment.anvil.repair.enchantment-costs"),
    DISENCHANTMENT_REPAIR_MAX_COST("disenchantment.anvil.repair.max-cost"),
    DISENCHANTMENT_ANVIL_BYPASS_TOO_EXPENSIVE("disenchantment.anvil.bypass-too-expensive"),

    DISENCHANTMENT_ENCHANTMENT_CHANCES("disenchantment.enchantment-chances"),

    DISENCHANTMENT_ECONOMY_ENABLED("disenchantment.economy.enabled"),
    DISENCHANTMENT_ECONOMY_COST("disenchantment.economy.cost"),
    DISENCHANTMENT_ECONOMY_SHOW_COST("disenchantment.economy.show-cost"),
    DISENCHANTMENT_ECONOMY_CHARGE_MESSAGE("disenchantment.economy.charge-message"),

    // Cost multiplier settings (LuckPerms meta / permission-node discount)
    COST_MULTIPLIER_LUCKPERMS("disenchantment.cost-multiplier.luckperms"),

    // Cooldown settings
    COOLDOWN_SECONDS("disenchantment.cooldown.seconds"),
    COOLDOWN_USE_CMI("disenchantment.cooldown.use-cmi"),
    COOLDOWN_USE_ESSENTIALS("disenchantment.cooldown.use-essentials"),

    // Shatterment settings
    SHATTERMENT_ENABLED("shatterment.enabled"),
    SHATTERMENT_WORLDS_MODE("shatterment.worlds.mode"),
    SHATTERMENT_WORLDS_LIST("shatterment.worlds.list"),
    SHATTERMENT_MATERIALS_MODE("shatterment.materials.mode"),
    SHATTERMENT_MATERIALS_LIST("shatterment.materials.list"),
    SHATTERMENT_ENCHANTMENTS_STATES("shatterment.enchantments-states"),

    SHATTERMENT_ANVIL_SOUND_ENABLED("shatterment.anvil.sound.enabled"),
    SHATTERMENT_ANVIL_VOLUME("shatterment.anvil.sound.volume"),
    SHATTERMENT_ANVIL_PITCH("shatterment.anvil.sound.pitch"),

    SHATTERMENT_REPAIR_RESET_ENABLED("shatterment.anvil.repair.reset"),
    SHATTERMENT_REPAIR_COST_ENABLED("shatterment.anvil.repair.cost"),
    SHATTERMENT_REPAIR_COST_BASE("shatterment.anvil.repair.base"),
    SHATTERMENT_REPAIR_COST_MULTIPLIER("shatterment.anvil.repair.multiply"),
    SHATTERMENT_ANVIL_REPAIR_ENCHANTMENT_COSTS("shatterment.anvil.repair.enchantment-costs"),
    SHATTERMENT_REPAIR_MAX_COST("shatterment.anvil.repair.max-cost"),
    SHATTERMENT_ANVIL_BYPASS_TOO_EXPENSIVE("shatterment.anvil.bypass-too-expensive"),

    SHATTERMENT_ENCHANTMENT_CHANCES("shatterment.enchantment-chances"),

    SHATTERMENT_ECONOMY_ENABLED("shatterment.economy.enabled"),
    SHATTERMENT_ECONOMY_COST("shatterment.economy.cost"),
    SHATTERMENT_ECONOMY_SHOW_COST("shatterment.economy.show-cost"),
    SHATTERMENT_ECONOMY_CHARGE_MESSAGE("shatterment.economy.charge-message"),

    SHATTERMENT_SPLIT_COUNT("shatterment.split-count"),

    // Shared settings
    ENCHANTMENT_GROUPS("enchantment-groups"),

    // Grindstone settings (extract + transfer)
    EVENT_PRIORITIES_GRINDSTONE("event-priorities.grindstone"),
    EVENT_PRIORITIES_GRINDSTONE_CLICK("event-priorities.grindstone-click"),

    GRINDSTONE_ENABLED("grindstone.enabled"),
    GRINDSTONE_CURSES("grindstone.curses"),

    GRINDSTONE_SOUND_ENABLED("grindstone.sound.enabled"),
    GRINDSTONE_SOUND_VOLUME("grindstone.sound.volume"),
    GRINDSTONE_SOUND_PITCH("grindstone.sound.pitch"),

    GRINDSTONE_REPAIR_RESET_ENABLED("grindstone.repair.reset"),
    GRINDSTONE_REPAIR_COST_ENABLED("grindstone.repair.cost"),
    GRINDSTONE_REPAIR_COST_BASE("grindstone.repair.base"),
    GRINDSTONE_REPAIR_COST_MULTIPLIER("grindstone.repair.multiply"),
    GRINDSTONE_REPAIR_ENCHANTMENT_COSTS("grindstone.repair.enchantment-costs"),
    GRINDSTONE_REPAIR_MAX_COST("grindstone.repair.max-cost"),

    GRINDSTONE_ECONOMY_ENABLED("grindstone.economy.enabled"),
    GRINDSTONE_ECONOMY_COST("grindstone.economy.cost"),
    GRINDSTONE_ECONOMY_SHOW_COST("grindstone.economy.show-cost"),
    GRINDSTONE_ECONOMY_CHARGE_MESSAGE("grindstone.economy.charge-message"),

    GRINDSTONE_TRANSFER_ENABLED("grindstone.transfer.enabled"),
    GRINDSTONE_TRANSFER_ALLOW_OVER_MAX("grindstone.transfer.allow-over-max"),
    GRINDSTONE_TRANSFER_MAX_ENCHANTMENTS("grindstone.transfer.max-enchantments"),

    GRINDSTONE_TRANSFER_REPAIR_RESET_ENABLED("grindstone.transfer.repair.reset"),
    GRINDSTONE_TRANSFER_REPAIR_COST_ENABLED("grindstone.transfer.repair.cost"),
    GRINDSTONE_TRANSFER_REPAIR_COST_BASE("grindstone.transfer.repair.base"),
    GRINDSTONE_TRANSFER_REPAIR_COST_MULTIPLIER("grindstone.transfer.repair.multiply"),
    GRINDSTONE_TRANSFER_REPAIR_ENCHANTMENT_COSTS("grindstone.transfer.repair.enchantment-costs"),
    GRINDSTONE_TRANSFER_REPAIR_MAX_COST("grindstone.transfer.repair.max-cost"),

    GRINDSTONE_TRANSFER_ECONOMY_ENABLED("grindstone.transfer.economy.enabled"),
    GRINDSTONE_TRANSFER_ECONOMY_COST("grindstone.transfer.economy.cost"),
    GRINDSTONE_TRANSFER_ECONOMY_SHOW_COST("grindstone.transfer.economy.show-cost"),
    GRINDSTONE_TRANSFER_ECONOMY_CHARGE_MESSAGE("grindstone.transfer.economy.charge-message"),
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
