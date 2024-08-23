package cz.kominekjan.disenchantment.config;

public enum ConfigKeys {
    // Current config keys
    ENABLED("enabled"),

    ENABLE_LOGGING("enable-logging"),
    LOGGING_LEVEL("logging-level"),

    // Disenchantment settings
    DISABLE_DISENCHANTMENT("disable-disenchantment"),
    DISABLED_DISENCHANTMENT_WORLDS("disabled-disenchantment-worlds"),
    DISABLED_DISENCHANTMENT_MATERIALS("disabled-disenchantment-materials"),
    DISENCHANTMENT_ENCHANTMENTS_STATUS("disenchantment-enchantments-status"),

    DISENCHANTMENT_ENABLE_ANVIL_SOUND("disenchantment-enable-anvil-sound"),
    DISENCHANTMENT_ANVIL_VOLUME("disenchantment-anvil-volume"),
    DISENCHANTMENT_ANVIL_PITCH("disenchantment-anvil-pitch"),

    DISENCHANTMENT_ENABLE_REPAIR_RESET("disenchantment-enable-repair-reset"),
    DISENCHANTMENT_ENABLE_REPAIR_COST("disenchantment-enable-repair-cost"),
    DISENCHANTMENT_BASE_REPAIR_COST("disenchantment-base"),
    DISENCHANTMENT_COST_MULTIPLIER("disenchantment-multiply"),

    // Shatterment settings
    DISABLE_SHATTERMENT("disable-shatterment"),
    DISABLED_SHATTERMENT_WORLDS("disabled-shatterment-worlds"),
    SHATTERMENT_ENCHANTMENTS_STATUS("shatterment-enchantments-status"),

    SHATTERMENT_ENABLE_ANVIL_SOUND("shatterment-enable-anvil-sound"),
    SHATTERMENT_ANVIL_VOLUME("shatterment-anvil-volume"),
    SHATTERMENT_ANVIL_PITCH("shatterment-anvil-pitch"),

    SHATTERMENT_ENABLE_REPAIR_RESET("shatterment-enable-repair-reset"),
    SHATTERMENT_ENABLE_REPAIR_COST("shatterment-enable-repair-cost"),
    SHATTERMENT_BASE_REPAIR_COST("shatterment-base"),
    SHATTERMENT_COST_MULTIPLIER("shatterment-multiply"),


    // Old config keys
    // @deleted (DO NOT USE, only for migrations)
    DISABLED_WORLDS("disabled-worlds"),
    DISABLED_ITEMS("disabled-materials"),
    DISABLED_ENCHANTMENTS("disabled-enchantments"),

    DISABLE_BOOK_SPLITTING("disable-book-splitting"),
    DISABLED_BOOK_SPLITTING_WORLDS("disabled-book-splitting-worlds"),
    DISABLED_BOOK_SPLITTING_ENCHANTMENTS("disabled-book-splitting-enchantments"),

    ENABLE_ANVIL_SOUND("enable-anvil-sound"),
    ANVIL_VOLUME("anvil-volume"),
    ANVIL_PITCH("anvil-pitch"),
    ENABLE_REPAIR_RESET("enable-repair-reset"),
    ENABLE_REPAIR_COST("enable-repair-cost"),
    BASE_REPAIR_COST("base"),
    COST_MULTIPLIER("multiply"),
    ;

    private final String key;

    ConfigKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
