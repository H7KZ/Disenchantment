package cz.kominekjan.disenchantment.plugins.impl.squared;

import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.set.RegistryKeySet;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

// Dummy object to wrap a squared enchantment in an enchantment instance.
@SuppressWarnings("removal")
public class SquarredWrappedEnchantment extends Enchantment {
    private final CustomEnchant enchantment;
    private final NamespacedKey key;

    SquarredWrappedEnchantment(CustomEnchant enchantment) {
        this.enchantment = enchantment;
        this.key = new NamespacedKey("enchantmentsquarred", enchantment.getType().toLowerCase());
    }

    public CustomEnchant getEnchantment() {
        return enchantment;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }

    // We do not use bellow methods so we do not care about what we return.

    @Override
    public @NotNull String getName() {
        return "";
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public @NotNull Component displayName(int level) {
        return null;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMinModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getAnvilCost() {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlotGroup> getActiveSlotGroups() {
        return Set.of();
    }

    @Override
    public @NotNull Component description() {
        return null;
    }

    @Override
    public @NotNull RegistryKeySet<ItemType> getSupportedItems() {
        return null;
    }

    @Override
    public @Nullable RegistryKeySet<ItemType> getPrimaryItems() {
        return null;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public @NotNull RegistryKeySet<Enchantment> getExclusiveWith() {
        return null;
    }

    @Override // deprecated for suppression not a real issue are not using this function.
    public @NotNull EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override // deprecated for suppression not a real issue are not using this function.
    public @NotNull EnchantmentRarity getRarity() {
        return null;
    }

    @Override // deprecated for suppression not a real issue are not using this function.
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override // deprecated for suppression not a real issue are not using this function.
    public float getDamageIncrease(int level, @NotNull EntityType entityType) {
        return 0;
    }

    @Override // deprecated for suppression not a real issue are not using this function.
    public @NotNull String translationKey() {
        return "";
    }

    @Override // deprecated for suppression not a real issue are not using this function.
    public @NotNull String getTranslationKey() {
        return "";
    }
}
