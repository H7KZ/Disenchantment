package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.MockPluginAdapter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests of erase mode: catalyst parsing and matching, state handling, curses,
 * restrictions and result building.
 */
class EraseUtilsTest extends DisenchantmentTestBase {

    @BeforeEach
    @AfterEach
    void clear() {
        EraseUtils.clearCache();
    }

    private ItemStack enchantedSword(Object... keyLevelPairs) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        for (int i = 0; i < keyLevelPairs.length; i += 2) {
            sword.addUnsafeEnchantment(enchantment((String) keyLevelPairs[i]), (Integer) keyLevelPairs[i + 1]);
        }
        return sword;
    }

    private static ItemStack shard() {
        return new ItemStack(Material.ECHO_SHARD);
    }

    private static List<String> keys(List<IPluginEnchantment> enchantments) {
        return enchantments.stream().map(IPluginEnchantment::getKey).sorted().toList();
    }

    // -> catalyst parsing

    @Test
    void givenMixedCatalystList_whenParse_thenUnknownAndForbiddenSkipped() {
        Set<Material> parsed = EraseUtils.parseCatalysts(
                List.of("ECHO_SHARD", "nonsense_item", "BOOK", "ENCHANTED_BOOK", "AIR", "minecraft:amethyst_shard", "echo_shard"));

        assertEquals(Set.of(Material.ECHO_SHARD, Material.AMETHYST_SHARD), parsed);
    }

    @Test
    void givenNullEmptyOrBlankEntries_whenParse_thenNothingAccepted() {
        assertTrue(EraseUtils.parseCatalysts(null).isEmpty());
        assertTrue(EraseUtils.parseCatalysts(List.of()).isEmpty());
        assertTrue(EraseUtils.parseCatalysts(java.util.Arrays.asList("", "  ", null)).isEmpty());
    }

    @Test
    void givenDefaultConfig_whenGetCatalysts_thenEchoShard() {
        assertEquals(Set.of(Material.ECHO_SHARD), EraseUtils.getCatalysts());
    }

    @Test
    void givenOnlyInvalidCatalysts_whenEnabled_thenEraseIsNotActive() {
        setConfig("erase.enabled", true);
        setConfig("erase.catalysts", List.of("BOOK", "not_a_material"));

        assertTrue(EraseUtils.getCatalysts().isEmpty());
        assertFalse(EraseUtils.isActive());
    }

    @Test
    void givenEmptyCatalystList_whenEnabled_thenEraseIsNotActive() {
        setConfig("erase.enabled", true);
        setConfig("erase.catalysts", List.of());

        assertFalse(EraseUtils.isActive());
    }

    @Test
    void givenDisabled_whenIsActive_thenFalseEvenWithValidCatalyst() {
        assertFalse(EraseUtils.isActive(), "Erase mode is off by default");

        setConfig("erase.enabled", true);
        assertTrue(EraseUtils.isActive());
    }

    // -> catalyst matching

    @Test
    void givenCatalystItem_whenIsCatalyst_thenMatchedByMaterialOnly() {
        ItemStack renamed = shard();
        ItemMeta meta = renamed.getItemMeta();
        meta.setDisplayName("My shard");
        renamed.setItemMeta(meta);

        assertTrue(EraseUtils.isCatalyst(shard()));
        assertTrue(EraseUtils.isCatalyst(renamed));
        assertFalse(EraseUtils.isCatalyst(new ItemStack(Material.DIAMOND)));
        assertFalse(EraseUtils.isCatalyst(new ItemStack(Material.BOOK)));
        assertFalse(EraseUtils.isCatalyst(null));
    }

    @Test
    void givenBookConfiguredAsCatalyst_whenGetErasable_thenStillRejected() {
        setConfig("erase.catalysts", List.of("BOOK"));

        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), new ItemStack(Material.BOOK)).isEmpty());
    }

    @Test
    void givenCustomCatalyst_whenGetErasable_thenOnlyThatMaterialWorks() {
        setConfig("erase.catalysts", List.of("AMETHYST_SHARD"));
        ItemStack sword = enchantedSword("sharpness", 5);

        assertTrue(EraseUtils.getErasableEnchantments(sword, shard()).isEmpty());
        assertEquals(List.of("sharpness"), keys(EraseUtils.getErasableEnchantments(sword, new ItemStack(Material.AMETHYST_SHARD))));
    }

    // -> basic selection

    @Test
    void givenEnchantedSwordAndCatalyst_whenGetErasable_thenAllEnchantmentsReturned() {
        List<IPluginEnchantment> result = EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5, "unbreaking", 3), shard());

        assertEquals(List.of("sharpness", "unbreaking"), keys(result));
    }

    @Test
    void givenNullOrAirInputs_whenGetErasable_thenEmpty() {
        assertTrue(EraseUtils.getErasableEnchantments(null, shard()).isEmpty());
        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), null).isEmpty());
        assertTrue(EraseUtils.getErasableEnchantments(new ItemStack(Material.AIR), shard()).isEmpty());
        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), new ItemStack(Material.AIR)).isEmpty());
    }

    @Test
    void givenUnenchantedItem_whenGetErasable_thenEmpty() {
        assertTrue(EraseUtils.getErasableEnchantments(new ItemStack(Material.DIAMOND_SWORD), shard()).isEmpty());
    }

    @Test
    void givenEnchantedBookAsSource_whenGetErasable_thenEmpty() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentUtils.addStoredEnchantment(book, enchantment("sharpness"), 5);

        assertTrue(EraseUtils.getErasableEnchantments(book, shard()).isEmpty());
    }

    @Test
    void givenWrongSecondItem_whenGetErasable_thenEmpty() {
        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), new ItemStack(Material.DIAMOND)).isEmpty());
    }

    @Test
    void givenStackedSource_whenGetErasable_thenEmpty() {
        ItemStack tools = new ItemStack(Material.DIAMOND_PICKAXE, 2);
        tools.addUnsafeEnchantment(enchantment("efficiency"), 5);

        assertTrue(EraseUtils.getErasableEnchantments(tools, shard()).isEmpty());
    }

    // -> enchantment states

    @Test
    void givenKeepState_whenGetErasable_thenThatEnchantmentStays() {
        setDisenchantEnchantmentStates(List.of("unbreaking:keep"));

        List<IPluginEnchantment> result = EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5, "unbreaking", 3), shard());

        assertEquals(List.of("sharpness"), keys(result));
    }

    @Test
    void givenOnlyKeptEnchantments_whenGetErasable_thenNothingToErase() {
        setDisenchantEnchantmentStates(List.of("sharpness:keep"));

        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), shard()).isEmpty());
    }

    @Test
    void givenDisableState_whenGetErasable_thenWholeOperationRefused() {
        setDisenchantEnchantmentStates(List.of("unbreaking:disable"));

        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5, "unbreaking", 3), shard()).isEmpty());
    }

    @Test
    void givenDeleteState_whenGetErasable_thenErasedLikeEverythingElse() {
        setDisenchantEnchantmentStates(List.of("unbreaking:delete"));

        List<IPluginEnchantment> result = EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5, "unbreaking", 3), shard());

        assertEquals(List.of("sharpness", "unbreaking"), keys(result));
    }

    @Test
    void givenDeleteState_whenPrepareAndClickFlavour_thenSameResult() {
        setDisenchantEnchantmentStates(List.of("unbreaking:delete"));
        ItemStack sword = enchantedSword("sharpness", 5, "unbreaking", 3);

        assertEquals(keys(EraseUtils.getErasableEnchantments(sword, shard(), true)), keys(EraseUtils.getErasableEnchantments(sword, shard(), false)));
    }

    @Test
    void givenEnchantmentChance_whenGetErasable_thenIgnored() {
        setConfig("disenchantment.enchantment-chances.sharpness", 0.0);

        assertEquals(List.of("sharpness"), keys(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), shard())));
    }

    // -> curses

    @Test
    void givenCurse_whenGetErasable_thenCurseStaysByDefault() {
        List<IPluginEnchantment> result = EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5, "vanishing_curse", 1), shard());

        assertEquals(List.of("sharpness"), keys(result));
    }

    @Test
    void givenOnlyCurses_whenGetErasable_thenNothingToErase() {
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addUnsafeEnchantment(enchantment("binding_curse"), 1);
        helmet.addUnsafeEnchantment(enchantment("vanishing_curse"), 1);

        assertTrue(EraseUtils.getErasableEnchantments(helmet, shard()).isEmpty());
    }

    @Test
    void givenEraseCursesOn_whenGetErasable_thenCursesIncluded() {
        setConfig("erase.erase-curses", true);

        List<IPluginEnchantment> result = EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5, "vanishing_curse", 1), shard());

        assertEquals(List.of("sharpness", "vanishing_curse"), keys(result));
    }

    @Test
    void givenCurseKeys_whenIsCurse_thenDetectedByKeyAndRegistry() {
        assertTrue(EraseUtils.isCurse(mockEnchant("binding_curse", 1)));
        assertTrue(EraseUtils.isCurse(mockEnchant("vanishing_curse", 1)));
        assertFalse(EraseUtils.isCurse(mockEnchant("sharpness", 5)));
        assertFalse(EraseUtils.isCurse(mockEnchant("mending", 1)));
    }

    @Test
    void givenAdapterEnchantmentEndingInCurse_whenIsCurse_thenDetected() {
        IPluginEnchantment custom = new IPluginEnchantment() {
            public String getKey() {
                return "custom:hunger_curse";
            }

            public int getLevel() {
                return 1;
            }
        };

        assertTrue(EraseUtils.isCurse(custom));
    }

    // -> restrictions

    @Test
    void givenMaterialInDenylist_whenGetErasable_thenEmpty() {
        setConfig("disenchantment.materials.mode", "DENYLIST");
        setConfig("disenchantment.materials.list", List.of("DIAMOND_SWORD"));

        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), shard()).isEmpty());
    }

    @Test
    void givenMaterialNotInAllowlist_whenGetErasable_thenEmpty() {
        setConfig("disenchantment.materials.mode", "ALLOWLIST");
        setConfig("disenchantment.materials.list", List.of("DIAMOND_PICKAXE"));

        assertTrue(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), shard()).isEmpty());
    }

    @Test
    void givenRestrictedMaterialAndBypass_whenGetErasable_thenAllowed() {
        setConfig("disenchantment.materials.mode", "DENYLIST");
        setConfig("disenchantment.materials.list", List.of("DIAMOND_SWORD"));

        EventUtils.setMaterialBypass(true);
        try {
            assertEquals(List.of("sharpness"), keys(EraseUtils.getErasableEnchantments(enchantedSword("sharpness", 5), shard())));
        } finally {
            EventUtils.setMaterialBypass(false);
        }
    }

    // -> adapters

    @Test
    void givenAdapter_whenGetErasable_thenAdapterEnchantmentsFiltered() {
        MockPluginAdapter adapter = new MockPluginAdapter("TestPlugin", mockEnchant("sharpness", 5), mockEnchant("binding_curse", 1));

        List<IPluginEnchantment> result = EraseUtils.getErasableEnchantments(
                new ItemStack(Material.DIAMOND_SWORD), shard(), false, adapter, world());

        assertEquals(List.of("sharpness"), keys(result));
    }

    @Test
    void givenAdapterAndWrongCatalyst_whenGetErasable_thenEmpty() {
        MockPluginAdapter adapter = new MockPluginAdapter("TestPlugin", mockEnchant("sharpness", 5));

        assertTrue(EraseUtils.getErasableEnchantments(
                new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND), false, adapter, world()).isEmpty());
    }

    // -> result

    @Test
    void givenSelection_whenBuildResult_thenErasedRemovedOthersAndMetaKept() {
        ItemStack sword = enchantedSword("sharpness", 5, "unbreaking", 3, "vanishing_curse", 1);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName("Excalibur");
        meta.setLore(List.of("legendary"));
        ((Damageable) meta).setDamage(42);
        sword.setItemMeta(meta);

        ItemStack result = EraseUtils.buildResult(sword, EraseUtils.getErasableEnchantments(sword, shard()));

        Enchantment vanishing = enchantment("vanishing_curse");
        assertFalse(result.containsEnchantment(enchantment("sharpness")));
        assertFalse(result.containsEnchantment(enchantment("unbreaking")));
        assertTrue(result.containsEnchantment(vanishing), "The curse was not selected, so it stays");
        assertEquals("Excalibur", result.getItemMeta().getDisplayName());
        assertEquals(List.of("legendary"), result.getItemMeta().getLore());
        assertEquals(42, ((Damageable) result.getItemMeta()).getDamage());
        assertTrue(sword.containsEnchantment(enchantment("sharpness")), "The source item must not be mutated");
    }

    // -> prepared-result marker

    @Test
    void givenBuiltResult_whenMarked_thenRecognisedAndSourceIsNot() {
        ItemStack source = enchantedSword("sharpness", 5);
        ItemStack result = EraseUtils.buildResult(source, EraseUtils.getErasableEnchantments(source, shard()));

        assertFalse(EraseUtils.isPreparedResult(result));
        assertSame(result, EraseUtils.markPrepared(result));

        assertTrue(EraseUtils.isPreparedResult(result));
        assertFalse(EraseUtils.isPreparedResult(source));
        assertFalse(EraseUtils.isPreparedResult(null));
        assertFalse(EraseUtils.isPreparedResult(new ItemStack(Material.AIR)));
    }

    @Test
    void givenMarkedResult_whenDeliveredItemRebuilt_thenItIsNotMarked() {
        // The delivered item is rebuilt from the live source (slot 0), which is never marked.
        ItemStack source = enchantedSword("sharpness", 5);
        EraseUtils.markPrepared(EraseUtils.buildResult(source, List.of()));

        assertFalse(EraseUtils.isPreparedResult(EraseUtils.buildResult(source, EraseUtils.getErasableEnchantments(source, shard()))));
    }
}
