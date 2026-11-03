package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.types.BedrockDetectMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.UUID;

import static com.jankominek.disenchantment.types.BedrockDetectMethod.FLOODGATE;
import static com.jankominek.disenchantment.types.BedrockDetectMethod.GEYSER;
import static com.jankominek.disenchantment.types.BedrockDetectMethod.PREFIX;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BedrockUtils}: every detection method, the configured order, and the guarantee that it
 * never throws. A test double of Floodgate's API lives on the test classpath; Geyser's does not, which covers
 * the "reflection target missing" path.
 */
class BedrockUtilsTest extends DisenchantmentTestBase {

    private static final UUID FLOODGATE_UUID = new UUID(0L, 0x1234L);
    private static final UUID JAVA_UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final List<String> PREFIXES = List.of(".", "*");

    @AfterEach
    void resetFloodgate() {
        org.geysermc.floodgate.api.FloodgateApi.reset();
    }

    // -> UUID

    @Test
    void givenFloodgateStyleUuid_whenViaUuid_thenDetected() {
        assertTrue(BedrockUtils.viaUuid(FLOODGATE_UUID));
        assertTrue(BedrockUtils.viaUuid(new UUID(0L, Long.MIN_VALUE)));
    }

    @Test
    void givenJavaUuid_whenViaUuid_thenNotDetected() {
        assertFalse(BedrockUtils.viaUuid(JAVA_UUID));
        assertFalse(BedrockUtils.viaUuid(new UUID(1L, 0L)));
        assertFalse(BedrockUtils.viaUuid(null));
    }

    // -> prefix

    @Test
    void givenConfiguredPrefix_whenViaPrefix_thenDetected() {
        assertTrue(BedrockUtils.viaPrefix(".Steve", PREFIXES));
        assertTrue(BedrockUtils.viaPrefix("*Alex", PREFIXES));
    }

    @Test
    void givenPlainNameOrPrefixInsideName_whenViaPrefix_thenNotDetected() {
        assertFalse(BedrockUtils.viaPrefix("Steve", PREFIXES));
        assertFalse(BedrockUtils.viaPrefix("St.eve", PREFIXES));
        assertFalse(BedrockUtils.viaPrefix("", PREFIXES));
        assertFalse(BedrockUtils.viaPrefix(null, PREFIXES));
    }

    @Test
    void givenBlankOrNullPrefixes_whenViaPrefix_thenNobodyIsDetected() {
        assertFalse(BedrockUtils.viaPrefix("Steve", List.of("")), "an empty prefix would match everyone");
        assertFalse(BedrockUtils.viaPrefix("Steve", java.util.Arrays.asList(null, "")));
        assertFalse(BedrockUtils.viaPrefix("Steve", List.of()));
        assertFalse(BedrockUtils.viaPrefix("Steve", null));
    }

    // -> reflection

    @Test
    void givenFloodgateKnowsThePlayer_whenViaFloodgate_thenDetected() {
        org.geysermc.floodgate.api.FloodgateApi.BEDROCK.add(JAVA_UUID);

        assertTrue(BedrockUtils.viaFloodgate(JAVA_UUID), "Floodgate's own answer wins, whatever the UUID looks like");
        assertFalse(BedrockUtils.viaFloodgate(UUID.randomUUID()));
    }

    @Test
    void givenFloodgateApiThrows_whenViaFloodgate_thenFalseAndNoException() {
        org.geysermc.floodgate.api.FloodgateApi.broken = true;

        assertDoesNotThrow(() -> assertFalse(BedrockUtils.viaFloodgate(JAVA_UUID)));
    }

    @Test
    void givenGeyserIsNotInstalled_whenViaGeyser_thenFalseAndNoException() {
        assertDoesNotThrow(() -> assertFalse(BedrockUtils.viaGeyser(FLOODGATE_UUID)));
    }

    @Test
    void givenNullUuid_whenAskingApis_thenFalse() {
        assertFalse(BedrockUtils.viaFloodgate(null));
        assertFalse(BedrockUtils.viaGeyser(null));
    }

    // -> order and combination

    @Test
    void givenSeveralMethods_whenAnyHits_thenDetected() {
        assertTrue(BedrockUtils.isBedrockPlayer(JAVA_UUID, ".Steve", List.of(FLOODGATE, GEYSER, BedrockDetectMethod.UUID, PREFIX), PREFIXES));
        assertTrue(BedrockUtils.isBedrockPlayer(FLOODGATE_UUID, "Steve", List.of(FLOODGATE, GEYSER, BedrockDetectMethod.UUID, PREFIX), PREFIXES));

        org.geysermc.floodgate.api.FloodgateApi.BEDROCK.add(JAVA_UUID);
        assertTrue(BedrockUtils.isBedrockPlayer(JAVA_UUID, "Steve", List.of(FLOODGATE), PREFIXES));
    }

    @Test
    void givenOnlyMethodsThatMiss_whenDetecting_thenNotDetected() {
        assertFalse(BedrockUtils.isBedrockPlayer(JAVA_UUID, "Steve", List.of(FLOODGATE, GEYSER, BedrockDetectMethod.UUID, PREFIX), PREFIXES));
        // The name would match the prefix, but PREFIX is not in the configured list.
        assertFalse(BedrockUtils.isBedrockPlayer(JAVA_UUID, ".Steve", List.of(BedrockDetectMethod.UUID), PREFIXES));
    }

    @Test
    void givenEmptyOrNullMethodList_whenDetecting_thenNobodyIsBedrock() {
        assertFalse(BedrockUtils.isBedrockPlayer(FLOODGATE_UUID, ".Steve", List.of(), PREFIXES));
        assertFalse(BedrockUtils.isBedrockPlayer(FLOODGATE_UUID, ".Steve", null, PREFIXES));
        assertFalse(BedrockUtils.isBedrockPlayer(FLOODGATE_UUID, ".Steve", java.util.Arrays.asList((BedrockDetectMethod) null), PREFIXES));
    }

    // -> through the config

    @Test
    void givenDefaultConfig_whenPlayerNameHasPrefix_thenDetected() {
        PlayerMock bedrock = server.addPlayer(".Steve");
        PlayerMock java = server.addPlayer("Alex");

        assertTrue(BedrockUtils.isBedrockPlayer(bedrock));
        assertFalse(BedrockUtils.isBedrockPlayer(java));
    }

    @Test
    void givenDetectRestrictedToUuid_whenPlayerOnlyHasPrefix_thenNotDetected() {
        setConfig("bedrock.detect", List.of("UUID"));
        PlayerMock prefixed = server.addPlayer(".Steve");

        assertFalse(BedrockUtils.isBedrockPlayer(prefixed));
    }

    @Test
    void givenCustomPrefix_whenPlayerNameMatches_thenDetected() {
        setConfig("bedrock.prefixes", List.of("BE_"));

        assertTrue(BedrockUtils.isBedrockPlayer(server.addPlayer("BE_Steve")));
        assertFalse(BedrockUtils.isBedrockPlayer(server.addPlayer(".Alex")));
    }

    @Test
    void givenNullPlayer_whenDetecting_thenFalse() {
        assertFalse(BedrockUtils.isBedrockPlayer(null));
    }
}
