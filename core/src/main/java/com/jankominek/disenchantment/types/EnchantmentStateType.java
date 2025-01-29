package com.jankominek.disenchantment.types;

import com.jankominek.disenchantment.config.I18n;

public enum EnchantmentStateType {
    ENABLED(null),
    KEEP("keep"),
    DELETE("delete"),
    DISABLE("disable"),
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
            case "disable" -> DISABLE;
            default -> null;
        };
    }

    public static EnchantmentStateType getNextState(EnchantmentStateType lastStatus) {
        return switch (lastStatus) {
            case ENABLED -> KEEP;
            case KEEP -> DELETE;
            case DELETE -> DISABLE;
            case DISABLE -> ENABLED;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case ENABLED -> I18n.States.enabled();
            case KEEP -> I18n.States.keep();
            case DELETE -> I18n.States.delete();
            case DISABLE -> I18n.States.disable();
        };
    }

    public String getConfigName() {
        return configName;
    }
}
