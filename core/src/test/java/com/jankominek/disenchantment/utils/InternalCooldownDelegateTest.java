package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalCooldownDelegateTest extends DisenchantmentTestBase {

    @Test
    void givenNoPriorOperation_whenIsOnCooldown_thenFalse() {
        setConfig("disenchantment.cooldown.seconds", 30);
        InternalCooldownDelegate delegate = new InternalCooldownDelegate();
        PlayerMock player = server.addPlayer();

        assertFalse(delegate.isOnCooldown(player));
        assertEquals(0, delegate.getRemainingSeconds(player));
    }

    @Test
    void givenJustRecordedOperation_whenIsOnCooldown_thenTrue() {
        setConfig("disenchantment.cooldown.seconds", 30);
        InternalCooldownDelegate delegate = new InternalCooldownDelegate();
        PlayerMock player = server.addPlayer();

        delegate.recordOperation(player);

        assertTrue(delegate.isOnCooldown(player));
        assertTrue(delegate.getRemainingSeconds(player) > 0);
        assertTrue(delegate.getRemainingSeconds(player) <= 30);
    }

    @Test
    void givenElapsedCooldownWindow_whenIsOnCooldown_thenFalse() throws Exception {
        setConfig("disenchantment.cooldown.seconds", 30);
        InternalCooldownDelegate delegate = new InternalCooldownDelegate();
        PlayerMock player = server.addPlayer();

        delegate.recordOperation(player);

        // Simulate the cooldown window having fully elapsed by rewriting the stored
        // timestamp directly, rather than sleeping the test for 30+ seconds.
        java.lang.reflect.Field field = InternalCooldownDelegate.class.getDeclaredField("lastOperationAt");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<java.util.UUID, Long> map =
                (java.util.concurrent.ConcurrentHashMap<java.util.UUID, Long>) field.get(delegate);
        map.put(player.getUniqueId(), System.currentTimeMillis() - 31_000L);

        assertFalse(delegate.isOnCooldown(player));
        assertEquals(0, delegate.getRemainingSeconds(player));
    }

    @Test
    void givenDifferentPlayers_whenOneRecordsOperation_thenOtherIsUnaffected() {
        setConfig("disenchantment.cooldown.seconds", 30);
        InternalCooldownDelegate delegate = new InternalCooldownDelegate();
        PlayerMock playerA = server.addPlayer();
        PlayerMock playerB = server.addPlayer();

        delegate.recordOperation(playerA);

        assertTrue(delegate.isOnCooldown(playerA));
        assertFalse(delegate.isOnCooldown(playerB));
    }

    @Test
    void givenClear_whenIsOnCooldown_thenFalse() {
        setConfig("disenchantment.cooldown.seconds", 30);
        InternalCooldownDelegate delegate = new InternalCooldownDelegate();
        PlayerMock player = server.addPlayer();

        delegate.recordOperation(player);
        assertTrue(delegate.isOnCooldown(player));

        delegate.clear();

        assertFalse(delegate.isOnCooldown(player));
    }

    // ----------------------------------------------------------------------------------------------------
    // CooldownManager orchestration

    @Test
    void givenZeroCooldownSeconds_whenIsOnCooldown_thenAlwaysFalse() {
        setConfig("disenchantment.cooldown.seconds", 0);
        PlayerMock player = server.addPlayer();

        CooldownManager.recordOperation(player);

        assertFalse(CooldownManager.isOnCooldown(player));
    }

    @Test
    void givenBypassPermission_whenIsOnCooldown_thenFalse() {
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, "disenchantment.cooldown.bypass", true);

        CooldownManager.recordOperation(player);

        assertFalse(CooldownManager.isOnCooldown(player));

        CooldownManager.reset();
    }

    @Test
    void givenInternalDelegate_whenRecordThenCheck_thenOnCooldown() {
        setConfig("disenchantment.cooldown.seconds", 30);
        PlayerMock player = server.addPlayer();

        CooldownManager.recordOperation(player);

        assertTrue(CooldownManager.isOnCooldown(player));
        assertTrue(CooldownManager.getRemainingSeconds(player) > 0);

        CooldownManager.reset();
    }
}
