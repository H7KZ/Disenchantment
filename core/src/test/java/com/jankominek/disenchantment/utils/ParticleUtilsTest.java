package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.types.ParticleScopeType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests particle-name resolution (reflection on public fields only, data-less particles only) and spawning.
 */
class ParticleUtilsTest extends DisenchantmentTestBase {

    @BeforeEach
    @AfterEach
    void clear() {
        ParticleUtils.clearCache();
    }

    // -> resolve

    @Test
    void givenKnownName_whenResolve_thenParticleReturned() {
        assertEquals(List.of(Particle.HEART), ParticleUtils.resolve(List.of("HEART")));
    }

    @Test
    void givenLowerCaseName_whenResolve_thenResolvedCaseInsensitively() {
        assertEquals(List.of(Particle.HEART), ParticleUtils.resolve(List.of("heart")));
    }

    @Test
    void givenUnknownNames_whenResolve_thenEmpty() {
        assertTrue(ParticleUtils.resolve(List.of("NOT_A_PARTICLE", "", "  ")).isEmpty());
    }

    @Test
    void givenNullOrEmptyList_whenResolve_thenEmpty() {
        assertTrue(ParticleUtils.resolve(null).isEmpty());
        assertTrue(ParticleUtils.resolve(List.of()).isEmpty());
    }

    @Test
    void givenParticleNeedingData_whenResolve_thenSkipped() {
        // DUST needs a Particle.DustOptions, ITEM an ItemStack: a bare config name cannot supply them.
        assertTrue(ParticleUtils.resolve(List.of("DUST", "ITEM")).isEmpty());
    }

    @Test
    void givenUnknownThenKnownName_whenResolve_thenFallbackUsed() {
        assertEquals(List.of(Particle.FLAME), ParticleUtils.resolve(List.of("NOT_A_PARTICLE", "FLAME")));
    }

    @Test
    void givenDuplicateNames_whenResolve_thenCollapsed() {
        assertEquals(List.of(Particle.HEART), ParticleUtils.resolve(List.of("HEART", "heart")));
    }

    @Test
    void givenNonFieldName_whenResolve_thenSkipped() {
        // valueOf/values are static methods, not particle constants.
        assertTrue(ParticleUtils.resolve(List.of("valueOf", "values", "class")).isEmpty());
    }

    @Test
    void givenSameListTwice_whenResolve_thenCachedInstanceReturned() {
        List<Particle> first = ParticleUtils.resolve(List.of("HEART"));
        assertSame(first, ParticleUtils.resolve(List.of("HEART")));
    }

    // -> spawn

    @Test
    void givenPlayerScope_whenSpawn_thenOnlyPlayerReceivesParticles() {
        Player player = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        Location location = new Location(world, 1, 2, 3);

        ParticleUtils.spawn(player, location, List.of(Particle.HEART), 7, 0.5, ParticleScopeType.PLAYER);

        Mockito.verify(player).spawnParticle(Particle.HEART, location, 7, 0.5, 0.5, 0.5, 0.0);
        Mockito.verifyNoInteractions(world);
    }

    @Test
    void givenWorldScope_whenSpawn_thenWorldReceivesParticles() {
        Player player = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        Location location = new Location(world, 1, 2, 3);

        ParticleUtils.spawn(player, location, List.of(Particle.HEART), 7, 0.5, ParticleScopeType.WORLD);

        Mockito.verify(world).spawnParticle(Particle.HEART, location, 7, 0.5, 0.5, 0.5, 0.0);
        Mockito.verify(player, Mockito.never()).spawnParticle(Mockito.any(Particle.class), Mockito.any(Location.class),
                Mockito.anyInt(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble());
    }

    // -> location

    @Test
    void givenAnvilWithLocation_whenEffectLocation_thenCentreTopOfBlock() {
        Player player = Mockito.mock(Player.class);
        Inventory inventory = Mockito.mock(Inventory.class);
        World world = Mockito.mock(World.class);
        Mockito.when(inventory.getLocation()).thenReturn(new Location(world, 10, 64, -5));

        Location location = ParticleUtils.anvilEffectLocation(player, inventory);

        assertEquals(10.5, location.getX());
        assertEquals(65.0, location.getY());
        assertEquals(-4.5, location.getZ());
    }

    @Test
    void givenVirtualAnvil_whenEffectLocation_thenAbovePlayer() {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getLocation()).thenReturn(new Location(null, 1, 70, 2));
        Inventory inventory = Mockito.mock(Inventory.class);

        Location location = ParticleUtils.anvilEffectLocation(player, inventory);

        assertEquals(1.0, location.getX());
        assertEquals(71.0, location.getY());
        assertEquals(2.0, location.getZ());
    }

    // -> config-driven

    @Test
    void givenParticlesDisabled_whenPlaySuccess_thenNothingSpawned() {
        setConfig("feedback.particles.enabled", false);
        Player player = Mockito.mock(Player.class);

        ParticleUtils.playSuccess(player, Mockito.mock(Inventory.class));

        Mockito.verifyNoInteractions(player);
    }

    @Test
    void givenCustomTypes_whenPlaySuccess_thenConfiguredParticleSpawnedWithConfiguredCount() {
        setConfig("feedback.particles.types", List.of("HEART"));
        setConfig("feedback.particles.count", 5);
        setConfig("feedback.particles.spread", 1.0);
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getLocation()).thenReturn(new Location(null, 0, 0, 0));

        ParticleUtils.playSuccess(player, Mockito.mock(Inventory.class));

        Mockito.verify(player).spawnParticle(Mockito.eq(Particle.HEART), Mockito.any(Location.class),
                Mockito.eq(5), Mockito.eq(1.0), Mockito.eq(1.0), Mockito.eq(1.0), Mockito.eq(0.0));
    }

    @Test
    void givenDefaults_whenPlaySuccess_thenEnchantParticleSpawned() {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getLocation()).thenReturn(new Location(null, 0, 0, 0));

        ParticleUtils.playSuccess(player, Mockito.mock(Inventory.class));

        // Exactly one of ENCHANT / ENCHANTMENT_TABLE exists on any server version.
        Mockito.verify(player, Mockito.atLeastOnce()).spawnParticle(Mockito.any(Particle.class), Mockito.any(Location.class),
                Mockito.eq(30), Mockito.eq(0.4), Mockito.eq(0.4), Mockito.eq(0.4), Mockito.eq(0.0));
    }

    @Test
    void givenScopeValues_whenParse_thenFallBackToPlayer() {
        assertEquals(ParticleScopeType.WORLD, ParticleScopeType.fromConfig(" world "));
        assertEquals(ParticleScopeType.PLAYER, ParticleScopeType.fromConfig("nonsense"));
        assertEquals(ParticleScopeType.PLAYER, ParticleScopeType.fromConfig(null));
    }
}
