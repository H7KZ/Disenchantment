package com.jankominek.disenchantment.guis;

import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIComponent {
    public static ItemStack back() {
        return new ItemBuilder(Material.NETHER_STAR)
                .setDisplayName(I18n.GUI.back())
                .addAllFlags()
                .build();
    }

    public static ItemStack previous() {
        return new ItemBuilder(Material.FIREWORK_STAR)
                .setDisplayName(I18n.GUI.previous())
                .addAllFlags()
                .build();
    }

    public static ItemStack next() {
        return new ItemBuilder(Material.FIREWORK_STAR)
                .setDisplayName(I18n.GUI.next())
                .addAllFlags()
                .build();
    }

    public static class Navigation {
        public static ItemStack worlds() {
            return new ItemBuilder(Material.FILLED_MAP)
                    .setDisplayName(I18n.GUI.Navigation.Worlds.title())
                    .setLore(I18n.GUI.Navigation.Worlds.lore())
                    .addAllFlags()
                    .build();
        }

        public static ItemStack repair() {
            return new ItemBuilder(Material.NETHERITE_SCRAP)
                    .setDisplayName(I18n.GUI.Navigation.Repair.title())
                    .setLore(I18n.GUI.Navigation.Repair.lore())
                    .addAllFlags()
                    .build();
        }

        public static ItemStack enchantments() {
            return new ItemBuilder(Material.ENCHANTING_TABLE)
                    .setDisplayName(I18n.GUI.Navigation.Enchantments.title())
                    .setLore(I18n.GUI.Navigation.Enchantments.lore())
                    .addAllFlags()
                    .build();
        }

        public static ItemStack materials() {
            return new ItemBuilder(Material.ITEM_FRAME)
                    .setDisplayName(I18n.GUI.Navigation.Materials.title())
                    .setLore(I18n.GUI.Navigation.Materials.lore())
                    .addAllFlags()
                    .build();
        }

        public static ItemStack sound() {
            return new ItemBuilder(Material.JUKEBOX)
                    .setDisplayName(I18n.GUI.Navigation.Sound.title())
                    .setLore(I18n.GUI.Navigation.Sound.lore())
                    .addAllFlags()
                    .build();
        }

        public static ItemStack spigot() {
            return new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                    .setDisplayName(I18n.GUI.Navigation.Spigot.title())
                    .setLore(I18n.GUI.Navigation.Spigot.lore())
                    .addAllFlags()
                    .build();
        }

        public static class Plugin {
            public static ItemStack enabled() {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Navigation.Plugin.title())
                        .setLore(I18n.GUI.Navigation.Plugin.Lore.enabled())
                        .addAllFlags()
                        .build();
            }

            public static ItemStack disabled() {
                return new ItemBuilder(Material.RED_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Navigation.Plugin.title())
                        .setLore(I18n.GUI.Navigation.Plugin.Lore.disabled())
                        .addAllFlags()
                        .build();
            }
        }
    }

    public static class Worlds {
        public static ItemStack worldByEnvironment(World.Environment environment, String name, boolean disenchantment, boolean shatterment) {
            List<String> lore = new ArrayList<>();

            if (disenchantment) lore.add(I18n.GUI.Worlds.Lore.Disenchantment.enabled());
            else lore.add(I18n.GUI.Worlds.Lore.Disenchantment.disabled());

            if (shatterment) lore.add(I18n.GUI.Worlds.Lore.Shatterment.enabled());
            else lore.add(I18n.GUI.Worlds.Lore.Shatterment.disabled());

            return switch (environment) {
                case NORMAL ->
                        world("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWVhYTlhYzE1NzU4ZDUxNzdhODk2NjA1OTg1ZTk4YmVhYzhmZWUwZTZiMmM2OGE4ZGMxZjNjOTFjMDc5ZmI4OSJ9fX0=", name, lore);
                case NETHER ->
                        world("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkzYmZjNDMxOTAwNzIzZjdmYTI4Nzg2NDk2MzgwMTdjZTYxNWQ4ZDhjYWI4ZDJmMDcwYTYxZWIxYWEwMGQwMiJ9fX0=", name, lore);
                case THE_END ->
                        world("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFlYTI3YWE5M2M5NDRjNDYzMDJiZjRlYTkyMjg4NWZlZWE3MDlmNDFjMWRmNzYxNjMzMmViMjQ2ZjkwZDM4ZSJ9fX0=", name, lore);
                default ->
                        world("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=", name, lore);
            };
        }

        private static ItemStack world(String texture, String name, List<String> lore) {
            return new HeadBuilder()
                    .setTexture(texture)
                    .setDisplayName(name)
                    .setLore(lore)
                    .addAllFlags()
                    .build();
        }

        public static ItemStack help() {
            return new ItemBuilder(Material.MAP)
                    .setDisplayName(I18n.GUI.Worlds.Help.title())
                    .setLore(I18n.GUI.Worlds.Help.lore())
                    .addAllFlags()
                    .build();
        }
    }

    public static class Repair {
        public static class Disenchantment {
            public static class Cost {
                public static ItemStack disabled() {
                    return new ItemBuilder(Material.RED_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.title())
                            .setLore(I18n.GUI.Repair.Disenchantment.Lore.disabled())
                            .addAllFlags()
                            .build();
                }

                public static ItemStack enabled() {
                    return new ItemBuilder(Material.LIME_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.title())
                            .setLore(I18n.GUI.Repair.Disenchantment.Lore.enabled())
                            .addAllFlags()
                            .build();
                }

                public static ItemStack base(int cost, int amount) {
                    return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                            .setAmount(amount)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.Base.title())
                            .setLore(
                                    I18n.GUI.Repair.Disenchantment.Base.lore()
                                            .stream().map(s -> s.replace("{cost}", String.valueOf(cost))).toList()
                            )
                            .addAllFlags()
                            .build();
                }

                public static ItemStack multiplier(double cost, int amount) {
                    return new ItemBuilder(Material.DRAGON_BREATH)
                            .setAmount(amount)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.Multiplier.title())
                            .setLore(
                                    I18n.GUI.Repair.Disenchantment.Multiplier.lore()
                                            .stream().map(s -> s.replace("{multiplier}", String.valueOf(cost))).toList()
                            )
                            .addAllFlags()
                            .build();
                }
            }
        }

        public static class Shatterment {
            public static class Cost {
                public static ItemStack disabled() {
                    return new ItemBuilder(Material.RED_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.title())
                            .setLore(I18n.GUI.Repair.Shatterment.Lore.disabled())
                            .addAllFlags()
                            .build();
                }

                public static ItemStack enabled() {
                    return new ItemBuilder(Material.LIME_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.title())
                            .setLore(I18n.GUI.Repair.Shatterment.Lore.enabled())
                            .addAllFlags()
                            .build();
                }

                public static ItemStack base(int cost, int amount) {
                    return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                            .setAmount(amount)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.Base.title())
                            .setLore(
                                    I18n.GUI.Repair.Shatterment.Base.lore()
                                            .stream().map(s -> s.replace("{cost}", String.valueOf(cost))).toList()
                            )
                            .addAllFlags()
                            .build();
                }

                public static ItemStack multiplier(double cost, int amount) {
                    return new ItemBuilder(Material.DRAGON_BREATH)
                            .setAmount(amount)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.Multiplier.title())
                            .setLore(
                                    I18n.GUI.Repair.Shatterment.Multiplier.lore()
                                            .stream().map(s -> s.replace("{multiplier}", String.valueOf(cost))).toList()
                            )
                            .addAllFlags()
                            .build();
                }
            }
        }
    }

    public static class Enchantments {
        public static ItemStack enchantment(String name, EnchantmentStateType disenchantment, EnchantmentStateType shatterment) {
            List<String> lore = new ArrayList<>();

            lore.add(I18n.GUI.Enchantments.Lore.disenchantment().replace("{state}", disenchantment.getDisplayName()));
            lore.add(I18n.GUI.Enchantments.Lore.shatterment().replace("{state}", shatterment.getDisplayName()));

            return new ItemBuilder(Material.ENCHANTED_BOOK)
                    .setDisplayName(name)
                    .setLore(lore)
                    .addAllFlags()
                    .build();
        }

        public static ItemStack help() {
            return new ItemBuilder(Material.MAP)
                    .setDisplayName(I18n.GUI.Enchantments.Help.title())
                    .setLore(I18n.GUI.Enchantments.Help.lore())
                    .addAllFlags()
                    .build();
        }
    }

    public static class Materials {
        public static ItemStack material(Material material, boolean disenchantment, boolean shatterment) {
            List<String> lore = new ArrayList<>();

            if (disenchantment) lore.add(I18n.GUI.Materials.Lore.Disenchantment.enabled());
            else lore.add(I18n.GUI.Materials.Lore.Disenchantment.disabled());

            if (shatterment) lore.add(I18n.GUI.Materials.Lore.Shatterment.enabled());
            else lore.add(I18n.GUI.Materials.Lore.Shatterment.disabled());

            return new ItemBuilder(material)
                    .setDisplayName(ChatColor.GRAY + material.getKey().getKey())
                    .setLore(lore)
                    .addAllFlags()
                    .build();
        }

        public static ItemStack help() {
            return new ItemBuilder(Material.MAP)
                    .setDisplayName(I18n.GUI.Materials.Help.title())
                    .setLore(I18n.GUI.Materials.Help.lore())
                    .addAllFlags()
                    .build();
        }
    }

    public static class Sound {
        public static class Disenchantment {
            public static ItemStack disabled() {
                return new ItemBuilder(Material.RED_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.title())
                        .setLore(I18n.GUI.Sound.Disenchantment.Lore.disabled())
                        .addAllFlags()
                        .build();
            }

            public static ItemStack enabled() {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.title())
                        .setLore(I18n.GUI.Sound.Disenchantment.Lore.enabled())
                        .addAllFlags()
                        .build();
            }

            public static ItemStack volume(double volume, int amount) {
                return new ItemBuilder(Material.FIRE_CHARGE)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.Volume.title())
                        .setLore(
                                I18n.GUI.Sound.Disenchantment.Volume.lore()
                                        .stream().map(s -> s.replace("{volume}", String.valueOf(volume))).toList()
                        )
                        .addAllFlags()
                        .build();
            }

            public static ItemStack pitch(double pitch, int amount) {
                return new ItemBuilder(Material.SLIME_BALL)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.Pitch.title())
                        .setLore(
                                I18n.GUI.Sound.Disenchantment.Pitch.lore()
                                        .stream().map(s -> s.replace("{pitch}", String.valueOf(pitch))).toList()
                        )
                        .addAllFlags()
                        .build();
            }
        }

        public static class Shatterment {
            public static ItemStack disabled() {
                return new ItemBuilder(Material.RED_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.title())
                        .setLore(I18n.GUI.Sound.Shatterment.Lore.disabled())
                        .addAllFlags()
                        .build();
            }

            public static ItemStack enabled() {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.title())
                        .setLore(I18n.GUI.Sound.Shatterment.Lore.enabled())
                        .addAllFlags()
                        .build();
            }

            public static ItemStack volume(double volume, int amount) {
                return new ItemBuilder(Material.FIRE_CHARGE)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.Volume.title())
                        .setLore(
                                I18n.GUI.Sound.Shatterment.Volume.lore()
                                        .stream().map(s -> s.replace("{volume}", String.valueOf(volume))).toList()
                        )
                        .addAllFlags()
                        .build();
            }

            public static ItemStack pitch(double pitch, int amount) {
                return new ItemBuilder(Material.SLIME_BALL)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.Pitch.title())
                        .setLore(
                                I18n.GUI.Sound.Shatterment.Pitch.lore()
                                        .stream().map(s -> s.replace("{pitch}", String.valueOf(pitch))).toList()
                        )
                        .addAllFlags()
                        .build();
            }
        }
    }
}
