package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the per-player refusal-message debounce and the failure sound.
 */
class FeedbackUtilsTest extends DisenchantmentTestBase {

    private final AtomicLong now = new AtomicLong(1_000_000L);

    @BeforeEach
    void installClock() {
        FeedbackUtils.reset();
        FeedbackUtils.setClock(now::get);
    }

    @AfterEach
    void restoreClock() {
        FeedbackUtils.setClock(null);
        FeedbackUtils.reset();
    }

    // -> debounce

    @Test
    void givenTwoRefusalsWithinOneSecond_whenSend_thenOnlyFirstSent() {
        PlayerMock player = server.addPlayer("Debounced");

        assertTrue(FeedbackUtils.sendRefusalMessage(player, "first"));
        now.addAndGet(999);
        assertFalse(FeedbackUtils.sendRefusalMessage(player, "second"));

        assertNotNull(player.nextMessage());
        assertNull(player.nextMessage(), "The second message within the window must be dropped");
    }

    @Test
    void givenRefusalAfterOneSecond_whenSend_thenSentAgain() {
        PlayerMock player = server.addPlayer("Debounced");

        assertTrue(FeedbackUtils.sendRefusalMessage(player, "first"));
        now.addAndGet(1000);
        assertTrue(FeedbackUtils.sendRefusalMessage(player, "second"));
    }

    @Test
    void givenTwoPlayers_whenSend_thenDebouncedIndependently() {
        PlayerMock a = server.addPlayer("A");
        PlayerMock b = server.addPlayer("B");

        assertTrue(FeedbackUtils.sendRefusalMessage(a, "x"));
        assertTrue(FeedbackUtils.sendRefusalMessage(b, "x"));
    }

    @Test
    void givenClockGoingBackwards_whenSend_thenWindowDoesNotStickForever() {
        PlayerMock player = server.addPlayer("Debounced");

        assertTrue(FeedbackUtils.sendRefusalMessage(player, "first"));
        now.addAndGet(-5_000);
        assertTrue(FeedbackUtils.sendRefusalMessage(player, "second"));
    }

    @Test
    void givenForgottenPlayer_whenSend_thenSentImmediately() {
        PlayerMock player = server.addPlayer("Debounced");

        assertTrue(FeedbackUtils.sendRefusalMessage(player, "first"));
        FeedbackUtils.forget(player);
        assertTrue(FeedbackUtils.sendRefusalMessage(player, "second"));
    }

    // -> not-enough-xp gate

    @Test
    void givenNotEnoughXpMessageDisabled_whenRefuse_thenNoMessageButSoundPlays() {
        setConfig("feedback.not-enough-xp-message", false);
        PlayerMock player = server.addPlayer("Broke");

        FeedbackUtils.refuseNotEnoughXp(player, 12);

        assertNull(player.nextMessage());
        assertEquals(1, player.getHeardSounds().size());
    }

    @Test
    void givenNotEnoughXpMessageEnabled_whenRefuseTwiceQuickly_thenOneMessageTwoSounds() {
        PlayerMock player = server.addPlayer("Broke");

        FeedbackUtils.refuseNotEnoughXp(player, 12);
        FeedbackUtils.refuseNotEnoughXp(player, 12);

        assertNotNull(player.nextMessage());
        assertNull(player.nextMessage());
        assertEquals(2, player.getHeardSounds().size(), "The sound is not debounced");
    }

    // -> failure sound

    @Test
    void givenDefaults_whenPlayFailureSound_thenConfiguredKeyPlayedAsString() {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(player.getLocation()).thenReturn(new Location(null, 1, 2, 3));

        FeedbackUtils.playFailureSound(player);

        Mockito.verify(player).playSound(Mockito.any(Location.class), Mockito.eq("entity.villager.no"), Mockito.eq(1.0f), Mockito.eq(1.0f));
    }

    @Test
    void givenCustomSound_whenPlayFailureSound_thenUsed() {
        setConfig("feedback.failure-sound.sound", "block.note_block.bass");
        setConfig("feedback.failure-sound.volume", 0.5);
        setConfig("feedback.failure-sound.pitch", 2.0);
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getLocation()).thenReturn(new Location(null, 0, 0, 0));

        FeedbackUtils.playFailureSound(player);

        Mockito.verify(player).playSound(Mockito.any(Location.class), Mockito.eq("block.note_block.bass"), Mockito.eq(0.5f), Mockito.eq(2.0f));
    }

    @Test
    void givenSoundDisabled_whenPlayFailureSound_thenSilent() {
        setConfig("feedback.failure-sound.enabled", false);
        Player player = Mockito.mock(Player.class);

        FeedbackUtils.playFailureSound(player);

        Mockito.verifyNoInteractions(player);
    }

    @Test
    void givenBlankSoundKey_whenPlayFailureSound_thenDefaultKeyUsed() {
        setConfig("feedback.failure-sound.sound", "  ");
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getLocation()).thenReturn(new Location(null, 0, 0, 0));

        FeedbackUtils.playFailureSound(player);

        Mockito.verify(player).playSound(Mockito.any(Location.class), Mockito.eq("entity.villager.no"), Mockito.anyFloat(), Mockito.anyFloat());
    }
}
