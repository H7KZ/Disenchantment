<!-- generated-by: gsd-doc-writer -->

# Utilities Reference

`core/src/main/java/com/jankominek/disenchantment/utils/`

---

## `AnvilCostUtils`

`utils/AnvilCostUtils.java`

Handles XP cost calculation and NMS delegation for anvil repair cost read/write.

### Methods

```java
public static int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView)
```

Delegates to `nms.getRepairCost(...)`. Returns the current cost in experience levels.

```java
public static void setItemRepairCost(ItemStack item, int repairCost)
```

Delegates to `nms.setItemRepairCost(item, repairCost)`. Sets the NBT repair cost on an item, used to reset an item's
work-penalty after disenchanting.

```java
public static void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost)
```

Delegates to `nms.setAnvilRepairCost(...)`. Sets the displayed cost in the anvil UI.

### Cost Formula

```java
public static int costForEnchantment(String key, int level, double base, double multiplier, Map<String, Integer> overrides)
```

For a single enchantment:

- If the key has a fixed override: returns `overrides.get(key)`.
- Otherwise: returns `(int) Math.round(base + level * multiplier)`.

```java
public static int countAnvilCost(List<IPluginEnchantment> enchantments, AnvilEventType anvilEventType)
```

Total cost for all enchantments:

1. Reads `base` and `multiplier` from config (disenchantment or shatterment branch).
2. If cost is disabled, returns 0.
3. Sorts enchantments by level descending.
4. Seeds `totalCost = base` (config base value).
5. For each enchantment:
    - If an override exists: `totalCost += overrides.get(key)`. **Does not advance multiplier.**
    - Otherwise: `totalCost += level * currentMultiplier`. Then `multiplier += baseMultiplier` (accumulating
      multiplier).
6. Returns `(int) Math.round(totalCost)`.

**Example:** base=1, multiplier=1, enchantments=[Sharpness V, Looting III]

- Sorted: [Sharpness V (5), Looting III (3)]
- Start: totalCost=1 (base), multiplier=1
- Sharpness V: totalCost += 5 * 1 = 5 → totalCost=6, multiplier=2
- Looting III: totalCost += 3 * 2 = 6 → totalCost=12
- Result: 12 XP levels

---

## `DiagnosticUtils`

`utils/DiagnosticUtils.java`

Generates diagnostic reports and handles crash logging.

### `debug(String category, String msg)`

```java
public static void debug(String category, String msg)
```

Logs `[DEBUG][CATEGORY] msg` at INFO level to the plugin logger, but only when `Config.Logging.getLevel()` is at least
`DEBUG`. Safe to call before config is loaded (silently no-ops if `Disenchantment.config == null`).

Categories used throughout the codebase: `DISENCHANT`, `SHATTER`, `NMS`, `ECONOMY`, `ADAPTER`, `STARTUP`, `CONFIG`,
`GUI`, `UPDATE`.

### `isDebugEnabled()`

```java
public static boolean isDebugEnabled()
```

Returns `true` if log level is DEBUG. Used to guard expensive string concatenation in hot paths.

### `throwReport(Throwable e)`

```java
public static void throwReport(Throwable e)
```

Generates a full diagnostic report including plugin version, server info, NMS status, memory, all config values, active
plugins, and PrepareAnvil listeners. Appends the full exception cause chain. Logs at SEVERE. If
`Config.Logging.isSaveReportsEnabled()` is true (or config is not yet loaded), also writes to
`plugins/Disenchantment/logs/crash-<timestamp>.txt`.

### `getReport(Boolean extended, CommandSender sender)`

```java
public static String getReport(Boolean extended, CommandSender sender)
```

Returns a formatted string report. `extended=true` adds sound settings, disabled worlds, enchantment states per feature,
and (if sender is a Player) their effective `disenchantment.*` permissions. Used by the `Diagnostic` command.

### `saveReportToFile(String content, String prefix)`

```java
public static String saveReportToFile(String content, String prefix)
```

Writes `content` to `plugins/Disenchantment/logs/<prefix>-<timestamp>.txt`. Returns the absolute path or `null` on
failure.

---

## `EconomyUtils`

`utils/EconomyUtils.java`

Manages the optional Vault/VaultUnlocked economy hook.

```java
public static boolean setup()
```

Attempts to hook into Vault or VaultUnlocked. Checks for either plugin via `Bukkit.getPluginManager().getPlugin(...)`.
If neither present, returns false. Otherwise looks up `RegisteredServiceProvider<Economy>` and caches the provider.
Called from `ServerLoadEvent` (not `onEnable()`) to ensure VaultUnlocked's bridge service is registered first.

```java
public static boolean isAvailable()
```

Returns `economy != null`.

```java
public static boolean has(Player player, double amount)
```

Checks if the player has at least `amount` in their balance.

```java
public static EconomyResponse withdraw(Player player, double amount)
```

Withdraws `amount` from the player's balance. Returns Vault's `EconomyResponse`.

```java
public static String format(double amount)
```

Formats `amount` as a currency string using the economy plugin's own formatter (e.g. `"$100.00"`). Falls back to
`String.valueOf(amount)` if no economy is hooked.

```java
public static void reset()
```

Sets the cached `economy` reference to null. Called during `disable()`.

---

## `EventUtils`

`utils/EventUtils.java`

Contains the business logic that determines which enchantments are eligible for transfer or splitting. Called by both
the `PrepareAnvilEvent` and `InventoryClickEvent` handlers.

### `EventUtils.Disenchantment`

```java
public static List<IPluginEnchantment> getDisenchantedEnchantments(
    ItemStack firstItem, ItemStack secondItem, boolean withDelete)
```

Vanilla overload (no plugin). Validates:

- Both items non-null and non-air.
- First item is NOT an enchanted book (disenchanting a book onto a book is not allowed).
- Second item IS a plain `BOOK`.
- First item's material is not in the disabled materials list.
- Second item has no enchantments (must be blank).
- No enchantment on the first item has state `DISABLE` (if any do, the entire operation is blocked).

Then applies enchantment state filtering: removes enchantments with state `KEEP` from the result. If `withDelete=true`
(PrepareAnvil — show what will happen), also removes `DELETE` state enchantments from the result set (they will be
removed from the source item but not placed on the book). If `withDelete=false` (InventoryClick — execution), DELETE
enchantments remain in the result so they can be removed from the source.

```java
public static List<IPluginEnchantment> getDisenchantedEnchantments(
    ItemStack firstItem, ItemStack secondItem, boolean withDelete,
    ISupportedPlugin activatedPlugin, World world)
```

Plugin overload. Same validation logic but reads enchantments via `activatedPlugin.getItemEnchantments(item, world)`.

```java
public static List<IPluginEnchantment> findEnchantmentsToDelete(List<IPluginEnchantment> enchantments)
```

Filters the provided list to only those with state `DELETE` in `Config.Disenchantment.getEnchantmentStates()`. These are
removed from the source item but not added to the result book.

### `EventUtils.Shatterment`

```java
public static List<IPluginEnchantment> getShattermentEnchantments(
    ItemStack firstItem, ItemStack secondItem, boolean withDelete)
```

Validates:

- First item IS `ENCHANTED_BOOK`.
- Second item IS a plain `BOOK` (blank).
- Book has at least 2 enchantments (shatter requires multi-enchant book).
- No enchantment has state `DISABLE`.

Then applies state filtering identically to the disenchantment overload.

```java
public static List<IPluginEnchantment> getShattermentEnchantments(
    ItemStack firstItem, ItemStack secondItem, boolean withDelete,
    ISupportedPlugin activatedPlugin, World world)
```

Plugin overload.

```java
public static List<IPluginEnchantment> findEnchantmentsToDelete(List<IPluginEnchantment> enchantments)
```

Same as disenchantment variant but reads from `Config.Shatterment.getEnchantmentStates()`.

---

## `SchedulerUtils`

`utils/SchedulerUtils.java`

Abstracts task scheduling for both Bukkit/Spigot and Folia (Paper's regionized multithreading). Uses reflection at
class-load time to detect and cache Folia scheduler API methods. If Folia class
`io.papermc.paper.threadedregions.RegionizedServer` is not found, `IS_FOLIA = false` and all Bukkit scheduler fallbacks
are used.

### Folia detection

```java
public static boolean isFolia()
```

Returns `IS_FOLIA` — determined at class load via `Class.forName("io.papermc.paper.threadedregions.RegionizedServer")`.

### Methods

```java
public static void runForEntity(Plugin plugin, Entity entity, Runnable task)
```

Runs `task` on the entity's owning region thread (Folia) or main thread (Bukkit).

```java
public static void runForEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks)
```

Runs `task` after `delayTicks` on the entity's owning region thread or main thread. Used for the 2-tick deferred
second-item removal.

```java
public static void runGlobal(Plugin plugin, Runnable task)
```

Runs on the global region scheduler (Folia) or main thread (Bukkit). Used for non-entity-bound tasks.

```java
public static Object runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks)
```

Schedules a repeating async task. On Folia, converts ticks to milliseconds (1 tick = 50ms). Returns the task object (
`ScheduledTask` on Folia, `BukkitTask` on Bukkit). Used by `UpdateChecker.run()`.

```java
public static void cancelTask(Object task)
```

Cancels a task object. Handles both Folia `ScheduledTask` (via `taskCancel.invoke(task)`) and Bukkit `BukkitTask`.

```java
public static void cancelAllTasks(Plugin plugin)
```

Cancels all tasks for the plugin. On Folia cancels both global and async schedulers.

---

## `ConfigUtils`

See [CONFIG_SYSTEM.md](CONFIG_SYSTEM.md) — `setupConfig()` and `setupLocaleConfigs()`.

---

## `EnchantmentUtils`

`utils/EnchantmentUtils.java`

Provides vanilla-side enchantment operations. Key methods:

```java
public static List<Enchantment> getRegisteredEnchantments()
```

Delegates to `nms.getRegisteredEnchantments()`.

```java
public static List<IPluginEnchantment> getItemEnchantments(ItemStack item)
```

Returns vanilla enchantments on the item wrapped as `IPluginEnchantment`. Reads from `EnchantmentStorageMeta` for
enchanted books, or `ItemMeta.getEnchants()` for regular items.

```java
public static ItemStack removeEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments)
```

Removes the given enchantments from the item's meta. Used during disenchant click execution.

---

## `MaterialUtils`

`utils/MaterialUtils.java`

Helper for material-related checks. Used to filter the materials list in GUI and config.

---

## `MapUtils`

`utils/MapUtils.java`

General map utility methods. Used for map manipulations in enchantment state processing.

---

## `PrecisionUtils`

`utils/PrecisionUtils.java`

Provides rounding and precision helpers for cost display formatting.

---

## `UpdateChecker`

`utils/UpdateChecker.java`

Checks for new plugin versions by querying `https://api.spigotmc.org/legacy/update.php?resource=110741`.

```java
public UpdateChecker(int resourceId)
// Constructor. resourceId = Disenchantment.spigotmcId = 110741.

public void getVersion(Consumer<String> consumer)
// Opens an HTTP connection to SpigotMC API and passes the latest version string to consumer.
// Non-blocking when called from an async context.

public Runnable runnableUpdateTask(String version)
// Returns a Runnable that calls getVersion() and logs a warning if a newer version is available.

public Object run(Plugin plugin, String version)
// Schedules runnableUpdateTask() as a repeating async timer:
//   initial delay: 3 * 20 ticks (3 seconds)
//   period: 8 * 60 * 60 * 20 ticks (8 hours)
// Returns the task object (added to Disenchantment.tasks for cleanup on disable).
```

---

## `BStatsMetrics`

`utils/BStatsMetrics.java`

Integrates with the bStats metrics service (https://bstats.org). Initialized in `enable()` with:

```java
new BStatsMetrics(plugin, bstatsId);  // bstatsId = 19058
```

Sends anonymous usage statistics (server version, plugin version, Java version, OS) to bStats. Players can opt out via
the global bStats config at `plugins/bStats/config.yml`. No personal data is collected.
