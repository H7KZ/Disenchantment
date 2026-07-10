package com.jankominek.disenchantment.stats;

import java.util.UUID;

public record OperationRecord(
    long timestamp,
    UUID playerUuid,
    String playerName,
    String world,
    OperationType operationType,
    String itemMaterial,
    String enchantmentKeys,
    int xpCost,
    double economyCost
) {}
