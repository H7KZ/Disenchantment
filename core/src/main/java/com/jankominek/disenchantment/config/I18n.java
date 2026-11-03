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
        /**
         * Returns the localized "requires permission" message.
         */
        public static String requiresPermission() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_REQUIRES_PERMISSION.getKey()));
        }

        /**
         * Returns the localized "invalid argument" message.
         */
        public static String invalidArgument() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_INVALID_ARGUMENT.getKey()));
        }

        /**
         * Returns the localized "specify enchantment state" prompt.
         */
        public static String specifyEnchantmentState() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_ENCHANTMENT_STATE.getKey()));
        }

        /**
         * Returns the localized message confirming an enchantment is enabled.
         *
         * @param enchantment the enchantment name to substitute
         */
        public static String enchantmentIsEnabled(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_ENABLED.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        /**
         * Returns the localized message confirming an enchantment is set to "keep".
         *
         * @param enchantment the enchantment name to substitute
         */
        public static String enchantmentIsKept(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_KEPT.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        /**
         * Returns the localized message confirming an enchantment is set to "delete".
         *
         * @param enchantment the enchantment name to substitute
         */
        public static String enchantmentIsDeleted(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_DELETED.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        /**
         * Returns the localized message confirming an enchantment is disabled.
         *
         * @param enchantment the enchantment name to substitute
         */
        public static String enchantmentIsDisabled(String enchantment) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_IS_DISABLED.getKey(), "default")
                            .replace("{enchantment}", enchantment)
            );
        }

        /**
         * Returns the localized message confirming a material is enabled.
         *
         * @param material the material name to substitute
         */
        public static String materialIsEnabled(String material) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MATERIAL_IS_ENABLED.getKey(), "default")
                            .replace("{material}", material)
            );
        }

        /**
         * Returns the localized message confirming a material is disabled.
         *
         * @param material the material name to substitute
         */
        public static String materialIsDisabled(String material) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MATERIAL_IS_DISABLED.getKey(), "default")
                            .replace("{material}", material)
            );
        }

        /**
         * Returns the localized message reporting the current restriction mode.
         *
         * @param feature the feature label to substitute
         * @param mode    the mode value to substitute
         */
        public static String modeCurrent(String feature, String mode) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MODE_CURRENT.getKey(), "&7{feature} restriction mode: &f{mode}")
                            .replace("{feature}", feature)
                            .replace("{mode}", mode)
            );
        }

        /**
         * Returns the localized message confirming a restriction mode change.
         *
         * @param feature the feature label to substitute
         * @param mode    the mode value to substitute
         */
        public static String modeSet(String feature, String mode) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MODE_SET.getKey(), "&a{feature} restriction mode set to &f{mode}")
                            .replace("{feature}", feature)
                            .replace("{mode}", mode)
            );
        }

        /**
         * Returns the localized message for an invalid restriction mode value.
         *
         * @param mode the invalid mode value to substitute
         */
        public static String modeInvalid(String mode) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MODE_INVALID.getKey(), "&cInvalid mode '{mode}'. Use ALLOWLIST or DENYLIST.")
                            .replace("{mode}", mode)
            );
        }

        /**
         * Returns the localized "specify repair value" prompt.
         */
        public static String specifyRepairValue() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_REPAIR_VALUE.getKey()));
        }

        /**
         * Returns the localized "specify a valid double" prompt.
         */
        public static String specifyValidDouble() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_VALID_DOUBLE.getKey()));
        }

        /**
         * Returns the localized "specify a number that is zero or greater" prompt.
         */
        public static String specifyNonNegativeDouble() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_NON_NEGATIVE_DOUBLE.getKey(), "&cPlease specify a number that is zero or greater!&r"));
        }

        /**
         * Returns the localized "repair cost is disabled" message.
         */
        public static String repairCostIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_REPAIR_COST_IS_DISABLED.getKey()));
        }

        /**
         * Returns the localized "repair cost reset is enabled" message.
         */
        public static String repairCostResetIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_REPAIR_COST_RESET_IS_ENABLED.getKey()));
        }

        /**
         * Returns the localized message confirming the base repair cost was set.
         *
         * @param cost the cost value to substitute
         */
        public static String repairBaseCostIsSet(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_REPAIR_BASE_COST_IS_SET.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        /**
         * Returns the localized message confirming the repair cost multiplier was set.
         *
         * @param multiply the multiplier value to substitute
         */
        public static String repairMultiplierIsSet(String multiply) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_REPAIR_MULTIPLIER_IS_SET.getKey(), "default")
                            .replace("{multiply}", multiply)
            );
        }

        /**
         * Returns the localized "specify sound state" prompt.
         */
        public static String specifySoundState() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_SOUND_STATE.getKey()));
        }

        /**
         * Returns the localized "sound is enabled" message.
         */
        public static String soundIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SOUND_IS_ENABLED.getKey()));
        }

        /**
         * Returns the localized "sound is disabled" message.
         */
        public static String soundIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SOUND_IS_DISABLED.getKey()));
        }

        /**
         * Returns the localized message confirming the sound volume was set.
         *
         * @param volume the volume value to substitute
         */
        public static String soundVolumeIsSet(String volume) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_SOUND_VOLUME_IS_SET.getKey(), "default")
                            .replace("{volume}", volume)
            );
        }

        /**
         * Returns the localized message confirming the sound pitch was set.
         *
         * @param pitch the pitch value to substitute
         */
        public static String soundPitchIsSet(String pitch) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_SOUND_PITCH_IS_SET.getKey(), "default")
                            .replace("{pitch}", pitch)
            );
        }

        /**
         * Returns the localized "world not found" message.
         *
         * @param world the world name to substitute
         */
        public static String worldNotFound(String world) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_WORLD_NOT_FOUND.getKey(), "default")
                            .replace("{world}", world)
            );
        }

        /**
         * Returns the localized message confirming a world is enabled.
         *
         * @param world the world name to substitute
         */
        public static String worldIsEnabled(String world) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_WORLD_IS_ENABLED.getKey(), "default")
                            .replace("{world}", world)
            );
        }

        /**
         * Returns the localized message confirming a world is disabled.
         *
         * @param world the world name to substitute
         */
        public static String worldIsDisabled(String world) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_WORLD_IS_DISABLED.getKey(), "default")
                            .replace("{world}", world)
            );
        }

        /**
         * Returns the localized economy cost message shown before an operation.
         *
         * @param cost the cost value to substitute
         */
        public static String economyCost(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_COST.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        /**
         * Returns the localized "insufficient funds" message.
         *
         * @param cost the required cost to substitute
         */
        public static String economyInsufficientFunds(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_INSUFFICIENT_FUNDS.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        /**
         * Returns the localized economy charge confirmation message.
         *
         * @param cost the charged amount to substitute
         */
        public static String economyCharged(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_CHARGED.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        /**
         * Returns the localized "economy not available" message.
         */
        public static String economyNotAvailable() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_NOT_AVAILABLE.getKey()));
        }

        /**
         * Returns the localized cooldown message shown when a player must wait.
         *
         * @param seconds the remaining wait time to substitute
         */
        public static String cooldownActive(String seconds) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_COOLDOWN_ACTIVE.getKey(), "&cYou must wait &f{seconds}s &cbefore doing that again.&r")
                            .replace("{seconds}", seconds)
            );
        }

        /**
         * Returns the localized message shown when maintenance mode is active.
         */
        public static String maintenanceActive() {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MAINTENANCE_ACTIVE.getKey(), "&cDisenchantment is currently under maintenance. Please try again later.&r")
            );
        }

        /**
         * Returns the localized message confirming maintenance mode was enabled.
         */
        public static String maintenanceIsEnabled() {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MAINTENANCE_IS_ENABLED.getKey(), "&aMaintenance mode is now &fenabled&a.&r")
            );
        }

        /**
         * Returns the localized message confirming maintenance mode was disabled.
         */
        public static String maintenanceIsDisabled() {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_MAINTENANCE_IS_DISABLED.getKey(), "&aMaintenance mode is now &fdisabled&a.&r")
            );
        }

        /**
         * Returns the localized message confirming the configuration was reloaded.
         */
        public static String configReloaded() {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_CONFIG_RELOADED.getKey(), "&aConfig reloaded.&r")
            );
        }

        /**
         * Returns the localized message shown when a reload is refused because config.yml has a syntax error.
         *
         * @param error the parser error message to substitute
         */
        public static String reloadFailed(String error) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_RELOAD_FAILED.getKey(), "&cconfig.yml has a syntax error and was not loaded; the running settings are unchanged: &f{error}&r")
                            .replace("{error}", error)
            );
        }

        /**
         * Returns the localized warning that some enchantments may not transfer due to chance settings.
         */
        public static String someEnchantmentsMayNotTransfer() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SOME_ENCHANTMENTS_MAY_NOT_TRANSFER.getKey(), "&7Some enchantments may not transfer"));
        }

        /**
         * Returns the localized "specify enchantment and chance" prompt.
         */
        public static String specifyEnchantmentChance() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIFY_ENCHANTMENT_CHANCE.getKey(), "&cSpecify an enchantment and a chance between 0.0 and 1.0"));
        }

        /**
         * Returns the localized message confirming an enchantment chance was set.
         *
         * @param enchantment the enchantment name to substitute
         * @param chance      the chance value to substitute
         */
        public static String enchantmentChanceIsSet(String enchantment, String chance) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENT_CHANCE_IS_SET.getKey(), "&aChance for {enchantment} set to {chance}")
                            .replace("{enchantment}", enchantment)
                            .replace("{chance}", chance)
            );
        }

        /**
         * Returns the localized message confirming economy integration was enabled.
         */
        public static String economyEnabledIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_ENABLED_IS_ENABLED.getKey()));
        }

        /**
         * Returns the localized message confirming economy integration was disabled.
         */
        public static String economyEnabledIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_ENABLED_IS_DISABLED.getKey()));
        }

        /**
         * Returns the localized message confirming the economy cost was set.
         *
         * @param cost the new cost value to substitute
         */
        public static String economyCostIsSet(String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_COST_IS_SET.getKey(), "default")
                            .replace("{cost}", cost)
            );
        }

        /**
         * Returns the localized message confirming the "show cost" option was enabled.
         */
        public static String economyShowCostIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_SHOW_COST_IS_ENABLED.getKey()));
        }

        /**
         * Returns the localized message confirming the "show cost" option was disabled.
         */
        public static String economyShowCostIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_SHOW_COST_IS_DISABLED.getKey()));
        }

        /**
         * Returns the localized message confirming the charge message was enabled.
         */
        public static String economyChargeMessageIsEnabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_CHARGE_MESSAGE_IS_ENABLED.getKey()));
        }

        /**
         * Returns the localized message confirming the charge message was disabled.
         */
        public static String economyChargeMessageIsDisabled() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_CHARGE_MESSAGE_IS_DISABLED.getKey()));
        }

        // ------------------------------------------------------------------------------------------
        // Anvil event feedback messages (audit fixes)

        /**
         * Returns the localized message shown when the economy provider rejected the withdrawal.
         */
        public static String economyWithdrawFailed() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ECONOMY_WITHDRAW_FAILED.getKey(),
                    "&cThe payment could not be processed. You have not been charged.&r"));
        }

        /**
         * Returns the localized message shown when the player lacks the experience levels for an operation.
         *
         * @param levels the value substituted for {levels}
         */
        public static String notEnoughLevels(String levels) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_NOT_ENOUGH_LEVELS.getKey(), "&cYou need &f{levels} &clevels to do that.&r")
                            .replace("{levels}", levels)
            );
        }

        /**
         * Returns the localized message shown when every enchantment failed its chance roll and a plain book was delivered.
         */
        public static String enchantmentsFailedToTransfer() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_ENCHANTMENTS_FAILED_TO_TRANSFER.getKey(),
                    "&cThe enchantments failed to transfer. You received a plain book instead.&r"));
        }

        /**
         * Returns the localized hint shown when a non-standard click (shift-click, number key, ...) on the result slot was blocked.
         */
        public static String useNormalClick() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_USE_NORMAL_CLICK.getKey(),
                    "&cTake the result with a normal left or right click.&r"));
        }

        /**
         * Returns the localized preview-lore line listing the enchantments a result book transfers.
         *
         * @param enchantments the value substituted for {enchantments}
         */
        public static String previewTransfers(String enchantments) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_PREVIEW_TRANSFERS.getKey(), "&7Transfers: &f{enchantments}&r")
                            .replace("{enchantments}", enchantments)
            );
        }

        /**
         * Returns the localized preview-lore line showing an experience-level cost.
         *
         * @param levels the value substituted for {levels}
         */
        public static String previewCostLevels(String levels) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_PREVIEW_COST_LEVELS.getKey(), "&7Cost: &f{levels} levels&r")
                            .replace("{levels}", levels)
            );
        }

        /**
         * Returns the localized preview-lore line showing an experience-level and a money cost.
         *
         * @param levels the value substituted for {levels}
         * @param money  the formatted amount substituted for {money}
         */
        public static String previewCostLevelsMoney(String levels, String money) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_PREVIEW_COST_LEVELS_MONEY.getKey(), "&7Cost: &f{levels} levels &7+ &f{money}&r")
                            .replace("{levels}", levels)
                            .replace("{money}", money)
            );
        }

        /**
         * Returns the localized preview-lore line showing a money-only cost.
         *
         * @param money the formatted amount substituted for {money}
         */
        public static String previewCostMoney(String money) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_PREVIEW_COST_MONEY.getKey(), "&7Cost: &f{money}&r")
                            .replace("{money}", money)
            );
        }

        /**
         * Returns the localized hint shown when an anvil operation was refused because the anvil is not a special anvil.
         */
        public static String specialAnvilRequired() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_SPECIAL_ANVIL_REQUIRED.getKey(),
                    "&7This only works at a special anvil.&r"));
        }

        /**
         * Returns the localized usage line of the give anvil command.
         */
        public static String giveAnvilUsage() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_GIVE_ANVIL_USAGE.getKey(),
                    "&cUsage: /disenchantment give anvil <player> [amount]&r"));
        }

        /**
         * Returns the localized message shown when the give command target is not online.
         *
         * @param player the value substituted for {player}
         */
        public static String givePlayerNotFound(String player) {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_GIVE_PLAYER_NOT_FOUND.getKey(),
                    "&cPlayer {player} is not online!&r").replace("{player}", player));
        }

        /**
         * Returns the localized message shown when the give command amount is out of range.
         *
         * @param max the value substituted for {max}
         */
        public static String giveAmountInvalid(String max) {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_GIVE_AMOUNT_INVALID.getKey(),
                    "&cThe amount must be a whole number between 1 and {max}!&r").replace("{max}", max));
        }

        /**
         * Returns the localized confirmation sent to the sender after giving special anvils.
         *
         * @param amount the value substituted for {amount}
         * @param player the value substituted for {player}
         */
        public static String giveAnvilGiven(String amount, String player) {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_GIVE_ANVIL_GIVEN.getKey(),
                            "&aGave &f{amount}x &aspecial anvil to &f{player}&a.&r")
                    .replace("{amount}", amount).replace("{player}", player));
        }

        /**
         * Returns the localized notice sent to the receiver of special anvils.
         *
         * @param amount the value substituted for {amount}
         */
        public static String giveAnvilReceived(String amount) {
            return I18n.translateColors(localeConfig.getString(I18nKeys.MESSAGES_GIVE_ANVIL_RECEIVED.getKey(),
                    "&7You received &f{amount}x &7special anvil.&r").replace("{amount}", amount));
        }

        /**
         * Returns the localized action bar text showing the experience cost of a grindstone operation.
         *
         * @param levels the value substituted for {levels}
         */
        public static String grindstoneCost(String levels) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_GRINDSTONE_COST.getKey(), "&7Cost: &f{levels} &7levels&r")
                            .replace("{levels}", levels)
            );
        }

        /**
         * Returns the localized action bar text showing the experience and economy cost of a grindstone operation.
         *
         * @param levels the value substituted for {levels}
         * @param cost   the value substituted for {cost}
         */
        public static String grindstoneCostEconomy(String levels, String cost) {
            return I18n.translateColors(
                    localeConfig.getString(I18nKeys.MESSAGES_GRINDSTONE_COST_ECONOMY.getKey(), "&7Cost: &f{levels} &7levels &8+ &f{cost}&r")
                            .replace("{levels}", levels)
                            .replace("{cost}", cost)
            );
        }
    }

    /**
     * Localized display names for enchantment state types.
     */
    public static class States {
        /**
         * Returns the localized display name for the "enable" state.
         */
        public static String enable() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_ENABLE.getKey()));
        }

        /**
         * Returns the localized display name for the "keep" state.
         */
        public static String keep() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_KEEP.getKey()));
        }

        /**
         * Returns the localized display name for the "delete" state.
         */
        public static String delete() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.STATES_DELETE.getKey()));
        }

        /**
         * Returns the localized display name for the "disable" state.
         */
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
            /**
             * Returns the localized help page title with the page number substituted.
             *
             * @param page the page number to substitute
             */
            public static String title(String page) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_HELP_TITLE.getKey(), "default")
                                .replace("{page}", page)
                );
            }

            /**
             * Returns all localized help page lines grouped by page number.
             */
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
            /**
             * Returns the localized status command title.
             */
            public static String title() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_TITLE.getKey()));
            }

            /**
             * Returns the localized global plugin state line.
             *
             * @param state the state label to substitute
             */
            public static String global(String state) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_STATUS_GLOBAL.getKey(), "default")
                                .replace("{state}", state)
                );
            }

            /**
             * Returns the localized disenchantment state line.
             *
             * @param state the state label to substitute
             */
            public static String disenchantment(String state) {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_DISENCHANTMENT.getKey(), "default")
                        .replace("{state}", state)
                );
            }

            /**
             * Returns the localized shatterment state line.
             *
             * @param state the state label to substitute
             */
            public static String shatterment(String state) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_STATUS_SHATTERMENT.getKey(), "default")
                                .replace("{state}", state)
                );
            }

            /**
             * Localized state labels used in the status command output.
             */
            public static class States {
                /**
                 * Returns the localized "enabled" state label.
                 */
                public static String enabled() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_STATES_ENABLED.getKey()));
                }

                /**
                 * Returns the localized "disabled" state label.
                 */
                public static String disabled() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_STATUS_STATES_DISABLED.getKey()));
                }
            }
        }

        /**
         * Localized strings for the enchantments command output.
         */
        public static class Enchantments {
            /**
             * Localized strings for the disenchantment enchantments command output.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment enchantments list title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_DISENCHANTMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized "no enchantment overrides configured" message.
                 */
                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_DISENCHANTMENT_EMPTY.getKey()));
                }

                /**
                 * Returns the localized per-enchantment state line.
                 *
                 * @param enchantment the enchantment name to substitute
                 * @param state       the state label to substitute
                 */
                public static String enchantment(String enchantment, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_DISENCHANTMENT_ENCHANTMENT.getKey(), "default")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{state}", state)
                    );
                }
            }

            /**
             * Localized strings for the shatterment enchantments command output.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment enchantments list title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_SHATTERMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized "no enchantment overrides configured" message.
                 */
                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ENCHANTMENTS_SHATTERMENT_EMPTY.getKey()));
                }

                /**
                 * Returns the localized per-enchantment state line.
                 *
                 * @param enchantment the enchantment name to substitute
                 * @param state       the state label to substitute
                 */
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
            /**
             * Localized strings for the disenchantment chances command output.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment chances list title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_DISENCHANTMENT_TITLE.getKey(), "&6Disenchantment enchantment chances:"));
                }

                /**
                 * Returns the localized "no custom chances configured" message.
                 */
                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_DISENCHANTMENT_EMPTY.getKey(), "No custom chances configured."));
                }

                /**
                 * Returns the localized per-enchantment chance line.
                 *
                 * @param enchantment the enchantment name to substitute
                 * @param chance      the chance value to substitute
                 */
                public static String enchantment(String enchantment, String chance) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_CHANCES_DISENCHANTMENT_ENCHANTMENT.getKey(), "&7{enchantment}: &f{chance}")
                                    .replace("{enchantment}", enchantment)
                                    .replace("{chance}", chance)
                    );
                }
            }

            /**
             * Localized strings for the shatterment chances command output.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment chances list title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_SHATTERMENT_TITLE.getKey(), "&6Shatterment enchantment chances:"));
                }

                /**
                 * Returns the localized "no custom chances configured" message.
                 */
                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_CHANCES_SHATTERMENT_EMPTY.getKey(), "No custom chances configured."));
                }

                /**
                 * Returns the localized per-enchantment chance line.
                 *
                 * @param enchantment the enchantment name to substitute
                 * @param chance      the chance value to substitute
                 */
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
            /**
             * Returns the localized disabled-materials list title.
             */
            public static String title() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_TITLE.getKey()));
            }

            /**
             * Returns the localized "no materials disabled" message.
             */
            public static String empty() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_EMPTY.getKey()));
            }

            /**
             * Returns the localized per-material state line.
             *
             * @param material the material name to substitute
             * @param state    the state label to substitute
             */
            public static String material(String material, String state) {
                return I18n.translateColors(
                        localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_MATERIAL.getKey(), "default")
                                .replace("{material}", material)
                                .replace("{state}", state)
                );
            }

            /**
             * Localized state labels used in the materials command output.
             */
            public static class States {
                /**
                 * Returns the localized "disabled" state label.
                 */
                public static String disabled() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_MATERIALS_STATES_DISABLED.getKey()));
                }
            }
        }

        /**
         * Localized strings for the repair cost command output.
         */
        public static class Repair {
            /**
             * Localized strings for the disenchantment repair command output.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment repair settings title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized repair cost state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String cost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized repair cost reset state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String reset(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_RESET.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized base repair cost line.
                 *
                 * @param cost the cost value to substitute
                 */
                public static String base(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_BASE.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                /**
                 * Returns the localized repair cost multiplier line.
                 *
                 * @param multiply the multiplier value to substitute
                 */
                public static String multiply(String multiply) {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_MULTIPLY.getKey(), "default")
                            .replace("{multiply}", multiply)
                    );
                }

                /**
                 * Localized state labels used in the disenchantment repair command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "enabled" state label.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized "disabled" state label.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the shatterment repair command output.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment repair settings title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized repair cost state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String cost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized repair cost reset state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String reset(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_RESET.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized base repair cost line.
                 *
                 * @param cost the cost value to substitute
                 */
                public static String base(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_BASE.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                /**
                 * Returns the localized repair cost multiplier line.
                 *
                 * @param multiply the multiplier value to substitute
                 */
                public static String multiply(String multiply) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_MULTIPLY.getKey(), "default")
                                    .replace("{multiply}", multiply)
                    );
                }

                /**
                 * Localized state labels used in the shatterment repair command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "enabled" state label.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_REPAIR_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized "disabled" state label.
                     */
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
            /**
             * Localized strings for the disenchantment sound command output.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment sound settings title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized sound enabled/disabled state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String sound(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_SOUND.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized sound volume line.
                 *
                 * @param volume the volume value to substitute
                 */
                public static String volume(String volume) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_VOLUME.getKey(), "default")
                                    .replace("{volume}", volume)
                    );
                }

                /**
                 * Returns the localized sound pitch line.
                 *
                 * @param pitch the pitch value to substitute
                 */
                public static String pitch(String pitch) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_PITCH.getKey(), "default")
                                    .replace("{pitch}", pitch)
                    );
                }

                /**
                 * Localized state labels used in the disenchantment sound command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "enabled" state label.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized "disabled" state label.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the shatterment sound command output.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment sound settings title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized sound enabled/disabled state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String sound(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_SOUND.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized sound volume line.
                 *
                 * @param volume the volume value to substitute
                 */
                public static String volume(String volume) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_VOLUME.getKey(), "default")
                                    .replace("{volume}", volume)
                    );
                }

                /**
                 * Returns the localized sound pitch line.
                 *
                 * @param pitch the pitch value to substitute
                 */
                public static String pitch(String pitch) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_PITCH.getKey(), "default")
                                    .replace("{pitch}", pitch)
                    );
                }

                /**
                 * Localized state labels used in the shatterment sound command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "enabled" state label.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_SOUND_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized "disabled" state label.
                     */
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
            /**
             * Localized strings for the disenchantment worlds command output.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment disabled-worlds list title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized "no worlds disabled" message.
                 */
                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_EMPTY.getKey()));
                }

                /**
                 * Returns the localized per-world state line.
                 *
                 * @param world the world name to substitute
                 * @param state the state label to substitute
                 */
                public static String world(String world, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_WORLD.getKey(), "default")
                                    .replace("{world}", world)
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Localized state labels used in the disenchantment worlds command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "disabled" state label.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the shatterment worlds command output.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment disabled-worlds list title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized "no worlds disabled" message.
                 */
                public static String empty() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_EMPTY.getKey()));
                }

                /**
                 * Returns the localized per-world state line.
                 *
                 * @param world the world name to substitute
                 * @param state the state label to substitute
                 */
                public static String world(String world, String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_WORLDS_SHATTERMENT_WORLD.getKey(), "default")
                                    .replace("{world}", world)
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Localized state labels used in the shatterment worlds command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "disabled" state label.
                     */
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
            /**
             * Localized strings for the disenchantment economy command output.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment economy settings title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized economy enabled/disabled state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String enabled(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_ENABLED.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized economy cost line.
                 *
                 * @param cost the cost value to substitute
                 */
                public static String cost(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_COST.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                /**
                 * Returns the localized "show cost" setting line.
                 *
                 * @param state the state label to substitute
                 */
                public static String showCost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_SHOW_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized "charge message" setting line.
                 *
                 * @param state the state label to substitute
                 */
                public static String chargeMessage(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Localized state labels used in the disenchantment economy command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "enabled" state label.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_STATES_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized "disabled" state label.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_DISENCHANTMENT_STATES_DISABLED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the shatterment economy command output.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment economy settings title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_TITLE.getKey()));
                }

                /**
                 * Returns the localized economy enabled/disabled state line.
                 *
                 * @param state the state label to substitute
                 */
                public static String enabled(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_ENABLED.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized economy cost line.
                 *
                 * @param cost the cost value to substitute
                 */
                public static String cost(String cost) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_COST.getKey(), "default")
                                    .replace("{cost}", cost)
                    );
                }

                /**
                 * Returns the localized "show cost" setting line.
                 *
                 * @param state the state label to substitute
                 */
                public static String showCost(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_SHOW_COST.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized "charge message" setting line.
                 *
                 * @param state the state label to substitute
                 */
                public static String chargeMessage(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_CHARGE_MESSAGE.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Localized state labels used in the shatterment economy command output.
                 */
                public static class States {
                    /**
                     * Returns the localized "enabled" state label.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.COMMANDS_ECONOMY_SHATTERMENT_STATES_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized "disabled" state label.
                     */
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
        /**
         * Returns the localized "back" button label.
         */
        public static String back() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_BACK.getKey()));
        }

        /**
         * Returns the localized "previous page" button label.
         */
        public static String previous() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_PREVIOUS.getKey()));
        }

        /**
         * Returns the localized "next page" button label.
         */
        public static String next() {
            return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NEXT.getKey()));
        }

        /**
         * Localized strings for the main navigation GUI.
         */
        public static class Navigation {
            /**
             * Returns the localized navigation inventory title.
             */
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_INVENTORY.getKey()));
            }

            /**
             * Localized strings for the plugin-toggle navigation item.
             */
            public static class Plugin {
                /**
                 * Returns the localized plugin-toggle item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_PLUGIN_TITLE.getKey()));
                }

                /**
                 * Localized lore lines for the plugin-toggle navigation item.
                 */
                public static class Lore {
                    /**
                     * Returns the localized lore when the plugin is enabled.
                     */
                    public static List<String> enabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_PLUGIN_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    /**
                     * Returns the localized lore when the plugin is disabled.
                     */
                    public static List<String> disabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_PLUGIN_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }
            }

            /**
             * Localized strings for the worlds navigation item.
             */
            public static class Worlds {
                /**
                 * Returns the localized worlds navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_WORLDS_TITLE.getKey()));
                }

                /**
                 * Returns the localized worlds navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_WORLDS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the repair navigation item.
             */
            public static class Repair {
                /**
                 * Returns the localized repair navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_REPAIR_TITLE.getKey()));
                }

                /**
                 * Returns the localized repair navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_REPAIR_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the enchantments navigation item.
             */
            public static class Enchantments {
                /**
                 * Returns the localized enchantments navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_ENCHANTMENTS_TITLE.getKey()));
                }

                /**
                 * Returns the localized enchantments navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_ENCHANTMENTS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the materials navigation item.
             */
            public static class Materials {
                /**
                 * Returns the localized materials navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_MATERIALS_TITLE.getKey()));
                }

                /**
                 * Returns the localized materials navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_MATERIALS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the sound navigation item.
             */
            public static class Sound {
                /**
                 * Returns the localized sound navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_SOUND_TITLE.getKey()));
                }

                /**
                 * Returns the localized sound navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_SOUND_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the Spigot page navigation item.
             */
            public static class Spigot {
                /**
                 * Returns the localized Spigot navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_SPIGOT_TITLE.getKey()));
                }

                /**
                 * Returns the localized Spigot navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_SPIGOT_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the economy navigation item.
             */
            public static class Economy {
                /**
                 * Returns the localized economy navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_ECONOMY_TITLE.getKey()));
                }

                /**
                 * Returns the localized economy navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_ECONOMY_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the stats navigation item.
             */
            public static class Stats {
                /**
                 * Returns the localized stats navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_STATS_TITLE.getKey()));
                }

                /**
                 * Returns the localized stats navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_STATS_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the split-count navigation item.
             */
            public static class SplitCount {
                /**
                 * Returns the localized split-count navigation item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_NAVIGATION_SPLIT_COUNT_TITLE.getKey()));
                }

                /**
                 * Returns the localized split-count navigation item lore.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_NAVIGATION_SPLIT_COUNT_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        /**
         * Localized strings for the worlds GUI screen.
         */
        public static class Worlds {
            /**
             * Returns the localized worlds inventory title.
             */
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_INVENTORY.getKey()));
            }

            /**
             * Localized lore lines shown on world items.
             */
            public static class Lore {
                /**
                 * Localized disenchantment state lore for world items.
                 */
                public static class Disenchantment {
                    /**
                     * Returns the localized lore when disenchantment is enabled in this world.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_DISENCHANTMENT_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized lore when disenchantment is disabled in this world.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_DISENCHANTMENT_DISABLED.getKey()));
                    }
                }

                /**
                 * Localized shatterment state lore for world items.
                 */
                public static class Shatterment {
                    /**
                     * Returns the localized lore when shatterment is enabled in this world.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_SHATTERMENT_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized lore when shatterment is disabled in this world.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_LORE_SHATTERMENT_DISABLED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the help item in the worlds GUI.
             */
            public static class Help {
                /**
                 * Returns the localized help item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_HELP_TITLE.getKey()));
                }

                /**
                 * Returns the localized help item lore lines.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_WORLDS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the restriction-mode button in the worlds GUI.
             */
            public static class Mode {
                /**
                 * Returns the localized mode button title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_WORLDS_MODE_TITLE.getKey()));
                }

                /**
                 * Returns the localized mode button lore with the current mode labels substituted.
                 *
                 * @param disenchantment the disenchantment mode label
                 * @param shatterment    the shatterment mode label
                 */
                public static List<String> lore(String disenchantment, String shatterment) {
                    return localeConfig.getStringList(I18nKeys.GUI_WORLDS_MODE_LORE.getKey())
                            .stream()
                            .map(line -> line.replace("{disenchantment}", disenchantment).replace("{shatterment}", shatterment))
                            .map(I18n::translateColors)
                            .toList();
                }
            }
        }

        /**
         * Localized strings for the repair GUI screens.
         */
        public static class Repair {
            /**
             * Localized strings for the disenchantment repair GUI screen.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment repair inventory title.
                 */
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_INVENTORY.getKey()));
                }

                /**
                 * Localized strings for the repair cost toggle item.
                 */
                public static class Cost {
                    /**
                     * Returns the localized repair cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_COST_LORE_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the repair cost toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when repair cost is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when repair cost is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the repair cost reset toggle item.
                 */
                public static class Reset {
                    /**
                     * Returns the localized repair cost reset item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_RESET_LORE_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the repair cost reset toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when repair cost reset is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_RESET_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when repair cost reset is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_RESET_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the base repair cost item.
                 */
                public static class Base {
                    /**
                     * Returns the localized base repair cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_BASE_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized base repair cost item lore with the current cost substituted.
                     *
                     * @param cost the cost value to substitute
                     */
                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_BASE_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the repair cost multiplier item.
                 */
                public static class Multiplier {
                    /**
                     * Returns the localized repair cost multiplier item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized repair cost multiplier item lore with the multiplier substituted.
                     *
                     * @param multiplier the multiplier value to substitute
                     */
                    public static List<String> lore(String multiplier) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MULTIPLIER_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{multiplier}", multiplier))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the max XP cost cap item.
                 */
                public static class MaxCost {
                    /**
                     * Returns the localized max cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MAXCOST_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized max cost item lore with the current cap substituted.
                     *
                     * @param cost the cap value to substitute
                     */
                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MAXCOST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }

                    /**
                     * Returns the localized "unlimited" label shown when no cap is set.
                     */
                    public static String unlimited() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_DISENCHANTMENT_MAXCOST_UNLIMITED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the shatterment repair GUI screen.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment repair inventory title.
                 */
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_INVENTORY.getKey()));
                }

                /**
                 * Localized strings for the repair cost toggle item.
                 */
                public static class Cost {
                    /**
                     * Returns the localized repair cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_COST_LORE_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the repair cost toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when repair cost is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when repair cost is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the repair cost reset toggle item.
                 */
                public static class Reset {
                    /**
                     * Returns the localized repair cost reset item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_RESET_LORE_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the repair cost reset toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when repair cost reset is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_RESET_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when repair cost reset is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_RESET_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the base repair cost item.
                 */
                public static class Base {
                    /**
                     * Returns the localized base repair cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_BASE_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized base repair cost item lore with the current cost substituted.
                     *
                     * @param cost the cost value to substitute
                     */
                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_BASE_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the repair cost multiplier item.
                 */
                public static class Multiplier {
                    /**
                     * Returns the localized repair cost multiplier item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_MULTIPLIER_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized repair cost multiplier item lore with the multiplier substituted.
                     *
                     * @param multiplier the multiplier value to substitute
                     */
                    public static List<String> lore(String multiplier) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_MULTIPLIER_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{multiplier}", multiplier))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the max XP cost cap item.
                 */
                public static class MaxCost {
                    /**
                     * Returns the localized max cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_MAXCOST_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized max cost item lore with the current cap substituted.
                     *
                     * @param cost the cap value to substitute
                     */
                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_REPAIR_SHATTERMENT_MAXCOST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }

                    /**
                     * Returns the localized "unlimited" label shown when no cap is set.
                     */
                    public static String unlimited() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_REPAIR_SHATTERMENT_MAXCOST_UNLIMITED.getKey()));
                    }
                }
            }
        }

        /**
         * Localized strings for the enchantments GUI screen.
         */
        public static class Enchantments {
            /**
             * Returns the localized enchantments inventory title.
             */
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_INVENTORY.getKey()));
            }

            /**
             * Localized lore lines shown on enchantment items.
             */
            public static class Lore {
                /**
                 * Returns the localized disenchantment state lore line for an enchantment item.
                 *
                 * @param state the state label to substitute
                 */
                public static String disenchantment(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_LORE_DISENCHANTMENT.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }

                /**
                 * Returns the localized shatterment state lore line for an enchantment item.
                 *
                 * @param state the state label to substitute
                 */
                public static String shatterment(String state) {
                    return I18n.translateColors(
                            localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_LORE_SHATTERMENT.getKey(), "default")
                                    .replace("{state}", state)
                    );
                }
            }

            /**
             * Localized strings for the help item in the enchantments GUI.
             */
            public static class Help {
                /**
                 * Returns the localized help item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ENCHANTMENTS_HELP_TITLE.getKey()));
                }

                /**
                 * Returns the localized help item lore lines.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_ENCHANTMENTS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }

        /**
         * Localized strings for the materials GUI screen.
         */
        public static class Materials {
            /**
             * Returns the localized materials inventory title.
             */
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_INVENTORY.getKey()));
            }

            /**
             * Localized lore lines shown on material items.
             */
            public static class Lore {
                /**
                 * Localized disenchantment state lore for material items.
                 */
                public static class Disenchantment {
                    /**
                     * Returns the localized lore when disenchantment is enabled for this material.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_DISENCHANTMENT_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized lore when disenchantment is disabled for this material.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_DISENCHANTMENT_DISABLED.getKey()));
                    }
                }

                /**
                 * Localized shatterment state lore for material items.
                 */
                public static class Shatterment {
                    /**
                     * Returns the localized lore when shatterment is enabled for this material.
                     */
                    public static String enabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_SHATTERMENT_ENABLED.getKey()));
                    }

                    /**
                     * Returns the localized lore when shatterment is disabled for this material.
                     */
                    public static String disabled() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_LORE_SHATTERMENT_DISABLED.getKey()));
                    }
                }
            }

            /**
             * Localized strings for the help item in the materials GUI.
             */
            public static class Help {
                /**
                 * Returns the localized help item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_HELP_TITLE.getKey()));
                }

                /**
                 * Returns the localized help item lore lines.
                 */
                public static List<String> lore() {
                    return localeConfig.getStringList(I18nKeys.GUI_MATERIALS_HELP_LORE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }

            /**
             * Localized strings for the restriction-mode button in the materials GUI.
             */
            public static class Mode {
                /**
                 * Returns the localized mode button title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_MATERIALS_MODE_TITLE.getKey()));
                }

                /**
                 * Returns the localized mode button lore with the current mode labels substituted.
                 *
                 * @param disenchantment the disenchantment mode label
                 * @param shatterment    the shatterment mode label
                 */
                public static List<String> lore(String disenchantment, String shatterment) {
                    return localeConfig.getStringList(I18nKeys.GUI_MATERIALS_MODE_LORE.getKey())
                            .stream()
                            .map(line -> line.replace("{disenchantment}", disenchantment).replace("{shatterment}", shatterment))
                            .map(I18n::translateColors)
                            .toList();
                }
            }
        }

        /**
         * Localized labels for {@link com.jankominek.disenchantment.types.RestrictionMode} values,
         * shared by the worlds and materials mode buttons.
         */
        public static class RestrictionMode {
            /**
             * Returns the localized ALLOWLIST label.
             */
            public static String allowlist() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_RESTRICTION_MODE_ALLOWLIST.getKey()));
            }

            /**
             * Returns the localized DENYLIST label.
             */
            public static String denylist() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_RESTRICTION_MODE_DENYLIST.getKey()));
            }

            /**
             * Returns the localized label for the given mode.
             *
             * @param mode the mode to label
             */
            public static String label(com.jankominek.disenchantment.types.RestrictionMode mode) {
                return mode == com.jankominek.disenchantment.types.RestrictionMode.ALLOWLIST ? allowlist() : denylist();
            }
        }

        /**
         * Localized strings for the sound GUI screens.
         */
        public static class Sound {
            /**
             * Localized strings for the disenchantment sound GUI screen.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment sound inventory title.
                 */
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_INVENTORY.getKey()));
                }

                /**
                 * Returns the localized sound toggle item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_TITLE.getKey()));
                }

                /**
                 * Localized lore for the sound toggle item.
                 */
                public static class Lore {
                    /**
                     * Returns the localized lore when sound is enabled.
                     */
                    public static List<String> enabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    /**
                     * Returns the localized lore when sound is disabled.
                     */
                    public static List<String> disabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                /**
                 * Localized strings for the volume item.
                 */
                public static class Volume {
                    /**
                     * Returns the localized volume item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_VOLUME_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized volume item lore with the current volume substituted.
                     *
                     * @param volume the volume value to substitute
                     */
                    public static List<String> lore(String volume) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_VOLUME_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{volume}", volume))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the pitch item.
                 */
                public static class Pitch {
                    /**
                     * Returns the localized pitch item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_DISENCHANTMENT_PITCH_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized pitch item lore with the current pitch substituted.
                     *
                     * @param pitch the pitch value to substitute
                     */
                    public static List<String> lore(String pitch) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_DISENCHANTMENT_PITCH_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{pitch}", pitch))
                                .toList();
                    }
                }
            }

            /**
             * Localized strings for the shatterment sound GUI screen.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment sound inventory title.
                 */
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_INVENTORY.getKey()));
                }

                /**
                 * Returns the localized sound toggle item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_TITLE.getKey()));
                }

                /**
                 * Localized lore for the sound toggle item.
                 */
                public static class Lore {
                    /**
                     * Returns the localized lore when sound is enabled.
                     */
                    public static List<String> enabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_LORE_ENABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }

                    /**
                     * Returns the localized lore when sound is disabled.
                     */
                    public static List<String> disabled() {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_LORE_DISABLED.getKey())
                                .stream().map(I18n::translateColors).toList();
                    }
                }

                /**
                 * Localized strings for the volume item.
                 */
                public static class Volume {
                    /**
                     * Returns the localized volume item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_VOLUME_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized volume item lore with the current volume substituted.
                     *
                     * @param volume the volume value to substitute
                     */
                    public static List<String> lore(String volume) {
                        return localeConfig.getStringList(I18nKeys.GUI_SOUND_SHATTERMENT_VOLUME_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{volume}", volume))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the pitch item.
                 */
                public static class Pitch {
                    /**
                     * Returns the localized pitch item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SOUND_SHATTERMENT_PITCH_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized pitch item lore with the current pitch substituted.
                     *
                     * @param pitch the pitch value to substitute
                     */
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

        /**
         * Localized strings for the economy GUI screens.
         */
        public static class Economy {
            /**
             * Localized strings for the disenchantment economy GUI screen.
             */
            public static class Disenchantment {
                /**
                 * Returns the localized disenchantment economy inventory title.
                 */
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_INVENTORY.getKey()));
                }

                /**
                 * Localized strings for the economy enabled toggle item.
                 */
                public static class Enabled {
                    /**
                     * Returns the localized economy enabled toggle item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_ENABLED_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the economy enabled toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when economy is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_ENABLED_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when economy is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_ENABLED_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the economy cost item.
                 */
                public static class Cost {
                    /**
                     * Returns the localized economy cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_COST_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized economy cost item lore with the current cost substituted.
                     *
                     * @param cost the cost value to substitute
                     */
                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_COST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the "show cost" toggle item.
                 */
                public static class ShowCost {
                    /**
                     * Returns the localized "show cost" item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_SHOW_COST_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the "show cost" toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when "show cost" is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_SHOW_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when "show cost" is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_SHOW_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the charge message toggle item.
                 */
                public static class ChargeMessage {
                    /**
                     * Returns the localized charge message item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the charge message toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when the charge message is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when the charge message is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_DISENCHANTMENT_CHARGE_MESSAGE_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }
            }

            /**
             * Localized strings for the shatterment economy GUI screen.
             */
            public static class Shatterment {
                /**
                 * Returns the localized shatterment economy inventory title.
                 */
                public static String inventory() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_INVENTORY.getKey()));
                }

                /**
                 * Localized strings for the economy enabled toggle item.
                 */
                public static class Enabled {
                    /**
                     * Returns the localized economy enabled toggle item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_ENABLED_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the economy enabled toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when economy is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_ENABLED_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when economy is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_ENABLED_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the economy cost item.
                 */
                public static class Cost {
                    /**
                     * Returns the localized economy cost item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_COST_TITLE.getKey()));
                    }

                    /**
                     * Returns the localized economy cost item lore with the current cost substituted.
                     *
                     * @param cost the cost value to substitute
                     */
                    public static List<String> lore(String cost) {
                        return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_COST_LORE.getKey())
                                .stream()
                                .map(I18n::translateColors)
                                .map(s -> s.replace("{cost}", cost))
                                .toList();
                    }
                }

                /**
                 * Localized strings for the "show cost" toggle item.
                 */
                public static class ShowCost {
                    /**
                     * Returns the localized "show cost" item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_SHOW_COST_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the "show cost" toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when "show cost" is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_SHOW_COST_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when "show cost" is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_SHOW_COST_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }

                /**
                 * Localized strings for the charge message toggle item.
                 */
                public static class ChargeMessage {
                    /**
                     * Returns the localized charge message item title.
                     */
                    public static String title() {
                        return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_ECONOMY_SHATTERMENT_CHARGE_MESSAGE_TITLE.getKey()));
                    }

                    /**
                     * Localized lore for the charge message toggle item.
                     */
                    public static class Lore {
                        /**
                         * Returns the localized lore when the charge message is enabled.
                         */
                        public static List<String> enabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_CHARGE_MESSAGE_LORE_ENABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }

                        /**
                         * Returns the localized lore when the charge message is disabled.
                         */
                        public static List<String> disabled() {
                            return localeConfig.getStringList(I18nKeys.GUI_ECONOMY_SHATTERMENT_CHARGE_MESSAGE_LORE_DISABLED.getKey())
                                    .stream().map(I18n::translateColors).toList();
                        }
                    }
                }
            }
        }

        /**
         * Localized strings for the stats GUI screen.
         */
        public static class Stats {
            /**
             * Returns the localized stats inventory title.
             */
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_STATS_INVENTORY.getKey()));
            }
        }

        /**
         * Localized strings for the split-count GUI screen.
         */
        public static class SplitCount {
            /**
             * Returns the localized split-count inventory title.
             */
            public static String inventory() {
                return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SPLIT_COUNT_INVENTORY.getKey()));
            }

            /**
             * Localized strings for the split count value item.
             */
            public static class Count {
                /**
                 * Returns the localized split count item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SPLIT_COUNT_COUNT_TITLE.getKey()));
                }

                /**
                 * Returns the localized lore for a fixed split count with the value substituted.
                 *
                 * @param value the count value to substitute
                 */
                public static List<String> loreFixed(String value) {
                    return localeConfig.getStringList(I18nKeys.GUI_SPLIT_COUNT_COUNT_LORE_FIXED.getKey())
                            .stream().map(I18n::translateColors).map(s -> s.replace("{value}", value)).toList();
                }

                /**
                 * Returns the localized lore for the minimum split count with the value substituted.
                 *
                 * @param value the minimum value to substitute
                 */
                public static List<String> loreMin(String value) {
                    return localeConfig.getStringList(I18nKeys.GUI_SPLIT_COUNT_COUNT_LORE_MIN.getKey())
                            .stream().map(I18n::translateColors).map(s -> s.replace("{value}", value)).toList();
                }

                /**
                 * Returns the localized lore for the maximum split count with the value substituted.
                 *
                 * @param value the maximum value to substitute
                 */
                public static List<String> loreMax(String value) {
                    return localeConfig.getStringList(I18nKeys.GUI_SPLIT_COUNT_COUNT_LORE_MAX.getKey())
                            .stream().map(I18n::translateColors).map(s -> s.replace("{value}", value)).toList();
                }
            }

            /**
             * Localized strings for the split mode toggle item (fixed vs. range).
             */
            public static class Mode {
                /**
                 * Returns the localized mode toggle item title.
                 */
                public static String title() {
                    return I18n.translateColors(localeConfig.getString(I18nKeys.GUI_SPLIT_COUNT_MODE_TITLE.getKey()));
                }

                /**
                 * Returns the localized lore when the split count is fixed.
                 */
                public static List<String> loreFixed() {
                    return localeConfig.getStringList(I18nKeys.GUI_SPLIT_COUNT_MODE_LORE_FIXED.getKey())
                            .stream().map(I18n::translateColors).toList();
                }

                /**
                 * Returns the localized lore when the split count is a random range.
                 */
                public static List<String> loreRange() {
                    return localeConfig.getStringList(I18nKeys.GUI_SPLIT_COUNT_MODE_LORE_RANGE.getKey())
                            .stream().map(I18n::translateColors).toList();
                }
            }
        }
    }
}
