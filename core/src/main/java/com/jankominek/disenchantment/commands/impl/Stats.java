package com.jankominek.disenchantment.commands.impl;

import com.jankominek.disenchantment.commands.CommandBuilder;
import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.stats.StatsCache;
import com.jankominek.disenchantment.stats.StatsManager;
import com.jankominek.disenchantment.types.PermissionGroupType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Stats {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public static final CommandBuilder command = new CommandBuilder(
            "stats",
            PermissionGroupType.COMMAND_STATS,
            new String[]{},
            false,
            Stats::execute,
            Stats::complete
    );

    public static void execute(CommandSender s, String[] args) {
        if (!Config.Logging.isOperationsEnabled()) {
            s.sendMessage("§cOperation logging is not enabled. Set §flogging.operations: true §cin config.yml to enable stats.");
            return;
        }

        StatsManager manager = StatsManager.getInstance();
        if (manager == null) {
            s.sendMessage("§cStats are not available (manager not initialised).");
            return;
        }

        StatsCache cache = manager.getCache();

        if (args.length >= 2) {
            String playerName = args[1];
            @SuppressWarnings("deprecation")
            OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
            UUID uuid = op.getUniqueId();
            StatsCache.PlayerStats ps = cache.getPlayerStats(uuid);
            if (ps == null) {
                s.sendMessage("§7No stats recorded for §f" + playerName + "§7.");
                return;
            }
            s.sendMessage("§7§lStats — " + playerName);
            s.sendMessage("§7Disenchants: §f" + ps.disenchants());
            s.sendMessage("§7Shatters:    §f" + ps.shatters());
            s.sendMessage("§7XP spent:    §f" + ps.xpSpent());
            s.sendMessage("§7Money spent: §f" + ps.moneySpent());
            s.sendMessage("§7Last op:     §f" + ISO.format(ps.lastOperation()));
            return;
        }

        s.sendMessage("§7§lServer Stats");
        s.sendMessage("§7Disenchants: §f" + cache.getTotalDisenchants());
        s.sendMessage("§7Shatters:    §f" + cache.getTotalShatters());
        s.sendMessage("§7XP spent:    §f" + cache.getTotalXpSpent());
        s.sendMessage("§7Money spent: §f" + cache.getTotalMoneySpent());
        s.sendMessage("§7Top enchant: §f" + cache.getTopEnchantment());
    }

    public static List<String> complete(CommandSender sender, String[] args) {
        return new ArrayList<>(List.of());
    }
}
