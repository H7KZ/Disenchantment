package com.jankominek.disenchantment.types;

import com.jankominek.disenchantment.config.I18n;

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
            case ENABLED -> I18n.States.enabled();
            case KEEP -> I18n.States.keep();
            case DELETE -> I18n.States.delete();
            case DISABLED -> I18n.States.disabled();
        };
    }

    public String getConfigName() {
        return configName;
    }
}
