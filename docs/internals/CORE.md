<!-- generated-by: gsd-doc-writer -->

# Core Plugin Class

`core/src/main/java/com/jankominek/disenchantment/Disenchantment.java`

The main entry point. Extends `JavaPlugin`. All subsystems are initialized here and the globally-shared static
references live here.

---

## Static Globals

These fields are set once during `enable()` and accessed throughout the codebase via static imports.

| Field          | Type                       | Purpose                                                                                         |
|----------------|----------------------------|-------------------------------------------------------------------------------------------------|
| `plugin`       | `Disenchantment`           | The `JavaPlugin` instance. Used to access `getDataFolder()`, `getServer()`, scheduler, etc.     |
| `nms`          | `NMS`                      | The version-specific NMS implementation loaded by `NMSMapper`.                                  |
| `config`       | `FileConfiguration`        | The live plugin config. Set from `getConfig()` after `ConfigUtils.setupConfig()`.               |
| `localeConfig` | `FileConfiguration`        | The active locale file loaded into memory. Set from `YamlConfiguration.loadConfiguration(...)`. |
| `logger`       | `java.util.logging.Logger` | The plugin logger. Set from `getLogger()`.                                                      |
| `enabled`      | `boolean`                  | Runtime toggle. `true` by default; read by all event handlers before processing.                |
| `startedAt`    | `Instant`                  | Captured at start of `enable()`. Used to calculate uptime in diagnostic reports.                |
| `commandName`  | `String` (static final)    | `"disenchantment"` — the root command registered in `plugin.yml`.                               |
| `spigotmcId`   | `int` (static final)       | `110741` — SpigotMC resource ID for the update checker.                                         |
| `bstatsId`     | `int` (static final)       | `19058` — bStats plugin ID.                                                                     |

---

## Plugin Lifecycle

### `onEnable()` / `onDisable()`

These are the standard Bukkit lifecycle methods. They are **thin wrappers** that call `enable()` or `disable()` inside a
try-catch:

```java
@Override
public void onEnable() {
    try {
        this.enable();
    } catch (Exception e) {
        DiagnosticUtils.throwReport(e);
        getServer().getPluginManager().disablePlugin(this);
    }
}
```

**Why the wrapper?** Bukkit's `onEnable()` does not propagate exceptions cleanly — the plugin silently enters a broken
state if an unchecked exception escapes. By catching here, the plugin generates a full crash report (including config
state and server info) and explicitly disables itself.

---

### `enable()` — Startup Sequence

Called from `onEnable()`. Runs the following steps **in order**:

1. **Set static instances** — `plugin = this`, `logger = getLogger()`, `startedAt = Instant.now()`.
2. **Config setup** — `ConfigUtils.setupConfig()` (saves default, strictly parses `config.yml`, applies migrations with a
   `config.yml.bak` backup, reloads). A YAML syntax error never overwrites the file: the broken file is copied to
   `config.yml.broken-<timestamp>`, defaults are used in memory and config saves are suppressed. Then
   `config = getConfig()`.
3. **NMS loading** — `NMSMapper.setup()` reflectively loads the correct `NMS_v*` implementation. If `null` is returned,
   the plugin logs a severe error and calls `disablePlugin(this)` immediately.
4. **Locale setup** — `ConfigUtils.setupLocaleConfigs()` extracts locale files from the JAR (does not overwrite existing
   ones, but merges in any keys the bundled version has that the existing file lacks). Then the configured locale file is loaded: first from `plugins/Disenchantment/locales/<locale>.yml`, then from
   the JAR resource. If neither exists, the plugin disables.
5. **Runtime enabled flag** — `Disenchantment.enabled = Config.isPluginEnabled()`.
6. **Plugin adapter activation** — Iterates all loaded server plugins by name and calls
   `SupportedPluginManager.activatePlugins(activatedPlugins)`.
7. **Economy hook (ServerLoadEvent)** — Registers a one-shot `ServerLoadEvent` listener that calls
   `EconomyUtils.setup()`. This is deferred because VaultUnlocked registers its bridge after all `onEnable()` calls
   finish; hooking at `ServerLoadEvent` guarantees the Vault service is available.
8. **Startup logging** — If log level is INFO or DEBUG, logs NMS module name and active plugin adapters. Debug level
   additionally logs event priorities, scheduler type, and economy/feature states.
9. **Listener registration** — Instantiates `DisenchantListener`, `DisenchantClickListener`, `ShatterListener`,
   `ShatterClickListener` (each with their configured `EventPriority`). Also registers `GUIClickEvent` as a standard
   `Listener`.
10. **Command registration** — Wires `CommandRegister` as executor and `CommandCompleter` as tab completer for the
    `disenchantment` command.
11. **bStats** — `metrics = new BStatsMetrics(plugin, bstatsId)` (stored in a field so it can be shut down later).
    If `logging.operations` is enabled, `StatsManager.init(dataFolder, metrics)` follows.
12. **Update checker** — `new UpdateChecker(spigotmcId).run(plugin, version)` — adds the returned task to the `tasks`
    list. Runs every 8 hours asynchronously.
13. **PlaceholderAPI** — If `PlaceholderAPI` plugin is present, registers `DisenchantmentPlaceholderExpansion`.

---

### `disable()` — Shutdown Sequence

Called from `onDisable()`. Runs:

1. Iterates `tasks` and calls `SchedulerUtils.cancelTask(task)` for each.
2. Calls `SchedulerUtils.cancelAllTasks(plugin)` for any tasks not in the list.
3. Calls `SupportedPluginManager.deactivateAllPlugins()` — clears the activated adapters list.
4. Calls `EconomyUtils.reset()` — nulls the cached `Economy` provider.
5. Calls `StatsManager.shutdown()` and `metrics.shutdown()` (the bStats submitter would otherwise keep running).

`reload()` does the same for stats/bStats: it shuts down the old `StatsManager` **and** the old `BStatsMetrics`
before creating new ones, so reloads never leave duplicate bStats submitters behind.

---

### `onToggle(boolean enable)`

```java
public static void onToggle(boolean enable) {
    enabled = enable;
}
```

A runtime toggle that does **not** reload the plugin. Called by the `/disenchantment toggle` command and the
NavigationGUI plugin-toggle button. Sets `Disenchantment.enabled` which is checked at the top of every event handler:

```java
if (!Config.isPluginEnabled() || !Disenchantment.enabled) return;
// Note: the actual check uses Config.isPluginEnabled() which reads from config,
// whereas enabled is the in-memory runtime flag for instant toggle without config write.
```

In practice, event handlers check `Config.isPluginEnabled()` (reads YAML config) and toggle only writes
`Disenchantment.enabled`. The toggle command also calls `Config.setPluginEnabled(boolean)` to persist the change.

---

## Task Management

```java
private final ArrayList<Object> tasks = new ArrayList<>();
```

Stored as `Object` (not `BukkitTask`) to support both Folia `ScheduledTask` and Bukkit `BukkitTask` objects through the
same list. The `SchedulerUtils.cancelTask(Object)` method handles both types via reflection and instanceof checks.

Currently the only task added is the update checker. The list is iterated on disable.

---

## Notes for Adding Startup Steps

If you need to run something at startup, add it in `enable()` after step 5 (once `enabled` is set) but before listener
registration if it affects listener behavior, or after listener registration for background tasks. Always add
long-running async tasks to `tasks` so they are cancelled on shutdown.

---

## Stats System (`stats/`)

`StatsManager` is a nullable singleton (`getInstance()` is `null` when `logging.operations: false`). `StatsCache` is the
in-memory read path; `StatsDatabase` is the SQLite store, written through a single-thread executor.

- **Boot**: `StatsManager.init` loads aggregates on the DB writer thread, then applies them to the cache and registers
  `StatsListener` through `SchedulerUtils.runGlobal` (global region thread on Folia, main thread on Spigot). Never use
  `Bukkit.getScheduler()` here — it throws `UnsupportedOperationException` on Folia. Failures in the callback are logged
  at SEVERE, not swallowed. The callback is skipped if the manager was shut down/replaced (reload) in the meantime.
- **Thread safety**: `StatsCache` totals are `LongAdder`/`DoubleAdder`; maps are `ConcurrentHashMap` updated with atomic
  `merge`. `record()` is safe from any region thread; readers (PAPI, bStats, GUI) may be async. `initialize()` is
  additive.
- **Pruning**: `StatsManager.pruneAsync(days)` / `StatsDatabase.pruneOlderThan(days)` delete operation records older than
  `days` days on the writer executor (no-op for `days <= 0`). Cached totals are not adjusted until the next boot.
- **Schema**: `operations` table with indexes on `player_uuid`, `operation_type`, `timestamp`. The legacy
  `idx_enchantment_keys` index is dropped on init.
