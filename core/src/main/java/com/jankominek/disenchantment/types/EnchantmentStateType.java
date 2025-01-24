package com.jankominek.disenchantment.types;

public enum EnchantmentStateType {
    ENABLED(null),
    KEEP("keep"),
    DELETE("delete"),
    DISABLED("disabled"),
    ;

    private final String configName;

    EnchantmentStateType(String configName) {
        this.configName = configName;
    }

    public static EnchantmentStateType getStateByName(String name) {
        return switch (name.toLowerCase()) {
            case "enabled" -> ENABLED;
            case "keep" -> KEEP;
            case "delete" -> DELETE;
            case "disabled" -> DISABLED;
            default -> null;
        };
    }

    public static EnchantmentStateType getNextState(EnchantmentStateType lastStatus) {
        return switch (lastStatus) {
            case ENABLED -> KEEP;
            case KEEP -> DELETE;
            case DELETE -> DISABLED;
            case DISABLED -> ENABLED;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case ENABLED -> ConfigKeys.I18N_STATES_ENABLED.getKey();
            case KEEP -> ConfigKeys.I18N_STATES_KEEP.getKey();
            case DELETE -> ConfigKeys.I18N_STATES_DELETE.getKey();
            case DISABLED -> ConfigKeys.I18N_STATES_DISABLED.getKey();
        };
    }

    public String getConfigName() {
        return configName;
    }
}
