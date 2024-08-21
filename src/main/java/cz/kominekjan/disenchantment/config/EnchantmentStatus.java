package cz.kominekjan.disenchantment.config;

import javax.annotation.Nullable;

public enum EnchantmentStatus {
    ENABLED,
    KEEP,
    DISABLED
    ;

    @Nullable
    public static EnchantmentStatus getStatusByName(String name){
        return switch (name.toLowerCase()){
            case "enabled" -> ENABLED;
            case "keep" -> KEEP;
            case "disabled" -> DISABLED;

            default -> null;
        };
    }

}
