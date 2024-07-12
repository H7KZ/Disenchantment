package cz.kominekjan.disenchantment.types;

public class DisabledEnchantment {
    private final String enchantment;
    private final Boolean keep;

    public DisabledEnchantment(String enchantment, Boolean keep) {
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
