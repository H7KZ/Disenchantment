package com.jankominek.disenchantment.types;

import org.bukkit.ChatColor;

public enum EnchantmentStateType {
    ENABLED(ChatColor.GREEN + "Enabled", null),
    KEEP(ChatColor.GOLD + "Keep", "keep"),
    DELETE(ChatColor.YELLOW + "Delete", "delete"),
    DISABLED(ChatColor.RED + "Disabled", "disabled"),
    ;

    private final String displayName;
    private final String configName;

    EnchantmentStateType(String displayName, String configName) {
        this.displayName = displayName;
        this.configName = configName;
    }

    public static EnchantmentStateType getStateByName(String name) {
        if (name == null) return null;

        return switch (name.toLowerCase()) {
            case "enabled" -> ENABLED;
            case "keep" -> KEEP;
            case "delete" -> DELETE;
            case "disabled" -> DISABLED;

            default -> null;
        };
    }

    public static EnchantmentStateType getNextState(EnchantmentStateType lastStatus) {
        if (lastStatus == null) return ENABLED;

        return switch (lastStatus) {
            case ENABLED -> KEEP;
            case KEEP -> DELETE;
            case DELETE -> DISABLED;
            case DISABLED -> ENABLED;
        };
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigName() {
        return configName;
    }
}
