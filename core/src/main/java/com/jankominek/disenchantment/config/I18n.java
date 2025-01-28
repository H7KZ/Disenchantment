package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.ConfigKeys;
import org.bukkit.ChatColor;

import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.config;

public class I18n {
    private static String translateColors(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getPrefix() {
        return I18n.translateColors(config.getString(ConfigKeys.I18N_PREFIX.getKey()));
    }

    public static class Messages {
        public static String requiresPermission() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_MESSAGES_REQUIRES_PERMISSION.getKey()));
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

        public static String disabled() {
            return I18n.translateColors(config.getString(ConfigKeys.I18N_STATES_DISABLED.getKey()));
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

                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Base {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_BASE_TITLE.getKey()));
                    }

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_BASE_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Multiplier {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }
            }

            public static class Shatterment {
                public static String inventory() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_INVENTORY.getKey()));
                }

                public static String title() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Base {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_BASE_TITLE.getKey()));
                    }

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_BASE_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Multiplier {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_REPAIR_SHATTERMENT_MULTIPLIER_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }
            }
        }

        public static class Enchantments {
            public static String inventory() {
                return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static String disenchantment() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_LORE_DISENCHANTMENT.getKey()));
                }

                public static String shatterment() {
                    return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_ENCHANTMENTS_LORE_SHATTERMENT.getKey()));
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
                public static class Disenchantment {
                    public static String enabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_LORE_DISENCHANTMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_LORE_DISENCHANTMENT_DISABLED.getKey()));
                    }
                }

                public static class Shatterment {
                    public static String enabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_LORE_SHATTERMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_MATERIALS_LORE_SHATTERMENT_DISABLED.getKey()));
                    }
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

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_VOLUME_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Pitch {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_PITCH_TITLE.getKey()));
                    }

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_DISENCHANTMENT_PITCH_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
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

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_VOLUME_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Pitch {
                    public static String title() {
                        return I18n.translateColors(config.getString(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_PITCH_TITLE.getKey()));
                    }

                    public static List<String> lore() {
                        return config.getStringList(ConfigKeys.I18N_GUI_SOUND_SHATTERMENT_PITCH_LORE.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }
            }
        }
    }
}
