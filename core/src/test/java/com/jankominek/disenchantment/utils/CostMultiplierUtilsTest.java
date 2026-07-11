package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CostMultiplierUtilsTest extends DisenchantmentTestBase {

    @Test
    void givenNoDiscountPermission_whenGetMultiplier_thenReturnsOne() {
        PlayerMock player = server.addPlayer();

        assertEquals(1.0, CostMultiplierUtils.getMultiplier(player));
    }

    @Test
    void givenDiscount50Permission_whenGetMultiplier_thenReturnsHalf() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "disenchantment.discount.50", true);

        assertEquals(0.5, CostMultiplierUtils.getMultiplier(player));
    }

    @Test
    void givenMultipleDiscountPermissions_whenGetMultiplier_thenHighestWins() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "disenchantment.discount.10", true);
        player.addAttachment(plugin, "disenchantment.discount.50", true);
        player.addAttachment(plugin, "disenchantment.discount.25", true);

        assertEquals(0.5, CostMultiplierUtils.getMultiplier(player));
    }

    @Test
    void givenDiscount95Permission_whenGetMultiplier_thenReturnsFivePercent() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "disenchantment.discount.95", true);

        assertEquals(0.05, CostMultiplierUtils.getMultiplier(player), 0.0001);
    }

    @Test
    void givenLuckPermsEnabledButNotInstalled_whenGetMultiplier_thenFallsBackToPermissionNode() {
        setConfig("disenchantment.cost-multiplier.luckperms", true);
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "disenchantment.discount.20", true);

        // LuckPerms plugin is not registered in the test server, so LuckPermsUtils.isAvailable()
        // is false and resolution falls through to the permission-node path.
        assertEquals(0.8, CostMultiplierUtils.getMultiplier(player), 0.0001);
    }

    @Test
    void givenNullPlayer_whenGetMultiplier_thenReturnsOne() {
        assertEquals(1.0, CostMultiplierUtils.getMultiplier(null));
    }
}
