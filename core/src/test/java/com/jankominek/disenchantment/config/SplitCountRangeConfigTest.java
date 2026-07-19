package com.jankominek.disenchantment.config;

import com.jankominek.disenchantment.Disenchantment;
import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.types.SplitCountRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitCountRangeConfigTest extends DisenchantmentTestBase {

    @Test
    void givenLegacyIntegerSplitCount_whenGetSplitCount_thenParsedAsFixedRange() {
        setConfig("shatterment.split-count", 3);

        SplitCountRange range = Config.Shatterment.getSplitCount();

        assertEquals(3, range.min());
        assertEquals(3, range.max());
        assertTrue(range.isFixed());
    }

    @Test
    void givenMapShapedSplitCount_whenGetSplitCount_thenParsedAsDistinctRange() {
        setConfig("shatterment.split-count.min", 2);
        setConfig("shatterment.split-count.max", 5);

        SplitCountRange range = Config.Shatterment.getSplitCount();

        assertEquals(2, range.min());
        assertEquals(5, range.max());
        assertFalse(range.isFixed());
    }

    @Test
    void givenFixedRange_whenSetSplitCount_thenPersistsAsPlainInteger() {
        Config.Shatterment.setSplitCount(new SplitCountRange(4, 4));

        assertEquals(4, Disenchantment.config.getInt("shatterment.split-count"));
        assertEquals(new SplitCountRange(4, 4), Config.Shatterment.getSplitCount());
    }

    @Test
    void givenDistinctRange_whenSetSplitCount_thenPersistsAsMap() {
        Config.Shatterment.setSplitCount(new SplitCountRange(1, 6));

        assertEquals(new SplitCountRange(1, 6), Config.Shatterment.getSplitCount());
    }
}
