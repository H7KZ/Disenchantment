package com.jankominek.disenchantment.events;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link GrindstoneOps}: what an extraction or a transfer moves.
 */
class GrindstoneOpsTest extends DisenchantmentTestBase {

    private ItemStack item(Material material, Object... enchantAndLevel) {
        ItemStack item = new ItemStack(material);
        for (int i = 0; i < enchantAndLevel.length; i += 2) {
            item.addUnsafeEnchantment(enchantment((String) enchantAndLevel[i]), (Integer) enchantAndLevel[i + 1]);
        }
        return item;
    }

    private static List<String> keys(List<IPluginEnchantment> list) {
        return list.stream().map(e -> e.getKey() + ":" + e.getLevel()).sorted().toList();
    }

    private List<IPluginEnchantment> extract(ItemStack source, boolean withDelete) {
        PlayerMock player = server.addPlayer();
        return GrindstoneOps.collectExtract(source, new ItemStack(Material.BOOK), withDelete, world(), player);
    }

    private List<IPluginEnchantment> transfer(ItemStack source, ItemStack target) {
        return GrindstoneOps.computeTransfer(source, target, server.addPlayer());
    }

    // -> extraction: reuses the disenchant pipeline

    @Test
    void givenEnchantedSwordAndBlankBook_whenCollectExtract_thenEnchantmentsCollected() {
        assertEquals(List.of("sharpness:5", "unbreaking:3"),
                keys(extract(item(Material.DIAMOND_SWORD, "sharpness", 5, "unbreaking", 3), true)));
    }

    @Test
    void givenEnchantedBookOrStackOrNamedBook_whenCollectExtract_thenRejected() {
        PlayerMock player = server.addPlayer();
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("sharpness"), 5);
        assertTrue(GrindstoneOps.collectExtract(book, new ItemStack(Material.BOOK), true, world(), player).isEmpty(),
                "Enchanted books are shattered, not disenchanted");

        ItemStack stack = item(Material.DIAMOND_SWORD, "sharpness", 5);
        stack.setAmount(2);
        assertTrue(GrindstoneOps.collectExtract(stack, new ItemStack(Material.BOOK), true, world(), player).isEmpty());

        ItemStack named = new ItemStack(Material.BOOK);
        ItemMeta meta = named.getItemMeta();
        meta.setDisplayName("Quest book");
        named.setItemMeta(meta);
        assertTrue(GrindstoneOps.collectExtract(item(Material.DIAMOND_SWORD, "sharpness", 5), named, true, world(), player).isEmpty(),
                "A tagged book must not be consumed");
    }

    @Test
    void givenCurse_whenCollectExtract_thenCurseStaysUnlessConfigured() {
        ItemStack sword = item(Material.DIAMOND_SWORD, "sharpness", 5, "vanishing_curse", 1);

        assertEquals(List.of("sharpness:5"), keys(extract(sword, true)));

        setConfig("grindstone.curses", true);
        assertEquals(List.of("sharpness:5", "vanishing_curse:1"), keys(extract(sword, true)));
    }

    @Test
    void givenOnlyCurses_whenCollectExtract_thenNothingToExtract() {
        assertTrue(extract(item(Material.DIAMOND_SWORD, "binding_curse", 1), true).isEmpty());
    }

    @Test
    void givenEnchantmentStates_whenCollectExtract_thenKeepDisableDeleteRespected() {
        ItemStack sword = item(Material.DIAMOND_SWORD, "sharpness", 5, "mending", 1);

        setDisenchantEnchantmentStates(List.of("mending:KEEP"));
        assertEquals(List.of("sharpness:5"), keys(extract(sword, true)));

        setDisenchantEnchantmentStates(List.of("mending:DISABLE"));
        assertTrue(extract(sword, true).isEmpty(), "A DISABLE enchantment blocks the whole operation");

        setDisenchantEnchantmentStates(List.of("mending:DELETE"));
        assertEquals(List.of("sharpness:5"), keys(extract(sword, true)), "Prepare: DELETE enchantments are not on the book");
        assertEquals(List.of("mending:1", "sharpness:5"), keys(extract(sword, false)), "Click: DELETE enchantments are kept in the list to be destroyed");
    }

    @Test
    void givenMaterialRestricted_whenCollectExtract_thenRejectedUnlessBypass() {
        setConfig("disenchantment.materials.list", List.of("DIAMOND_SWORD"));
        ItemStack sword = item(Material.DIAMOND_SWORD, "sharpness", 5);

        PlayerMock player = server.addPlayer();
        assertTrue(GrindstoneOps.collectExtract(sword, new ItemStack(Material.BOOK), true, world(), player).isEmpty());

        player.addAttachment(plugin, "disenchantment.bypass.material", true);
        assertEquals(1, GrindstoneOps.collectExtract(sword, new ItemStack(Material.BOOK), true, world(), player).size());
    }

    @Test
    void givenPluginAdapter_whenCollectExtract_thenAdapterEnchantmentsIncluded() {
        activateMockPlugin(new MockPluginAdapter("TestPlugin", mockEnchant("sharpness", 4)));

        assertEquals(List.of("sharpness:4"), keys(extract(new ItemStack(Material.DIAMOND_SWORD), true)));
    }

    @Test
    void givenCollectedEnchantments_whenBuildExtractBook_thenBookCarriesThem() {
        ItemStack book = GrindstoneOps.buildExtractBook(List.of(mockEnchant("sharpness", 5), mockEnchant("mending", 1)));

        assertEquals(Material.ENCHANTED_BOOK, book.getType());
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        assertEquals(5, meta.getStoredEnchantLevel(enchantment("sharpness")));
        assertEquals(1, meta.getStoredEnchantLevel(enchantment("mending")));
    }

    // -> transfer

    @Test
    void givenCompatibleEnchantments_whenComputeTransfer_thenOnlyCompatibleMove() {
        // sharpness cannot go on a pickaxe, efficiency and unbreaking can
        List<IPluginEnchantment> moved = transfer(
                item(Material.DIAMOND_SWORD, "sharpness", 5, "unbreaking", 3),
                new ItemStack(Material.IRON_PICKAXE));

        assertEquals(List.of("unbreaking:3"), keys(moved));
    }

    @Test
    void givenSameMaterial_whenComputeTransfer_thenNothing() {
        assertTrue(transfer(item(Material.DIAMOND_PICKAXE, "efficiency", 5), new ItemStack(Material.DIAMOND_PICKAXE)).isEmpty(),
                "Two items of the same kind are a vanilla repair");
    }

    @Test
    void givenBooksOrStacksOrNull_whenComputeTransfer_thenNothing() {
        ItemStack source = item(Material.DIAMOND_PICKAXE, "efficiency", 5);

        assertTrue(transfer(source, new ItemStack(Material.BOOK)).isEmpty());
        assertTrue(transfer(source, new ItemStack(Material.ENCHANTED_BOOK)).isEmpty());
        assertTrue(transfer(new ItemStack(Material.ENCHANTED_BOOK), new ItemStack(Material.IRON_PICKAXE)).isEmpty());
        assertTrue(transfer(source, null).isEmpty());
        assertTrue(transfer(null, new ItemStack(Material.IRON_PICKAXE)).isEmpty());

        ItemStack stackedTarget = new ItemStack(Material.IRON_PICKAXE);
        stackedTarget.setAmount(2);
        assertTrue(transfer(source, stackedTarget).isEmpty());
        assertTrue(GrindstoneOps.computeTransfer(source, source, server.addPlayer()).isEmpty(), "Same reference");
    }

    @Test
    void givenConflictWithTargetEnchantment_whenComputeTransfer_thenConflictingSkipped() {
        // fortune conflicts with silk touch which the shovel already carries
        List<IPluginEnchantment> moved = transfer(
                item(Material.DIAMOND_PICKAXE, "fortune", 3, "efficiency", 5),
                item(Material.IRON_SHOVEL, "silk_touch", 1));

        assertEquals(List.of("efficiency:5"), keys(moved));
    }

    @Test
    void givenConflictAmongSourceEnchantments_whenComputeTransfer_thenOnlyOneOfThemMoves() {
        // Unsafe item carrying both; the higher level wins, the other conflicts with the chosen one
        List<IPluginEnchantment> moved = transfer(
                item(Material.DIAMOND_PICKAXE, "fortune", 3, "silk_touch", 1),
                new ItemStack(Material.IRON_SHOVEL));

        assertEquals(List.of("fortune:3"), keys(moved));
    }

    @Test
    void givenOverMaxLevel_whenComputeTransfer_thenClampedUnlessAllowed() {
        ItemStack source = item(Material.DIAMOND_PICKAXE, "unbreaking", 5);
        ItemStack target = new ItemStack(Material.IRON_SHOVEL);

        assertEquals(List.of("unbreaking:3"), keys(transfer(source, target)));

        setConfig("grindstone.transfer.allow-over-max", true);
        assertEquals(List.of("unbreaking:5"), keys(transfer(source, target)));
    }

    @Test
    void givenTargetAlreadyHasEnchantment_whenComputeTransfer_thenOnlyUpgradesMove() {
        ItemStack source = item(Material.DIAMOND_PICKAXE, "unbreaking", 2, "efficiency", 5);

        assertEquals(List.of("efficiency:5"), keys(transfer(source, item(Material.IRON_SHOVEL, "unbreaking", 3))),
                "Equal or higher level on the target: skipped");
        assertEquals(List.of("efficiency:5", "unbreaking:2"), keys(transfer(source, item(Material.IRON_SHOVEL, "unbreaking", 1))),
                "Lower level on the target: upgraded");
    }

    @Test
    void givenStatesAndCurses_whenComputeTransfer_thenRespected() {
        ItemStack source = item(Material.DIAMOND_PICKAXE, "unbreaking", 3, "mending", 1, "vanishing_curse", 1);
        ItemStack target = new ItemStack(Material.IRON_SHOVEL);

        assertEquals(List.of("mending:1", "unbreaking:3"), keys(transfer(source, target)), "Curses stay by default");

        setConfig("grindstone.curses", true);
        assertEquals(List.of("mending:1", "unbreaking:3", "vanishing_curse:1"), keys(transfer(source, target)));

        setDisenchantEnchantmentStates(List.of("mending:KEEP"));
        assertEquals(List.of("unbreaking:3", "vanishing_curse:1"), keys(transfer(source, target)));

        setDisenchantEnchantmentStates(List.of("mending:DISABLE"));
        assertTrue(transfer(source, target).isEmpty());
    }

    @Test
    void givenMaterialRestricted_whenComputeTransfer_thenSourceMaterialChecked() {
        setConfig("disenchantment.materials.list", List.of("DIAMOND_PICKAXE"));

        assertTrue(transfer(item(Material.DIAMOND_PICKAXE, "unbreaking", 3), new ItemStack(Material.IRON_SHOVEL)).isEmpty());
    }

    @Test
    void givenMoreEnchantmentsThanCap_whenComputeTransfer_thenHighestLevelsKept() {
        setConfig("grindstone.transfer.max-enchantments", 2);

        List<IPluginEnchantment> moved = transfer(
                item(Material.DIAMOND_PICKAXE, "efficiency", 5, "unbreaking", 3, "mending", 1),
                new ItemStack(Material.IRON_SHOVEL));

        assertEquals(List.of("efficiency:5", "unbreaking:3"), keys(moved));
    }

    @Test
    void givenTransfers_whenBuildResultAndStrip_thenMovedNotCopied() {
        ItemStack source = item(Material.DIAMOND_PICKAXE, "efficiency", 5, "mending", 1);
        ItemStack target = new ItemStack(Material.IRON_SHOVEL);
        List<IPluginEnchantment> moved = transfer(source, target);

        ItemStack result = GrindstoneOps.buildTransferResult(target, moved);
        ItemStack stripped = GrindstoneOps.stripFromSource(source, moved);

        Enchantment efficiency = enchantment("efficiency");
        assertEquals(5, result.getEnchantmentLevel(efficiency));
        assertTrue(result.containsEnchantment(enchantment("mending")));
        assertTrue(stripped.getEnchantments().isEmpty());
        assertTrue(target.getEnchantments().isEmpty(), "Inputs are never mutated");
        assertEquals(5, source.getEnchantmentLevel(efficiency));
    }
}
