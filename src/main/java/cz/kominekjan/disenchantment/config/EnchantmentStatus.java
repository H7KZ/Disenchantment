package cz.kominekjan.disenchantment.config;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public enum EnchantmentStatus {
    ENABLED(ChatColor.GREEN, "enabled"),
    KEEP(ChatColor.GOLD, "keep"),
    DISABLED(ChatColor.RED, "disabled"),
    ;

    private final String displayName;
    private final String configName;
    EnchantmentStatus(ChatColor color, String configName) {
        this.displayName = color + configName;
        this.configName = configName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigName() {
        return configName;
    }

    @Nullable
    public static EnchantmentStatus getStatusByName(@NotNull String name){
        return switch (name.toLowerCase()){
            case "enabled" -> ENABLED;
            case "keep" -> KEEP;
            case "disabled" -> DISABLED;

            default -> null;
        };
    }

    @NotNull
    public static EnchantmentStatus getNextStatus(@Nullable EnchantmentStatus lastStatus){
        if(lastStatus == null) return ENABLED;

        return switch (lastStatus){
            case ENABLED -> DISABLED;
            case DISABLED -> KEEP;
            case KEEP -> ENABLED;
        };
    }

}
