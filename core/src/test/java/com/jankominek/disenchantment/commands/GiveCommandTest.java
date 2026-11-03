package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.anvils.SpecialAnvilItem;
import com.jankominek.disenchantment.commands.impl.Give;
import com.jankominek.disenchantment.config.I18n;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code /disenchantment give anvil <player> [amount]}.
 */
class GiveCommandTest extends DisenchantmentTestBase {

    private PlayerMock admin;
    private PlayerMock target;

    @BeforeEach
    void setUp() {
        admin = server.addPlayer("Admin");
        admin.setOp(true);
        target = server.addPlayer("Steve");
    }

    // MockBukkit round-trips messages through components, which drops redundant colour codes.
    private static String plain(String message) {
        return org.bukkit.ChatColor.stripColor(message);
    }

    private int specialAnvils(PlayerMock player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (SpecialAnvilItem.isSpecial(stack)) total += stack.getAmount();
        }
        return total;
    }

    @Test
    void givenPlayerAndAmount_whenGive_thenReceiverGetsSpecialAnvils() {
        Give.command.execute(admin, new String[]{"give", "anvil", "Steve", "3"});

        assertEquals(3, specialAnvils(target));
        assertEquals(plain(I18n.getPrefix() + " " + I18n.Messages.giveAnvilGiven("3", "Steve")), plain(admin.nextMessage()));
        assertEquals(plain(I18n.getPrefix() + " " + I18n.Messages.giveAnvilReceived("3")), plain(target.nextMessage()));
    }

    @Test
    void givenNoAmount_whenGive_thenOne() {
        Give.command.execute(admin, new String[]{"give", "anvil", "Steve"});

        assertEquals(1, specialAnvils(target));
    }

    @Test
    void givenSelfGive_whenGive_thenOnlyOneMessage() {
        Give.command.execute(admin, new String[]{"give", "anvil", "Admin", "2"});

        assertEquals(2, specialAnvils(admin));
        assertNotNull(admin.nextMessage());
        assertNull(admin.nextMessage());
    }

    @Test
    void givenFullInventory_whenGive_thenLeftoverDroppedAtFeet() {
        for (int i = 0; i < target.getInventory().getSize(); i++) target.getInventory().setItem(i, new ItemStack(Material.DIRT, 64));

        Give.command.execute(admin, new String[]{"give", "anvil", "Steve", "2"});

        assertEquals(0, specialAnvils(target));
        long dropped = target.getWorld().getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                .filter(item -> SpecialAnvilItem.isSpecial(item.getItemStack())).count();
        assertEquals(1, dropped);
    }

    @Test
    void givenUnknownPlayer_whenGive_thenNotFoundMessage() {
        Give.command.execute(admin, new String[]{"give", "anvil", "Nobody"});

        assertEquals(I18n.getPrefix() + " " + I18n.Messages.givePlayerNotFound("Nobody"), admin.nextMessage());
    }

    @Test
    void givenMissingPlayer_whenGive_thenUsageShown() {
        Give.command.execute(admin, new String[]{"give", "anvil"});

        assertEquals(I18n.getPrefix() + " " + I18n.Messages.giveAnvilUsage(), admin.nextMessage());
    }

    @Test
    void givenInvalidAmounts_whenGive_thenRejectedAndNothingGiven() {
        for (String amount : new String[]{"0", "-1", "65", "abc", "1.5"}) {
            Give.command.execute(admin, new String[]{"give", "anvil", "Steve", amount});

            assertEquals(I18n.getPrefix() + " " + I18n.Messages.giveAmountInvalid("64"), admin.nextMessage(), amount);
        }
        assertEquals(0, specialAnvils(target));
    }

    @Test
    void givenSenderWithoutPermission_whenGive_thenDenied() {
        Give.command.execute(target, new String[]{"give", "anvil", "Steve"});

        assertEquals(0, specialAnvils(target));
        assertEquals(I18n.getPrefix() + " " + I18n.Messages.requiresPermission(), target.nextMessage());
    }

    @Test
    void givenWrongSubArgument_whenGive_thenInvalidArgument() {
        Give.command.execute(admin, new String[]{"give", "sword", "Steve"});

        assertEquals(I18n.getPrefix() + " " + I18n.Messages.invalidArgument(), admin.nextMessage());
        assertEquals(0, specialAnvils(target));
    }

    @Test
    void givenCommandRegister_whenLookedUp_thenGiveRegistered() {
        assertSame(Give.command, CommandRegister.commands.get("give"));
    }

    // -> tab completion

    @Test
    void givenPartialInput_whenComplete_thenSuggestsAnvilPlayersAndAmounts() {
        assertEquals(List.of("anvil"), Give.complete(admin, new String[]{"give", "an"}));
        assertTrue(Give.complete(admin, new String[]{"give", "anvil", "St"}).contains("Steve"));
        assertFalse(Give.complete(admin, new String[]{"give", "anvil", "St"}).contains("Admin"));
        assertEquals(List.of("16"), Give.complete(admin, new String[]{"give", "anvil", "Steve", "1"}).stream().filter("16"::equals).toList());
        assertTrue(Give.complete(admin, new String[]{"give", "sword", ""}).isEmpty());
    }
}
