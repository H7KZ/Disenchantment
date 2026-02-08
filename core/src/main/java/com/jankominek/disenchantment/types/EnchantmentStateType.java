package com.jankominek.disenchantment.types;

import com.jankominek.disenchantment.config.I18n;

import java.util.Objects;

/**
 * Represents the possible states an enchantment can be configured to during
 * disenchantment or shatterment operations.
 * <ul>
 *   <li>{@link #ENABLE} - enchantment is processed normally (default)</li>
 *   <li>{@link #KEEP} - enchantment is kept on the source item and not transferred</li>
 *   <li>{@link #DELETE} - enchantment is removed from the source item but not transferred</li>
 *   <li>{@link #DISABLE} - enchantment blocks the entire operation</li>
 * </ul>
 */
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

    /**
     * Resolves an {@link EnchantmentStateType} from its configuration name (case-insensitive).
     *
     * @param name the state name to look up
     * @return the matching state, or {@code null} if no match is found
     */
    public static EnchantmentStateType getStateByName(String name) {
        return switch (name.toLowerCase()) {
            case "enable" -> ENABLE;
            case "keep" -> KEEP;
            case "delete" -> DELETE;
            case "disable" -> DISABLE;
            default -> null;
        };
    }

    /**
     * Returns the next state in the cyclic rotation: ENABLE -> KEEP -> DELETE -> DISABLE -> ENABLE.
     *
     * @param lastStatus the current state
     * @return the next state in the cycle
     */
    public static EnchantmentStateType getNextState(EnchantmentStateType lastStatus) {
        return switch (lastStatus) {
            case ENABLE -> KEEP;
            case KEEP -> DELETE;
            case DELETE -> DISABLE;
            case DISABLE -> ENABLE;
        };
    }

    /**
     * Gets the localized display name for this state from the i18n configuration.
     *
     * @return the translated display name
     */
    public String getDisplayName() {
        return switch (this) {
            case ENABLE -> I18n.States.enable();
            case KEEP -> I18n.States.keep();
            case DELETE -> I18n.States.delete();
            case DISABLE -> I18n.States.disable();
        };
    }

    /**
     * Gets the name used to represent this state in the configuration file.
     *
     * @return the configuration name string
     */
    public String getConfigName() {
        return Objects.requireNonNullElse(configName, "enable");
    }
}
