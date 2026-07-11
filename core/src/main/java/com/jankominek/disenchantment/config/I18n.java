package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.types.I18nKeys;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.localeConfig;

/**
 * Internationalization (i18n) utility that provides localized, color-translated
 * strings for plugin messages, command output, GUI labels, and status displays.
 * All strings are read from the active locale configuration file.
 */
public class I18n {
    private static String translateColors(String text) {
        if (text == null) return "> Missing translation <";
        return LegacyComponentSerializer.legacySection().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(text));
    }

    /**
     * Gets the localized chat prefix for plugin messages.
     *
     * @return the color-translated prefix string
     */
    public static String getPrefix() {
        return I18n.translateColors(localeConfig.getString(I18nKeys.PREFIX.getKey()));
    }

    /**
     * Localized feedback messages sent to players and operators.
     */
    public static class Messages {
        public static String requiresPermission() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_REQUIRES_PERMISSION.getKey()));
        }

        public static String invalidArgument() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_INVALID_ARGUMENT.getKey()));
        }

        public static String specifyEnchantmentState() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_ENCHANTMENT_STATE.getKey()));
        }

        public static String enchantmentIsEnabled(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_ENABLED.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        public static String enchantmentIsKept(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_KEPT.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        public static String enchantmentIsDeleted(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_DELETED.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        public static String enchantmentIsDisabled(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_DISABLED.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        public static String materialIsEnabled(String material) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MATERIAL_IS_ENABLED.getKey(), "default")
                            .replace("{material}", material)
            );
        }

        public static String materialIsDisabled(String material) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MATERIAL_IS_DISABLED.getKey(), "default")
                            .replace("{material}", material)
            );
        }

        public static String specifyRepairValue() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_REPAIR_VALUE.getKey()));
        }

        public static String specifyValidDouble() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_VALID_DOUBLE.getKey()));
        }

        public static String repairCostIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_REPAIR_COST_IS_DISABLED.getKey()));
        }

        public static String repairCostResetIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_REPAIR_COST_RESET_IS_ENABLED.getKey()));
        }

        public static String repairBaseCostIsSet(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_REPAIR_BASE_COST_IS_SET.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        public static String repairMultiplierIsSet(String multiply) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_REPAIR_MULTIPLIER_IS_SET.getKey(), "default")
                            .replace("{multiply}", multiply)
            );
        }

        public static String specifySoundState() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_SOUND_STATE.getKey()));
        }

        public static String soundIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SOUND_IS_ENABLED.getKey()));
        }

        public static String soundIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SOUND_IS_DISABLED.getKey()));
        }

        public static String soundVolumeIsSet(String volume) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_SOUND_VOLUME_IS_SET.getKey(), "default")
                            .replace("{volume}", volume)
            );
        }

        public static String soundPitchIsSet(String pitch) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_SOUND_PITCH_IS_SET.getKey(), "default")
                            .replace("{pitch}", pitch)
            );
        }

        public static String worldNotFound(String world) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_WORLD_NOT_FOUND.getKey(), "default")
                            .replace("{world}", world)
            );
        }

        public static String worldIsEnabled(String world) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_WORLD_IS_ENABLED.getKey(), "default")
                            .replace("{world}", world)
            );
        }

        public static String worldIsDisabled(String world) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_WORLD_IS_DISABLED.getKey(), "default")
                            .replace("{world}", world)
            );
        }

        public static String economyCost(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_COST.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        public static String economyInsufficientFunds(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_INSUFFICIENT_FUNDS.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        public static String economyCharged(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_CHARGED.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        public static String economyNotAvailable() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_NOT_AVAILABLE.getKey()));
        }

        public static String cooldownActive(String seconds) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_COOLDOWN_ACTIVE.getKey(), "&cYou must wait &f{seconds}s &cbefore doing that again.&r")
                            .replace("{seconds}", seconds)
            );
        }

        public static String someEnchantmentsMayNotTransfer() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SOME_ENCHANTMENTS_MAY_NOT_TRANSFER.getKey(), "&7Some enchantments may not transfer"));
        }

        public static String specifyEnchantmentChance() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_ENCHANTMENT_CHANCE.getKey(), "&cSpecify an enchantment and a chance between 0.0 and 1.0"));
        }

        public static String enchantmentChanceIsSet(String enchantment, String chance) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_CHANCE_IS_SET.getKey(), "&aChance for {enchantment} set to {chance}")
                            .replace("{enchantment}", enchantment)
                            .replace("{chance}", chance)
            );
        }

        public static String economyEnabledIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_ENABLED_IS_ENABLED.getKey()));
        }

        public static String economyEnabledIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_ENABLED_IS_DISABLED.getKey()));
        }

        public static String economyCostIsSet(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_COST_IS_SET.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        public static String economyShowCostIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_SHOW_COST_IS_ENABLED.getKey()));
        }

        public static String economyShowCostIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_SHOW_COST_IS_DISABLED.getKey()));
        }

        public static String economyChargeMessageIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_CHARGE_MESSAGE_IS_ENABLED.getKey()));
        }

        public static String economyChargeMessageIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_CHARGE_MESSAGE_IS_DISABLED.getKey()));
        }
    }

    /**
     * Localized display names for enchantment state types.
     */
    public static class States {
        public static String enable() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_ENABLE.getKey()));
        }

        public static String keep() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_KEEP.getKey()));
        }

        public static String delete() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_DELETE.getKey()));
        }

        public static String disable() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_DISABLE.getKey()));
        }
    }

    /**
     * Localized strings for command output (help pages, status,
     * enchantments, repair, sound, worlds, and economy sub-commands).
     */
    public static class Commands {
        /**
         * Localized strings for the help command output.
         */
        public static class Help {
            public static String title(String page) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_HELP_TITLE.getKey(), "default")
                                .replace("{page}", page)
                );
            }

            public static List<List<String>> pages() {
                return List.of(
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_1.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_2.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_3.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_4.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_5.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_6.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_7.getKey())
                                .stream().map(I18n::translateColors).toList(),
                        localeConfig.getStringList(I18nKeys.COMMANDS_HELP_PAGES_8.getKey())
                                .stream().map(I18n::translateColors).toList()
                );
            }
        }

        /**
         * Localized strings for the status command output.
         */
        public static class Status {
            public static String title() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_TITLE.getKey()));
            }

            public static String global(String state) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_STATUS_GLOBAL.getKey(), "default")
                                .replace("{state}", state)
                );
            }

            public static String disenchantment(String state) {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_DISENCHANTMENT.getKey(), "default")
                        .replace("{state}", state)
                );
            }

            public static String shatterment(String state) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_STATUS_SHATTERMENT.getKey(), "default")
                                .replace("{state}", state)
                );
            }

            public static class States {
                public static String enabled() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_STATES_ENABLED.getKey()));
                }

                public static String disabled() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_STATES_DISABLED.getKey()));
                }
            }
        }

        /**
         * Localized strings for the enchantments command output.
         */
        public static class Enchantments {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_DISENCHANTMENT_EMPTY.getKey()));
                }

                public static String enchantment(String enchantment, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_DISENCHANTMENT_ENCHANTMENT.getKey(), "default")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{state}", state)
                    );
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_SHATTERMENT_TITLE.getKey()));
                }

                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_SHATTERMENT_EMPTY.getKey()));
                }

                public static String enchantment(String enchantment, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_SHATTERMENT_ENCHANTMENT.getKey(), "default")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{state}", state)
                    );
                }
            }
        }

        /**
         * Localized strings for the enchantment chances command output.
         */
        public static class Chances {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_DISENCHANTMENT_TITLE.getKey(), "&6Disenchantment enchantment chances:"));
                }

                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_DISENCHANTMENT_EMPTY.getKey(), "No custom chances configured."));
                }

                public static String enchantment(String enchantment, String chance) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_CHANCES_DISENCHANTMENT_ENCHANTMENT.getKey(), "&7{enchantment}: &f{chance}")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{chance}", chance)
                    );
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_SHATTERMENT_TITLE.getKey(), "&6Shatterment enchantment chances:"));
                }

                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_SHATTERMENT_EMPTY.getKey(), "No custom chances configured."));
                }

                public static String enchantment(String enchantment, String chance) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_CHANCES_SHATTERMENT_ENCHANTMENT.getKey(), "&7{enchantment}: &f{chance}")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{chance}", chance)
                    );
                }
            }
        }

        /**
         * Localized strings for the materials command output.
         */
        public static class Materials {
            public static String title() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_TITLE.getKey()));
            }

            public static String empty() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_EMPTY.getKey()));
            }

            public static String material(String material, String state) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_MATERIAL.getKey(), "default")
                                .replace("{material}", material)
                                .replace("{state}", state)
                );
            }

            public static class States {
                public static String disabled() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_STATES_DISABLED.getKey()));
                }
            }
        }

        /**
         * Localized strings for the repair cost command output.
         */
        public static class Repair {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String cost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String reset(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_RESET.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String base(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_BASE.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                public static String multiply(String multiply) {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_MULTIPLY.getKey(), "default")
                            .replace("{multiply}", multiply)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_TITLE.getKey()));
                }

                public static String cost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String reset(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_RESET.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String base(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_BASE.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                public static String multiply(String multiply) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_MULTIPLY.getKey(), "default")
                                    .replace("{multiply}", multiply)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_STATES_DISABLED.getKey()));
                    }
                }
            }
        }

        /**
         * Localized strings for the sound command output.
         */
        public static class Sound {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String sound(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_SOUND.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String volume(String volume) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_VOLUME.getKey(), "default")
                                    .replace("{volume}", volume)
                    );
                }

                public static String pitch(String pitch) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_PITCH.getKey(), "default")
                                    .replace("{pitch}", pitch)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_TITLE.getKey()));
                }

                public static String sound(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_SOUND.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String volume(String volume) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_VOLUME.getKey(), "default")
                                    .replace("{volume}", volume)
                    );
                }

                public static String pitch(String pitch) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_PITCH.getKey(), "default")
                                    .replace("{pitch}", pitch)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_STATES_DISABLED.getKey()));
                    }
                }
            }
        }

        /**
         * Localized strings for the worlds command output.
         */
        public static class Worlds {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_EMPTY.getKey()));
                }

                public static String world(String world, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_WORLD.getKey(), "default")
                                    .replace("{world}", world)
                                    .replace("{state}", state)
                    );
                }

                public static class States {
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_TITLE.getKey()));
                }

                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_EMPTY.getKey()));
                }

                public static String world(String world, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_WORLD.getKey(), "default")
                                    .replace("{world}", world)
                                    .replace("{state}", state)
                    );
                }

                public static class States {
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_STATES_DISABLED.getKey()));
                    }
                }
            }
        }

        /**
         * Localized strings for the economy command output.
         */
        public static class Economy {
            public static class Disenchantment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_TITLE.getKey()));
                }

                public static String enabled(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_ENABLED.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String cost(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_COST.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                public static String showCost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_SHOW_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String chargeMessage(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            public static class Shatterment {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_TITLE.getKey()));
                }

                public static String enabled(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_ENABLED.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String cost(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_COST.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                public static String showCost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_SHOW_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String chargeMessage(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_CHARGE_MESSAGE.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static class States {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_STATES_DISABLED.getKey()));
                    }
                }
            }
        }
    }

    /**
     * Localized strings for in-game GUI inventory titles, lore, and navigation labels.
     */
    public static class GUI {
        public static String back() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_BACK.getKey()));
        }

        public static String previous() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_PREVIOUS.getKey()));
        }

        public static String next() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NEXT.getKey()));
        }

        public static class Navigation {
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_INVENTORY.getKey()));
            }

            public static class Plugin {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_PLUGIN_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_PLUGIN_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_PLUGIN_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }
            }

            public static class Worlds {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_WORLDS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_WORLDS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Repair {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_REPAIR_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_REPAIR_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Enchantments {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_ENCHANTMENTS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_ENCHANTMENTS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Materials {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_MATERIALS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_MATERIALS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Sound {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_SOUND_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_SOUND_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Spigot {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_SPIGOT_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_SPIGOT_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Economy {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_ECONOMY_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_ECONOMY_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            public static class Stats {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_STATS_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_STATS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Worlds {
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static class Disenchantment {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_DISENCHANTMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_DISENCHANTMENT_DISABLED.getKey()));
                    }
                }

                public static class Shatterment {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_SHATTERMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_SHATTERMENT_DISABLED.getKey()));
                    }
                }
            }

            public static class Help {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_HELP_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_WORLDS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Repair {
            public static class Disenchantment {
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_INVENTORY.getKey()));
                }

                public static class Cost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_COST_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Reset {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_RESET_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_RESET_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_RESET_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Base {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_BASE_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_BASE_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                public static class Multiplier {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    public static List<String> lore(String multiplier) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{multiplier}", multiplier))
                                .toList();
                    }
                }

                public static class MaxCost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MAXCOST_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MAXCOST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }

                    public static String unlimited() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MAXCOST_UNLIMITED.getKey()));
                    }
                }
            }

            public static class Shatterment {
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_INVENTORY.getKey()));
                }

                public static class Cost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_COST_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Reset {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_RESET_LORE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_RESET_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_RESET_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Base {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_BASE_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_BASE_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                public static class Multiplier {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    public static List<String> lore(String multiplier) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_MULTIPLIER_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{multiplier}", multiplier))
                                .toList();
                    }
                }

                public static class MaxCost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_MAXCOST_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_MAXCOST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }

                    public static String unlimited() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_MAXCOST_UNLIMITED.getKey()));
                    }
                }
            }
        }

        public static class Enchantments {
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static String disenchantment(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_LORE_DISENCHANTMENT.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                public static String shatterment(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_LORE_SHATTERMENT.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }
            }

            public static class Help {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_HELP_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_ENCHANTMENTS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Materials {
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_INVENTORY.getKey()));
            }

            public static class Lore {
                public static class Disenchantment {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_DISENCHANTMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_DISENCHANTMENT_DISABLED.getKey()));
                    }
                }

                public static class Shatterment {
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_SHATTERMENT_ENABLED.getKey()));
                    }

                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_SHATTERMENT_DISABLED.getKey()));
                    }
                }
            }

            public static class Help {
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_HELP_TITLE.getKey()));
                }

                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_MATERIALS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        public static class Sound {
            public static class Disenchantment {
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_INVENTORY.getKey()));
                }

                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Volume {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_VOLUME_TITLE.getKey()));
                    }

                    public static List<String> lore(String volume) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_VOLUME_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{volume}", volume))
                                .toList();
                    }
                }

                public static class Pitch {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_PITCH_TITLE.getKey()));
                    }

                    public static List<String> lore(String pitch) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_PITCH_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{pitch}", pitch))
                                .toList();
                    }
                }
            }

            public static class Shatterment {
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_INVENTORY.getKey()));
                }

                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_TITLE.getKey()));
                }

                public static class Lore {
                    public static List<String> enabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    public static List<String> disabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                public static class Volume {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_VOLUME_TITLE.getKey()));
                    }

                    public static List<String> lore(String volume) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_VOLUME_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{volume}", volume))
                                .toList();
                    }
                }

                public static class Pitch {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_PITCH_TITLE.getKey()));
                    }

                    public static List<String> lore(String pitch) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_PITCH_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{pitch}", pitch))
                                .toList();
                    }
                }
            }
        }

        public static class Economy {
            public static class Disenchantment {
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_INVENTORY.getKey()));
                }

                public static class Enabled {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_ENABLED_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_ENABLED_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_ENABLED_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Cost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_COST_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_COST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                public static class ShowCost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_SHOW_COST_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_SHOW_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_SHOW_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class ChargeMessage {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }
            }

            public static class Shatterment {
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_INVENTORY.getKey()));
                }

                public static class Enabled {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_ENABLED_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_ENABLED_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_ENABLED_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class Cost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_COST_TITLE.getKey()));
                    }

                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_COST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                public static class ShowCost {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_SHOW_COST_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_SHOW_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_SHOW_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                public static class ChargeMessage {
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_CHARGE_MESSAGE_TITLE.getKey()));
                    }

                    public static class Lore {
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_CHARGE_MESSAGE_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_CHARGE_MESSAGE_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }
            }
        }

        public static class Stats {
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_STATS_INVENTORY.getKey()));
            }
        }
    }
}
