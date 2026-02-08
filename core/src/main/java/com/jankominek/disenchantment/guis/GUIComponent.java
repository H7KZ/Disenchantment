package com.jankominek.disenchantment.guis;

import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.EnchantmentStateType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for building localized GUI item stacks used across the plugin's configuration menus.
 * Contains nested static classes for navigation, worlds, repair, enchantments, materials, and sound settings.
 */
public class GUIComponent {
    /**
     * Creates a back-navigation item (Nether Star) for returning to the previous GUI.
     *
     * @return the back navigation {@link ItemStack}
     */
    public static ItemStack back() {
        return new ItemBuilder(Material.NETHER_STAR)
                .setDisplayName(I18n.GUI.back())
                .addAllFlags()
                .build();
    }

    /**
     * Creates a previous-page navigation item (Firework Star).
     *
     * @return the previous page {@link ItemStack}
     */
    public static ItemStack previous() {
        return new ItemBuilder(Material.FIREWORK_STAR)
                .setDisplayName(I18n.GUI.previous())
                .addAllFlags()
                .build();
    }

    /**
     * Creates a next-page navigation item (Firework Star).
     *
     * @return the next page {@link ItemStack}
     */
    public static ItemStack next() {
        return new ItemBuilder(Material.FIREWORK_STAR)
                .setDisplayName(I18n.GUI.next())
                .addAllFlags()
                .build();
    }

    /**
     * Factory methods for items displayed in the main navigation GUI.
     */
    public static class Navigation {
        /**
         * Creates the worlds navigation item.
         *
         * @return the worlds {@link ItemStack}
         */
        public static ItemStack worlds() {
            return new ItemBuilder(Material.FILLED_MAP)
                    .setDisplayName(I18n.GUI.Navigation.Worlds.title())
                    .setLore(I18n.GUI.Navigation.Worlds.lore())
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the repair settings navigation item.
         *
         * @return the repair {@link ItemStack}
         */
        public static ItemStack repair() {
            return new ItemBuilder(Material.NETHERITE_SCRAP)
                    .setDisplayName(I18n.GUI.Navigation.Repair.title())
                    .setLore(I18n.GUI.Navigation.Repair.lore())
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the enchantments settings navigation item.
         *
         * @return the enchantments {@link ItemStack}
         */
        public static ItemStack enchantments() {
            return new ItemBuilder(Material.ENCHANTING_TABLE)
                    .setDisplayName(I18n.GUI.Navigation.Enchantments.title())
                    .setLore(I18n.GUI.Navigation.Enchantments.lore())
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the materials settings navigation item.
         *
         * @return the materials {@link ItemStack}
         */
        public static ItemStack materials() {
            return new ItemBuilder(Material.ITEM_FRAME)
                    .setDisplayName(I18n.GUI.Navigation.Materials.title())
                    .setLore(I18n.GUI.Navigation.Materials.lore())
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the sound settings navigation item.
         *
         * @return the sound {@link ItemStack}
         */
        public static ItemStack sound() {
            return new ItemBuilder(Material.JUKEBOX)
                    .setDisplayName(I18n.GUI.Navigation.Sound.title())
                    .setLore(I18n.GUI.Navigation.Sound.lore())
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the Spigot resource page navigation item.
         *
         * @return the Spigot link {@link ItemStack}
         */
        public static ItemStack spigot() {
            return new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                    .setDisplayName(I18n.GUI.Navigation.Spigot.title())
                    .setLore(I18n.GUI.Navigation.Spigot.lore())
                    .addAllFlags()
                    .build();
        }

        /**
         * Factory methods for the plugin toggle item in navigation, showing enabled/disabled state.
         */
        public static class Plugin {
            /**
             * Creates the plugin-enabled indicator item (lime terracotta).
             *
             * @return the enabled state {@link ItemStack}
             */
            public static ItemStack enabled() {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Navigation.Plugin.title())
                        .setLore(I18n.GUI.Navigation.Plugin.Lore.enabled())
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the plugin-disabled indicator item (red terracotta).
             *
             * @return the disabled state {@link ItemStack}
             */
            public static ItemStack disabled() {
                return new ItemBuilder(Material.RED_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Navigation.Plugin.title())
                        .setLore(I18n.GUI.Navigation.Plugin.Lore.disabled())
                        .addAllFlags()
                        .build();
            }
        }
    }

    /**
     * Factory methods for items displayed in the worlds configuration GUI.
     */
    public static class Worlds {
        /**
         * Creates a world item with a head texture matching the world environment type.
         *
         * @param environment    the world environment (NORMAL, NETHER, THE_END, or custom)
         * @param name           the display name for the world
         * @param disenchantment whether disenchantment is disabled for this world
         * @param shatterment    whether shatterment is disabled for this world
         * @return the world {@link ItemStack} with appropriate texture and lore
         */
        public static ItemStack worldByEnvironment(World.Environment environment, String name, boolean disenchantment, boolean shatterment) {
            List<String> lore = new ArrayList<>();

            if (!disenchantment) lore.add(I18n.GUI.Worlds.Lore.Disenchantment.enabled());
            else lore.add(I18n.GUI.Worlds.Lore.Disenchantment.disabled());

            if (!shatterment) lore.add(I18n.GUI.Worlds.Lore.Shatterment.enabled());
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

        /**
         * Creates the help item for the worlds GUI.
         *
         * @return the help {@link ItemStack}
         */
        public static ItemStack help() {
            return new ItemBuilder(Material.MAP)
                    .setDisplayName(I18n.GUI.Worlds.Help.title())
                    .setLore(I18n.GUI.Worlds.Help.lore())
                    .addAllFlags()
                    .build();
        }
    }

    /**
     * Factory methods for items displayed in the repair cost configuration GUIs.
     */
    public static class Repair {
        /**
         * Factory methods for disenchantment repair cost configuration items.
         */
        public static class Disenchantment {
            /**
             * Creates the base cost item for disenchantment repair.
             *
             * @param cost   the base cost value to display
             * @param amount the stack amount (clamped to 64)
             * @return the base cost {@link ItemStack}
             */
            public static ItemStack base(int cost, int amount) {
                return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Repair.Disenchantment.Base.title())
                        .setLore(I18n.GUI.Repair.Disenchantment.Base.lore(String.valueOf(cost)))
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the cost multiplier item for disenchantment repair.
             *
             * @param cost   the multiplier value to display
             * @param amount the stack amount (clamped to 64)
             * @return the multiplier {@link ItemStack}
             */
            public static ItemStack multiplier(double cost, int amount) {
                return new ItemBuilder(Material.DRAGON_BREATH)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Repair.Disenchantment.Multiplier.title())
                        .setLore(I18n.GUI.Repair.Disenchantment.Multiplier.lore(String.valueOf(cost)))
                        .addAllFlags()
                        .build();
            }

            /**
             * Factory methods for the disenchantment repair cost toggle item.
             */
            public static class Cost {
                /**
                 * Creates the cost-enabled indicator item (lime terracotta).
                 *
                 * @return the enabled state {@link ItemStack}
                 */
                public static ItemStack enabled() {
                    return new ItemBuilder(Material.LIME_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.Cost.title())
                            .setLore(I18n.GUI.Repair.Disenchantment.Cost.Lore.enabled())
                            .addAllFlags()
                            .build();
                }

                /**
                 * Creates the cost-disabled indicator item (red terracotta).
                 *
                 * @return the disabled state {@link ItemStack}
                 */
                public static ItemStack disabled() {
                    return new ItemBuilder(Material.RED_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.Cost.title())
                            .setLore(I18n.GUI.Repair.Disenchantment.Cost.Lore.disabled())
                            .addAllFlags()
                            .build();
                }
            }

            /**
             * Factory methods for the disenchantment repair cost reset toggle item.
             */
            public static class Reset {
                /**
                 * Creates the reset-enabled indicator item (lime terracotta).
                 *
                 * @return the enabled state {@link ItemStack}
                 */
                public static ItemStack enabled() {
                    return new ItemBuilder(Material.LIME_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.Reset.title())
                            .setLore(I18n.GUI.Repair.Disenchantment.Reset.Lore.enabled())
                            .addAllFlags()
                            .build();
                }

                /**
                 * Creates the reset-disabled indicator item (red terracotta).
                 *
                 * @return the disabled state {@link ItemStack}
                 */
                public static ItemStack disabled() {
                    return new ItemBuilder(Material.RED_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Disenchantment.Reset.title())
                            .setLore(I18n.GUI.Repair.Disenchantment.Reset.Lore.disabled())
                            .addAllFlags()
                            .build();
                }
            }
        }

        /**
         * Factory methods for shatterment repair cost configuration items.
         */
        public static class Shatterment {
            /**
             * Creates the base cost item for shatterment repair.
             *
             * @param cost   the base cost value to display
             * @param amount the stack amount (clamped to 64)
             * @return the base cost {@link ItemStack}
             */
            public static ItemStack base(int cost, int amount) {
                return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Repair.Shatterment.Base.title())
                        .setLore(I18n.GUI.Repair.Shatterment.Base.lore(String.valueOf(cost)))
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the cost multiplier item for shatterment repair.
             *
             * @param cost   the multiplier value to display
             * @param amount the stack amount (clamped to 64)
             * @return the multiplier {@link ItemStack}
             */
            public static ItemStack multiplier(double cost, int amount) {
                return new ItemBuilder(Material.DRAGON_BREATH)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Repair.Shatterment.Multiplier.title())
                        .setLore(I18n.GUI.Repair.Shatterment.Multiplier.lore(String.valueOf(cost)))
                        .addAllFlags()
                        .build();
            }

            /**
             * Factory methods for the shatterment repair cost toggle item.
             */
            public static class Cost {
                /**
                 * Creates the cost-enabled indicator item (lime terracotta).
                 *
                 * @return the enabled state {@link ItemStack}
                 */
                public static ItemStack enabled() {
                    return new ItemBuilder(Material.LIME_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.Cost.title())
                            .setLore(I18n.GUI.Repair.Shatterment.Cost.Lore.enabled())
                            .addAllFlags()
                            .build();
                }

                /**
                 * Creates the cost-disabled indicator item (red terracotta).
                 *
                 * @return the disabled state {@link ItemStack}
                 */
                public static ItemStack disabled() {
                    return new ItemBuilder(Material.RED_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.Cost.title())
                            .setLore(I18n.GUI.Repair.Shatterment.Cost.Lore.disabled())
                            .addAllFlags()
                            .build();
                }
            }

            /**
             * Factory methods for the shatterment repair cost reset toggle item.
             */
            public static class Reset {
                /**
                 * Creates the reset-enabled indicator item (lime terracotta).
                 *
                 * @return the enabled state {@link ItemStack}
                 */
                public static ItemStack enabled() {
                    return new ItemBuilder(Material.LIME_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.Reset.title())
                            .setLore(I18n.GUI.Repair.Shatterment.Reset.Lore.enabled())
                            .addAllFlags()
                            .build();
                }

                /**
                 * Creates the reset-disabled indicator item (red terracotta).
                 *
                 * @return the disabled state {@link ItemStack}
                 */
                public static ItemStack disabled() {
                    return new ItemBuilder(Material.RED_TERRACOTTA)
                            .setDisplayName(I18n.GUI.Repair.Shatterment.Reset.title())
                            .setLore(I18n.GUI.Repair.Shatterment.Reset.Lore.disabled())
                            .addAllFlags()
                            .build();
                }
            }
        }
    }

    /**
     * Factory methods for items displayed in the enchantments configuration GUI.
     */
    public static class Enchantments {
        /**
         * Creates an enchantment item showing its disenchantment and shatterment states.
         *
         * @param name           the enchantment display name
         * @param disenchantment the disenchantment state for this enchantment
         * @param shatterment    the shatterment state for this enchantment
         * @return the enchantment {@link ItemStack}
         */
        public static ItemStack enchantment(String name, EnchantmentStateType disenchantment, EnchantmentStateType shatterment) {
            List<String> lore = new ArrayList<>();

            lore.add(I18n.GUI.Enchantments.Lore.disenchantment(disenchantment.getDisplayName()));
            lore.add(I18n.GUI.Enchantments.Lore.shatterment(shatterment.getDisplayName()));

            return new ItemBuilder(Material.ENCHANTED_BOOK)
                    .setDisplayName(name)
                    .setLore(lore)
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the help item for the enchantments GUI.
         *
         * @return the help {@link ItemStack}
         */
        public static ItemStack help() {
            return new ItemBuilder(Material.MAP)
                    .setDisplayName(I18n.GUI.Enchantments.Help.title())
                    .setLore(I18n.GUI.Enchantments.Help.lore())
                    .addAllFlags()
                    .build();
        }
    }

    /**
     * Factory methods for items displayed in the materials configuration GUI.
     */
    public static class Materials {
        /**
         * Creates a material item showing whether disenchantment is disabled for it.
         *
         * @param material       the material to display
         * @param disenchantment whether disenchantment is disabled for this material
         * @return the material {@link ItemStack}
         */
        public static ItemStack material(Material material, boolean disenchantment) {
            List<String> lore = new ArrayList<>();

            if (!disenchantment) lore.add(I18n.GUI.Materials.Lore.enabled());
            else lore.add(I18n.GUI.Materials.Lore.disabled());

            return new ItemBuilder(material)
                    .setDisplayName(ChatColor.GRAY + material.getKey().getKey())
                    .setLore(lore)
                    .addAllFlags()
                    .build();
        }

        /**
         * Creates the help item for the materials GUI.
         *
         * @return the help {@link ItemStack}
         */
        public static ItemStack help() {
            return new ItemBuilder(Material.MAP)
                    .setDisplayName(I18n.GUI.Materials.Help.title())
                    .setLore(I18n.GUI.Materials.Help.lore())
                    .addAllFlags()
                    .build();
        }
    }

    /**
     * Factory methods for items displayed in the sound configuration GUIs.
     */
    public static class Sound {
        /**
         * Factory methods for disenchantment sound configuration items.
         */
        public static class Disenchantment {
            /**
             * Creates the sound-disabled indicator item (red terracotta).
             *
             * @return the disabled state {@link ItemStack}
             */
            public static ItemStack disabled() {
                return new ItemBuilder(Material.RED_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.title())
                        .setLore(I18n.GUI.Sound.Disenchantment.Lore.disabled())
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the sound-enabled indicator item (lime terracotta).
             *
             * @return the enabled state {@link ItemStack}
             */
            public static ItemStack enabled() {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.title())
                        .setLore(I18n.GUI.Sound.Disenchantment.Lore.enabled())
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the volume adjustment item for disenchantment sound.
             *
             * @param volume the current volume value
             * @param amount the stack amount (clamped to 64)
             * @return the volume {@link ItemStack}
             */
            public static ItemStack volume(double volume, int amount) {
                return new ItemBuilder(Material.FIRE_CHARGE)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.Volume.title())
                        .setLore(I18n.GUI.Sound.Disenchantment.Volume.lore(String.valueOf(volume)))
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the pitch adjustment item for disenchantment sound.
             *
             * @param pitch  the current pitch value
             * @param amount the stack amount (clamped to 64)
             * @return the pitch {@link ItemStack}
             */
            public static ItemStack pitch(double pitch, int amount) {
                return new ItemBuilder(Material.SLIME_BALL)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Disenchantment.Pitch.title())
                        .setLore(I18n.GUI.Sound.Disenchantment.Pitch.lore(String.valueOf(pitch)))
                        .addAllFlags()
                        .build();
            }
        }

        /**
         * Factory methods for shatterment sound configuration items.
         */
        public static class Shatterment {
            /**
             * Creates the sound-disabled indicator item (red terracotta).
             *
             * @return the disabled state {@link ItemStack}
             */
            public static ItemStack disabled() {
                return new ItemBuilder(Material.RED_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.title())
                        .setLore(I18n.GUI.Sound.Shatterment.Lore.disabled())
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the sound-enabled indicator item (lime terracotta).
             *
             * @return the enabled state {@link ItemStack}
             */
            public static ItemStack enabled() {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.title())
                        .setLore(I18n.GUI.Sound.Shatterment.Lore.enabled())
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the volume adjustment item for shatterment sound.
             *
             * @param volume the current volume value
             * @param amount the stack amount (clamped to 64)
             * @return the volume {@link ItemStack}
             */
            public static ItemStack volume(double volume, int amount) {
                return new ItemBuilder(Material.FIRE_CHARGE)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.Volume.title())
                        .setLore(I18n.GUI.Sound.Shatterment.Volume.lore(String.valueOf(volume)))
                        .addAllFlags()
                        .build();
            }

            /**
             * Creates the pitch adjustment item for shatterment sound.
             *
             * @param pitch  the current pitch value
             * @param amount the stack amount (clamped to 64)
             * @return the pitch {@link ItemStack}
             */
            public static ItemStack pitch(double pitch, int amount) {
                return new ItemBuilder(Material.SLIME_BALL)
                        .setAmount(amount)
                        .setDisplayName(I18n.GUI.Sound.Shatterment.Pitch.title())
                        .setLore(I18n.GUI.Sound.Shatterment.Pitch.lore(String.valueOf(pitch)))
                        .addAllFlags()
                        .build();
            }
        }
    }
}
