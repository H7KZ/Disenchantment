package cz.kominekjan.disenchantment.config;

public class DisabledConfigEnchantment {
    private final String enchantment;
    private final Boolean keep;

    public DisabledConfigEnchantment(String enchantment, Boolean keep) {
        this.enchantment = enchantment;
        this.keep = keep;
    }

    public String getEnchantmentKey() {
        return enchantment;
    }

    public Boolean doKeep() {
        return keep;
    }
}
