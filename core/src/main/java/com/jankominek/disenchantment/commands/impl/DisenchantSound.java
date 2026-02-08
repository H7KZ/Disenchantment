package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "disenchant:sound" subcommand for managing sound settings
 * in the disenchantment feature. Supports viewing and modifying the sound
 * enabled state, volume, and pitch.
 */
public class DisenchantSound {
    /**
     * The command definition for the disenchant:sound subcommand.
     */
    public static final CommandBuilder command = new CommandBuilder(
            "disenchant:sound",
            PermissionGroupType.COMMAND_DISENCHANT_SOUND,
            new String[]{"enable", "disable", "volume", "pitch"},
            false,
            DisenchantSound::execute,
            DisenchantSound::complete
    );

    /**
     * Executes the disenchant:sound command. With no extra arguments, displays current
     * sound settings. With a setting name, updates the sound configuration (enable,
     * disable, volume, or pitch).
     *
     * @param s    the command sender
     * @param args the command arguments: [subcommand, setting, value]
     */
    public static void execute(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(I18n.Commands.Sound.Disenchantment.title());

            s.sendMessage(
                    I18n.Commands.Sound.Disenchantment.sound(
                            Config.Disenchantment.Anvil.Sound.isEnabled() ?
                                    I18n.Commands.Sound.Disenchantment.States.enabled() :
                                    I18n.Commands.Sound.Disenchantment.States.disabled()
                    )
            );
            s.sendMessage(I18n.Commands.Sound.Disenchantment.volume(String.valueOf(Config.Disenchantment.Anvil.Sound.getVolume())));
            s.sendMessage(I18n.Commands.Sound.Disenchantment.pitch(String.valueOf(Config.Disenchantment.Anvil.Sound.getPitch())));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "enable": {
                Config.Disenchantment.Anvil.Sound.setEnabled(true);

                s.sendMessage(I18n.Messages.soundIsEnabled());

                break;
            }
            case "disable": {
                Config.Disenchantment.Anvil.Sound.setEnabled(false);

                s.sendMessage(I18n.Messages.soundIsDisabled());

                break;
            }
            case "volume": {
                try {
                    Config.Disenchantment.Anvil.Sound.setVolume(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.soundVolumeIsSet(args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            case "pitch": {
                try {
                    Config.Disenchantment.Anvil.Sound.setPitch(Double.parseDouble(args[2]));

                    s.sendMessage(I18n.Messages.soundPitchIsSet(args[2]));
                } catch (NumberFormatException e) {
                    s.sendMessage(I18n.Messages.specifyValidDouble());
                }

                break;
            }
            default: {
                s.sendMessage(I18n.Messages.specifySoundState());
            }
        }
    }

    /**
     * Provides tab completion suggestions for sound setting names.
     *
     * @param sender the command sender
     * @param args   the current command arguments
     * @return a list of matching sound setting suggestions
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>(List.of());

        for (String arg : DisenchantSound.command.args()) {
            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                result.add(arg);
            }
        }

        return result;
    }
}
