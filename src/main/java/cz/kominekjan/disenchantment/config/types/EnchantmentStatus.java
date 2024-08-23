package cz.kominekjan.disenchantment.config.types;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public enum EnchantmentStatus {
    ENABLED(ChatColor.GREEN + "Enabled", null),
    KEEP(ChatColor.GOLD + "Keep", "keep"),
    DISABLED(ChatColor.RED + "Disabled", "disabled"),
    ;

    private final String displayName;
    private final String configName;

    EnchantmentStatus(String displayName, String configName) {
        this.displayName = displayName;
        this.configName = configName;
    }

    @Nullable
    public static EnchantmentStatus getStatusByName(@Nullable String name) {
        if (name == null) return null;

        return switch (name.toLowerCase()) {
            case "enabled" -> ENABLED;
            case "keep" -> KEEP;
            case "disabled" -> DISABLED;

            default -> null;
        };
    }

    @NotNull
    public static EnchantmentStatus getNextStatus(@Nullable EnchantmentStatus lastStatus) {
        if (lastStatus == null) return ENABLED;

        return switch (lastStatus) {
            case ENABLED -> KEEP;
            case KEEP -> DISABLED;
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
