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

    // Feedback settings (particles, failure sound, refusal messages, preview lore)
    FEEDBACK_PARTICLES_ENABLED("feedback.particles.enabled"),
    FEEDBACK_PARTICLES_TYPES("feedback.particles.types"),
    FEEDBACK_PARTICLES_ERASE_TYPES("feedback.particles.erase-types"),
    FEEDBACK_PARTICLES_COUNT("feedback.particles.count"),
    FEEDBACK_PARTICLES_SPREAD("feedback.particles.spread"),
    FEEDBACK_PARTICLES_SCOPE("feedback.particles.scope"),
    FEEDBACK_FAILURE_SOUND_ENABLED("feedback.failure-sound.enabled"),
    FEEDBACK_FAILURE_SOUND_SOUND("feedback.failure-sound.sound"),
    FEEDBACK_FAILURE_SOUND_VOLUME("feedback.failure-sound.volume"),
    FEEDBACK_FAILURE_SOUND_PITCH("feedback.failure-sound.pitch"),
    FEEDBACK_NOT_ENOUGH_XP_MESSAGE("feedback.not-enough-xp-message"),
    FEEDBACK_PREVIEW_LORE("feedback.preview-lore"),

    // Erase mode settings
    EVENT_PRIORITIES_ERASE_CLICK("event-priorities.erase-click"),
    EVENT_PRIORITIES_ERASE("event-priorities.erase"),

    ERASE_ENABLED("erase.enabled"),
    ERASE_CATALYSTS("erase.catalysts"),
    ERASE_CONSUME_CATALYST("erase.consume-catalyst"),
    ERASE_ERASE_CURSES("erase.erase-curses"),

    ERASE_ANVIL_SOUND_ENABLED("erase.anvil.sound.enabled"),
    ERASE_ANVIL_VOLUME("erase.anvil.sound.volume"),
    ERASE_ANVIL_PITCH("erase.anvil.sound.pitch"),

    ERASE_REPAIR_RESET_ENABLED("erase.anvil.repair.reset"),
    ERASE_REPAIR_COST_ENABLED("erase.anvil.repair.cost"),
    ERASE_REPAIR_COST_BASE("erase.anvil.repair.base"),
    ERASE_REPAIR_COST_MULTIPLIER("erase.anvil.repair.multiply"),
    ERASE_ANVIL_REPAIR_ENCHANTMENT_COSTS("erase.anvil.repair.enchantment-costs"),
    ERASE_REPAIR_MAX_COST("erase.anvil.repair.max-cost"),
    ERASE_ANVIL_BYPASS_TOO_EXPENSIVE("erase.anvil.bypass-too-expensive"),

    ERASE_ECONOMY_ENABLED("erase.economy.enabled"),
    ERASE_ECONOMY_COST("erase.economy.cost"),
    ERASE_ECONOMY_SHOW_COST("erase.economy.show-cost"),
    ERASE_ECONOMY_CHARGE_MESSAGE("erase.economy.charge-message"),
    // Special anvils
    SPECIAL_ANVILS_MODE("special-anvils.mode"),
    SPECIAL_ANVILS_BLOCK_BELOW("special-anvils.block-below"),
    SPECIAL_ANVILS_ALLOW_VIRTUAL_ANVILS("special-anvils.allow-virtual-anvils"),
    SPECIAL_ANVILS_DROP_SPECIAL_ITEM("special-anvils.drop-special-item"),
    SPECIAL_ANVILS_HINT_MESSAGE("special-anvils.hint-message"),
    SPECIAL_ANVILS_ITEM_NAME("special-anvils.item.name"),
    SPECIAL_ANVILS_ITEM_LORE("special-anvils.item.lore"),
    SPECIAL_ANVILS_RECIPE_ENABLED("special-anvils.recipe.enabled"),
    SPECIAL_ANVILS_RECIPE_SHAPE("special-anvils.recipe.shape"),
    SPECIAL_ANVILS_RECIPE_INGREDIENTS("special-anvils.recipe.ingredients"),
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
