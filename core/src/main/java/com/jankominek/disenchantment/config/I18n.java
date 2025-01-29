package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import org.bukkit.ChatColor;

import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.config;

public class I18n {
    private static String translateColors(String text) {
        if (text == null) return "> Missing translation <";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getPrefix() {
        return I18n.translateColors(config.getString(ConfigKeys.I18N_PREFIX.getKey()));
    }

    public static class Messages {
        public static String requiresPermission() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_REQUIRES_PERMISSION.getKey()));
        }

        public static String invalidArgument() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_INVALID_ARGUMENT.getKey()));
        }

        public static String unknownEnchantment() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_UNKNOWN_ENCHANTMENT.getKey()));
        }

        public static String specifyEnchantmentState() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_SPECIFY_ENCHANTMENT_STATE.getKey()));
        }

        public static String enchantmentIsEnabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_ENCHANTMENT_IS_ENABLED.getKey()));
        }

        public static String enchantmentIsKept() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_ENCHANTMENT_IS_KEPT.getKey()));
        }

        public static String enchantmentIsDeleted() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_ENCHANTMENT_IS_DELETED.getKey()));
        }

        public static String enchantmentIsDisabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_ENCHANTMENT_IS_DISABLED.getKey()));
        }

        public static String materialIsEnabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_MATERIAL_IS_ENABLED.getKey()));
        }

        public static String materialIsDisabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_MATERIAL_IS_DISABLED.getKey()));
        }

        public static String specifyRepairValue() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_SPECIFY_REPAIR_VALUE.getKey()));
        }

        public static String specifyValidInteger() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_SPECIFY_VALID_INTEGER.getKey()));
        }

        public static String specifyValidDouble() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_SPECIFY_VALID_DOUBLE.getKey()));
        }

        public static String repairCostIsEnabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_REPAIR_COST_IS_ENABLED.getKey()));
        }

        public static String repairCostIsDisabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_REPAIR_COST_IS_DISABLED.getKey()));
        }

        public static String repairCostResetIsEnabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_REPAIR_COST_RESET_IS_ENABLED.getKey()));
        }

        public static String repairCostResetIsDisabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_REPAIR_COST_RESET_IS_DISABLED.getKey()));
        }

        public static String repairBaseCostIsSet(String cost) {
            return I18n.translateColors(
                    config.getString(ConfigKeys.I18N_MESSAGES_REPAIR_BASE_COST_IS_SET.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        public static String repairMultiplierIsSet(String multiply) {
            return I18n.translateColors(
                    config.getString(ConfigKeys.I18N_MESSAGES_REPAIR_MULTIPLIER_IS_SET.getKey(), "default")
                            .replace("{multiply}", multiply)
            );
        }
    }

    public static class States {
        public static String enabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_STATES_ENABLED.getKey()));
        }

        public static String keep() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_STATES_KEEP.getKey()));
        }

        public static String delete() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_STATES_DELETE.getKey()));
        }

        public static String disable() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_STATES_DISABLE.getKey()));
        }
    }

    public static class Commands {
        public static class Help {
            public static String title() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_HELP_TITLE.getKey()));
            }

            public static List<List<String>> pages() {
                return List.of(
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_1.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_2.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_3.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_4.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_5.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_6.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_7.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        config.getStringList(ConfigKeys.I18N_COMMANDS_HELP_PAGES_8.getKey())
                                .stream().map(I18n::translateColors).toList()
                );
            }
        }

        public static class Status {
            public static String title() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_STATUS_TITLE.getKey()));
            }

            public static String global(String state) {
                return I18n.translateColors(
                        config.getString(ConfigKeys.I18N_COMMANDS_STATUS_GLOBAL.getKey(), "default")
                                .replace("{state}", state)
                );
            }

            public static String disenchantment(String state) {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_STATUS_DISENCHANTMENT.getKey(), "default")
                        .replace("{state}", state)
                );
            }

            public static String shatterment(String state) {
                return I18n.translateColors(
                        config.getString(ConfigKeys.I18N_COMMANDS_STATUS_SHATTERMENT.getKey(), "default")
                                .replace("{state}", state)
                );
            }

            public static class States {
                public static String enabled() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_STATUS_STATES_ENABLED.getKey()));
                }

                public static String disabled() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_STATUS_STATES_DISABLED.getKey()));
                }
            }
        }

        public static class Enchantments {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_ENCHANTMENTS_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String empty() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_ENCHANTMENTS_DISENCHANTMENT_EMPTY.getKey()));
                }

                public static String enchantment(String enchantment, String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_ENCHANTMENTS_DISENCHANTMENT_ENCHANTMENT.getKey(), "default")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{state}", state)
                    );
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_ENCHANTMENTS_SHATTERMENT_TITLE.getKey()));
                }

                public static String empty() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_ENCHANTMENTS_SHATTERMENT_EMPTY.getKey()));
                }

                public static String enchantment(String enchantment, String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_ENCHANTMENTS_SHATTERMENT_ENCHANTMENT.getKey(), "default")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{state}", state)
                    );
                }
            }
        }

        public static class Materials {
            public static String title() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_MATERIALS_TITLE.getKey()));
            }

            public static String empty() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_MATERIALS_EMPTY.getKey()));
            }

            public static String material(String material, String state) {
                return I18n.translateColors(
                        config.getString(ConfigKeys.I18N_COMMANDS_MATERIALS_MATERIAL.getKey(), "default")
                                .replace("{material}", material)
                                .replace("{state}", state)
                );
            }

            public static class States {
                public static String disabled() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_MATERIALS_STATES_DISABLED.getKey()));
                }
            }
        }

        public static class Repair {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String cost(String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String reset(String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_RESET.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String base(String cost) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_BASE.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                public static String multiply(String multiply) {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_MULTIPLY.getKey(), "default")
                            .replace("{multiply}", multiply)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_TITLE.getKey()));
                }

                public static String cost(String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String reset(String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_RESET.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String base(String cost) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_BASE.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                public static String multiply(String multiply) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_MULTIPLY.getKey(), "default")
                                    .replace("{multiply}", multiply)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_COMMANDS_REPAIR_SHATTERMENT_STATES_DISABLED.getKey()));
                    }
                }
            }
        }
    }

    public static class GUI {
        public static String back() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_BACK.getKey()));
        }

        public static String previous() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_PREVIOUS.getKey()));
        }

        public static String next() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NEXT.getKey()));
        }

        public static class Navigation {
            public static String inventory() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_INVENTORY.getKey()));
            }

            public static class Plugin {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_PLUGIN_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_PLUGIN_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_PLUGIN_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }
            }

            public static class Worlds {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_WORLDS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_WORLDS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Repair {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_REPAIR_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_REPAIR_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Enchantments {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_ENCHANTMENTS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_ENCHANTMENTS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Materials {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_MATERIALS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_MATERIALS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Sound {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_SOUND_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_SOUND_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Spigot {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_NAVIGATION_SPIGOT_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_NAVIGATION_SPIGOT_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Worlds {
            public static String inventory() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_WORLDS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static class Disenchantment {
                    public static String enabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_WORLDS_LORE_DISENCHANTMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_WORLDS_LORE_DISENCHANTMENT_DISABLED.getKey()));
                    }
                }

                public static class Shatterment {
                    public static String enabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_WORLDS_LORE_SHATTERMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_WORLDS_LORE_SHATTERMENT_DISABLED.getKey()));
                    }
                }
            }

            public static class Help {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_WORLDS_HELP_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_WORLDS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Repair {
            public static class Disenchantment {
                public static String inventory() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_INVENTORY.getKey()));
                }

                public static class Cost {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_COST_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Reset {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_RESET_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_RESET_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_RESET_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Base {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_BASE_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_BASE_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                public static class Multiplier {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    public static List<String> lore(String multiplier) {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{multiplier}", multiplier))
                                .toList();
                    }
                }
            }

            public static class Shatterment {
                public static String inventory() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_INVENTORY.getKey()));
                }

                public static class Cost {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_COST_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Reset {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_RESET_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_RESET_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_RESET_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Base {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_BASE_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_BASE_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                public static class Multiplier {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    public static List<String> lore(String multiplier) {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_MULTIPLIER_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{multiplier}", multiplier))
                                .toList();
                    }
                }
            }
        }

        public static class Enchantments {
            public static String inventory() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static String disenchantment(String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_LORE_DISENCHANTMENT.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String shatterment(String state) {
                    return I18n.translateColors(
                            config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_LORE_SHATTERMENT.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }
            }

            public static class Help {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_HELP_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_ENCHANTMENTS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Materials {
            public static String inventory() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static String enabled() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_LORE_ENABLED.getKey()));
                }

                public static String disabled() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_LORE_DISABLED.getKey()));
                }
            }

            public static class Help {
                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_HELP_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return config.getStringList(ConfigKeys.I18N_GUI_MATERIALS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Sound {
            public static class Disenchantment {
                public static String inventory() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_INVENTORY.getKey()));
                }

                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Volume {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_VOLUME_TITLE.getKey()));
                    }

                    public static List<String> lore(String volume) {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_VOLUME_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{volume}", volume))
                                .toList();
                    }
                }

                public static class Pitch {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_PITCH_TITLE.getKey()));
                    }

                    public static List<String> lore(String pitch) {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_PITCH_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{pitch}", pitch))
                                .toList();
                    }
                }
            }

            public static class Shatterment {
                public static String inventory() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_INVENTORY.getKey()));
                }

                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Volume {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_VOLUME_TITLE.getKey()));
                    }

                    public static List<String> lore(String volume) {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_VOLUME_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{volume}", volume))
                                .toList();
                    }
                }

                public static class Pitch {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_PITCH_TITLE.getKey()));
                    }

                    public static List<String> lore(String pitch) {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_PITCH_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{pitch}", pitch))
                                .toList();
                    }
                }
            }
        }
    }
}
