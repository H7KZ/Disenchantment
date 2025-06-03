package com.jankominek.disenchantment.types;

import com.jankominek.disenchantment.config.I18n;

import java.util.Objects;

public enum EnchantmentStateType {
    ENABLE(null),
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
            case "enable" -> ENABLE;
            case "keep" -> KEEP;
            case "delete" -> DELETE;
            case "disable" -> DISABLE;
            default -> null;
        };
    }

    public static EnchantmentStateType getNextState(EnchantmentStateType lastStatus) {
        return switch (lastStatus) {
            case ENABLE -> KEEP;
            case KEEP -> DELETE;
            case DELETE -> DISABLE;
            case DISABLE -> ENABLE;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case ENABLE -> I18n.States.enable();
            case KEEP -> I18n.States.keep();
            case DELETE -> I18n.States.delete();
            case DISABLE -> I18n.States.disable();
        };
    }

    public String getConfigName() {
        return Objects.requireNonNullElse(configName, "enable");
    }
}
