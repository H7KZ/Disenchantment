# Issue #76 — Over-max (above vanilla-max) enchantment levels lost on disenchant

- **Issue:** [COMPATIBILITY] Past Max Vanilla Enchants (#76), labels `bug`, `priority: critical`
- **Reported on:** Paper 1.21.x ("26.2" is a typo), Disenchantment 6.5.0, no custom-enchant plugin
- **Repro:** `/enchant unbreaking 4` on an item → item + blank book in anvil → book comes out clamped to the vanilla max (Unbreaking 3) instead of 4.
- **Investigated against:** this repo's source (current `master`, v6.5.12; cross-checked at tag `v6.5.0`), an empirical MockBukkit probe run, and the Bukkit API Javadoc (Paper mirror).

---

## TL;DR / root-cause summary

The plugin's enchantment-**transfer** code does **not** clamp. Every read is raw and every write uses the
level-restriction-ignoring API (`ItemStack.addUnsafeEnchantment` / `EnchantmentStorageMeta.addStoredEnchant(ench, level, true)` /
`ItemMeta.addEnchant(ench, level, true)`). An empirical probe (below) proves a source item with **Unbreaking 4** produces a
result book with **Unbreaking 4**, and **Sharpness 10 → book Sharpness 10**. So the premise that the plugin's add-enchant call
clamps is **not** where the level is lost.

The one seam the plugin does **not** control with those calls is the **result-slot round-trip in the click handler**:
`DisenchantClickEvent` delivers the book by reading it back out of the anvil result slot
(`anvilInventory.getItem(2)`, `DisenchantClickEvent.java:87`) — the item that was handed to the server via
`PrepareAnvilEvent.setResult(...)` and stored in slot 2 — rather than rebuilding a fresh book from the raw-level
enchantment list it already re-collected two lines earlier. If a given Paper/CraftBukkit build normalizes or clamps the
ItemStack that sits in the anvil result slot (a server-side round-trip the plugin's `addUnsafe*` guarantees can't cover),
the clamped item is what gets delivered. The robust fix is to build and deliver the book in the click handler from the
freshly collected `IPluginEnchantment` list (raw levels), exactly as the prepare handler does, instead of trusting
`getItem(2)`.

> Honesty note: the transfer logic is provably correct and the delivery-path round-trip is the remaining plausible clamp
> point, but it could not be reproduced offline (MockBukkit does not run Paper's anvil/result-slot code). The report
> separates what is **proven** from what is a **hypothesis to confirm on a live server** — see "Confirmation steps".

---

## End-to-end trace (each step cited)

### 1. PrepareAnvil → build preview book (correct, preserves level)

`core/.../events/DisenchantListener.java:47` registers `PrepareAnvilEvent` and delegates to
`DisenchantEvent.onEvent` (`DisenchantEvent.java:34`).

- Enchantments are collected: `DisenchantEvent.java:90` → `AnvilEventGuards.collectEnchantments(...)`
  (`AnvilEventGuards.java:118`), which for the vanilla case (no custom-enchant plugin active) runs the base collector
  `EventUtils.Disenchantment.getDisenchantedEnchantments` (`EventUtils.java:48`).
- Source levels are read **raw**: `EventUtils.java:72` → `EnchantmentUtils.getItemEnchantments(firstItem)`
  (`EnchantmentUtils.java:129`). For a tool/armor (not `EnchantmentStorageMeta`) it takes the else branch
  `item.getEnchantments()` (`EnchantmentUtils.java:140`) and wraps each `(key, value)` via
  `remapEnchantment(entry.getKey(), entry.getValue())` (`EnchantmentUtils.java:144`). `value` is the raw stored level;
  `remapEnchantment(...).getLevel()` returns it unchanged (`EnchantmentUtils.java:176`).
- The preview book is built: `DisenchantEvent.java:120` `new ItemStack(Material.ENCHANTED_BOOK)`, then
  `book = pluginEnchantment.addToBook(book)` (`DisenchantEvent.java:122-124`). `addToBook`
  (`EnchantmentUtils.java:181-185`) calls `addStoredEnchantment(item, enchantment, getLevel())`, which resolves to
  **`storage.addStoredEnchant(enchantment, level, true)`** (`EnchantmentUtils.java:44`) — `ignoreLevelRestriction = true`.
- Result set: `DisenchantEvent.java:142` `e.setResult(book)`. Per Javadoc, this places the item into the anvil result slot.

**No clamp in this path.** The preview book carries the raw over-max level.

### 2. Result-slot click → deliver book (the seam)

`ShatterClickListener`/`DisenchantClickListener` route the result-slot `InventoryClickEvent` into
`DisenchantClickEvent.onEvent` (`DisenchantClickEvent.java:40`).

- The delivered book is read **back out of the result slot**:
  `DisenchantClickEvent.java:87` `ItemStack result = anvilInventory.getItem(2);` — i.e. the item the server stored after
  step 1's `setResult`, not a freshly built one.
- The handler independently **re-collects** the source enchantments (with raw levels available) at
  `DisenchantClickEvent.java:103-107`, but only uses that list for eligibility/cost/removal — **it never rebuilds the book
  from it.**
- The only mutation of `result` before delivery is the per-enchantment chance roll, which merely *removes* stored
  enchants (`resultItemMeta.getStoredEnchants()` / `removeStoredEnchant`, `DisenchantClickEvent.java:214-225`) — it never
  re-adds or re-levels them.
- Delivery: `DisenchantClickEvent.java:227` `p.setItemOnCursor(result);` — the round-tripped slot item is what the player
  receives.

Because the delivered item is whatever now sits in slot 2, any server-side normalization of the result-slot ItemStack
(outside the plugin's `addUnsafe*`/`ignoreLevelRestriction` guarantees) would reach the player here.

### 3. Shatter path — same design, same guarantees

`ShatterEvent.java:139-161` builds its book identically: `new ItemStack(Material.ENCHANTED_BOOK)` →
`book = pluginEnchantment.addToBook(book)` (`ShatterEvent.java:142`, same `addStoredEnchant(..., true)`) → `e.setResult(book)`
(`ShatterEvent.java:161`). `ShatterClickEvent` delivers from the result slot the same way. So shatter **preserves over-max
levels in its transfer logic too**, and shares the same delivery-round-trip seam.

### 4. No max-level clamping exists anywhere in the codebase

A full search for `getMaxLevel` / `getStartLevel` / `maxLevel` / `clamp` / `Math.min` / `Math.max` across all modules
(`core`, `v1_18_R1`, `v1_20_R4`, `v1_21_R1`, `v1_21_R4`, `v1_21_R5`) finds **no** enchantment-level clamping — the only
`Math.min/max`/`clamp` hits are for config chance `[0.0,1.0]`, GUI stack sizes (`≤64`), cooldowns, help-page numbers, and
the shatter split-count. Nothing consults an enchantment's registry max. Confirmed also at tag `v6.5.0`: the add calls were
already `addStoredEnchant(..., true)` / `addUnsafeEnchantment` (`v6.5.0:core/.../EnchantmentUtils.java:44,47,29`), i.e. the
transfer path never clamped, even in the reported version.

---

## Exact calls involved

| Role | Call | Location | Clamps? |
|------|------|----------|---------|
| Read source item level | `item.getEnchantments()` (raw map) | `EnchantmentUtils.java:140` | No |
| Read source book level | `meta.getStoredEnchants()` (raw map) | `EnchantmentUtils.java:133` | No |
| Carry level | `remapEnchantment(...).getLevel()` returns raw `level` | `EnchantmentUtils.java:176` | No |
| Write to book | `storage.addStoredEnchant(enchantment, level, true)` | `EnchantmentUtils.java:44` | No (ignoreLevelRestriction) |
| Write to non-book item | `meta.addEnchant(enchantment, level, true)` | `EnchantmentUtils.java:47` | No (ignoreLevelRestriction) |
| Write (direct) | `item.addUnsafeEnchantment(enchantment, level)` | `EnchantmentUtils.java:29` | No (unsafe) |
| **Deliver (the seam)** | `result = anvilInventory.getItem(2)` then `p.setItemOnCursor(result)` | `DisenchantClickEvent.java:87,227` | Not the plugin — depends on the server's result-slot round-trip |

---

## Empirical proof the transfer logic preserves over-max levels

A temporary MockBukkit probe (`OverMaxProbeTest`, since removed) fired the real `DisenchantEvent` handler against an
over-max source item and read the resulting book's stored level. Run via `mvn test -pl core --offline`:

```
>>> sharpness max level = 5
>>> source sword stored sharpness = 10
>>> RESULT book sharpness level = 10      <-- preserved
>>> unbreaking max level = 3
>>> source sword stored unbreaking = 4
>>> RESULT book unbreaking level = 4      <-- preserved
Tests run: 2, Failures: 0, Errors: 0
```

So the plugin's read→wrap→`addToBook`→`setResult` pipeline preserves levels above the registry max. (MockBukkit implements
the same `ignoreLevelRestriction`/unsafe semantics as CraftBukkit; it does not model Paper's live anvil result-slot code,
which is exactly the part the probe cannot cover — see the delivery seam above.)

---

## Official Javadoc confirmation (which add-enchant methods clamp vs preserve)

Read from the Paper API Javadoc mirror (the `jd.papermc.io` build of the Bukkit API; `hub.spigotmc.org/javadocs` returned
HTTP 403 and could not be read directly):

- **`EnchantmentStorageMeta.addStoredEnchant(Enchantment ench, int level, boolean ignoreLevelRestriction)`** —
  "Stores the specified enchantment in this item meta." Parameter `ignoreLevelRestriction`: *"this indicates the enchantment
  should be applied, ignoring the level limit."* Returns "true if the item meta changed as a result of this call."
  → With `true`, the level limit is bypassed (no clamp). **This is what the plugin uses.**
  Source: https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/meta/EnchantmentStorageMeta.html
- **`ItemStack.addUnsafeEnchantment(Enchantment, int)`** — *"This method is unsafe and will ignore level restrictions or
  item type."* → bypasses the max (no clamp). **Used at `EnchantmentUtils.java:29`.**
  Source: https://jd.papermc.io/paper/1.21.4/org/bukkit/inventory/ItemStack.html
- **`ItemStack.addEnchantment(Enchantment, int)`** — the *safe* variant; documented to throw and to enforce applicability.
  This is the one that would clamp/reject above-max — **the plugin does not use it.** (Same page.)
- **`ItemMeta.addEnchant(Enchantment, int level, boolean ignoreLevelRestriction)`** — same `ignoreLevelRestriction`
  semantics as `addStoredEnchant`; the plugin passes `true` (`EnchantmentUtils.java:47`).
- **`PrepareAnvilEvent.setResult(ItemStack)`** — "Set result item, may be null." The Javadoc does **not** document whether
  the item is copied, validated, or normalized when placed into the result slot — so the round-trip in step 2 is an
  undocumented server-implementation detail, which is precisely why the plugin should not rely on `getItem(2)` to preserve
  an over-max level.
  Source: https://jd.papermc.io/paper/1.21.4/org/bukkit/event/inventory/PrepareAnvilEvent.html

---

## Is shatter affected too?

Yes, symmetrically. Shatter's transfer logic preserves levels (`ShatterEvent.java:142`, same `addStoredEnchant(..., true)`)
and its click handler delivers from the result slot the same way, so it shares the identical delivery-round-trip seam.
Any fix should be applied to both `DisenchantClickEvent` and `ShatterClickEvent`.

---

## Fix options

### Option A (recommended) — rebuild the delivered book from the collected raw-level enchantments

In `DisenchantClickEvent` (and `ShatterClickEvent`), do not deliver `anvilInventory.getItem(2)` verbatim. After collecting
`enchantments` (already done at `DisenchantClickEvent.java:103-107`, raw levels intact), build a fresh
`ENCHANTED_BOOK` via `pluginEnchantment.addToBook(book)` — the same loop the prepare handler uses
(`DisenchantEvent.java:120-124`) — apply the existing chance roll to that fresh book, then `p.setItemOnCursor(freshBook)`.
This makes delivery independent of whatever the server did to the result-slot item, and guarantees the over-max level
(written with `addStoredEnchant(..., true)`) survives.
- **Pros:** authoritative; removes reliance on an undocumented result-slot round-trip; single, well-understood code path
  shared with prepare; fixes disenchant and shatter uniformly.
- **Cons:** must re-apply the per-enchantment chance roll to the rebuilt book (logic already exists, just move it);
  slightly more work in the click handler; needs test coverage for the over-max case.

### Option A as implemented (2026-08-31) — re-stamp raw levels on the result-slot book

The literal "rebuild a fresh book from the click-collected `enchantments` list" is **unsafe**, because the click-time
list (collected with `isPrepare=false`) does **not** equal the previewed book's contents:

- **Disenchant:** with `isPrepare=false` the collector *keeps* `DELETE`-state enchantments in the list
  (`EventUtils.java:84` only strips them when `withDelete=true`); they are meant to be destroyed, not written to the book.
  Rebuilding from that list would wrongly transfer them.
- **Shatter:** the click list is the *full* eligible set of the source book, not the split-off subset the result book
  actually carries. Rebuilding from it would deliver every enchantment.

So the implemented fix keeps the result book's actual enchant **set** (`resultItemMeta.getStoredEnchants()`, correct
membership) and only restores each surviving enchant's **level** from the raw collected list, folded into the existing
per-enchantment chance-roll pass, then delivers straight to the cursor:

- `DisenchantClickEvent.java` — chance-roll block now builds `sourceLevels` (key → raw level) from `enchantments`, and
  for each surviving stored enchant re-applies `resultItemMeta.addStoredEnchant(ench, rawLevel, true)` when the slot level
  differs from the raw source level.
- `ShatterClickEvent.java` — same change against `Config.Shatterment`.
- Regression test: `DisenchantClickEventTest#givenResultSlotBookClampedBelowSourceLevel_whenClickResult_thenDeliveredBookKeepsRawLevel`
  — slot 2 holds Unbreaking **3**, source item is Unbreaking **4**; asserts the delivered cursor book is Unbreaking **4**.
  Verified red before the fix (`expected: <4> but was: <3>`), green after; full core suite 261/261.

This is delivery-independent of the server round-trip (write uses `ignoreLevelRestriction=true`, delivery goes straight to
the cursor, no second pass through slot 2) and correct for both disenchant and shatter. **Caveat unchanged:** the
underlying server-side clamp is still a hypothesis; if a live repro shows the result-slot book already carries the correct
level, this fix is a harmless no-op (the `rawLevel.equals(slotLevel)` guard skips it) and the real clamp point is elsewhere.

### Option B — keep the write calls as-is (they are already correct)

No change needed to `EnchantmentUtils` — it already uses `addStoredEnchant(ench, level, true)` / `addUnsafeEnchantment`,
which the Javadoc confirms do not clamp. There is **no** call to swap to a "safe" clamping variant, and no clamp to remove;
the earlier hypothesis that the fix is "switch `addEnchant(..., false)` → `addStoredEnchant(..., true)`" does not apply here
because the code already uses the unsafe/ignore-restriction variants.

### Confirmation steps (to convert the hypothesis to a confirmed root cause on a live server)

1. Reproduce on the reporter's exact Paper build; enable the plugin's diagnostics (`DiagnosticUtils.debug("DISENCHANT", ...)`)
   and read the `enchantments=[key:level]` line logged at `DisenchantEvent.java:101-104` and `DisenchantClickEvent.java:123-126`
   — confirm the plugin *sees* level 4 on the source (proves the read is not the problem).
2. Inspect the actual delivered book's `minecraft:enchantments` component (`/data get` or an NBT viewer) vs. the source item's,
   to see whether the drop from 4→3 happens between `setResult` (step 1) and `getItem(2)` (step 2). If yes, the result-slot
   round-trip is confirmed and Option A fixes it.
3. Verify the source item truly stores level 4 (vanilla `/enchant` rejects above-max, so the item must have been created via
   components/another plugin) — rules out "the item never had 4" as the trivial explanation.

---

## Files referenced

- `core/src/main/java/com/jankominek/disenchantment/events/DisenchantEvent.java` (build preview book, `setResult`)
- `core/src/main/java/com/jankominek/disenchantment/events/DisenchantClickEvent.java` (delivery seam: `getItem(2)`, `setItemOnCursor`)
- `core/src/main/java/com/jankominek/disenchantment/events/ShatterEvent.java` / `ShatterClickEvent.java` (same design)
- `core/src/main/java/com/jankominek/disenchantment/utils/EnchantmentUtils.java` (read/write calls, all non-clamping)
- `core/src/main/java/com/jankominek/disenchantment/utils/EventUtils.java` (eligibility + raw-level collection)
- `core/src/main/java/com/jankominek/disenchantment/events/AnvilEventGuards.java` (`collectEnchantments`)
