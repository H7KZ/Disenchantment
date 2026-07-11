package com.jankominek.disenchantment.types;

/**
 * Represents the number of enchantments split off per shatterment operation.
 * When {@code min == max} the split count is fixed; otherwise a random value
 * within {@code [min, max]} (inclusive) is chosen for each operation.
 *
 * @param min the minimum split count (inclusive)
 * @param max the maximum split count (inclusive)
 */
public record SplitCountRange(int min, int max) {
    /**
     * Returns {@code true} when this range represents a fixed (non-random) split count.
     *
     * @return {@code true} if {@code min == max}
     */
    public boolean isFixed() {
        return min == max;
    }
}
