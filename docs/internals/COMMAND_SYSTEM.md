<!-- generated-by: gsd-doc-writer -->

# Command System

`core/src/main/java/com/jankominek/disenchantment/commands/`

The plugin uses a single root command `disenchantment` (with alias `de`) that routes to subcommand handlers via
`CommandRegister`. Each subcommand is declared as a `CommandBuilder` record in its own class inside `impl/`.

---

## Key Classes

### `CommandRegister`

`commands/CommandRegister.java` — implements `CommandExecutor`.

Holds a static `Map<String, CommandBuilder> commands` populated in a static initializer with all subcommands. When
Bukkit calls `onCommand()`, the register:

1. If `args` is empty, routes to `"help"`.
2. Otherwise looks up `args[0].toLowerCase()` in the map.
3. If not found, falls back to `"help"`.
4. Calls `command.execute(sender, args)`.
5. Wraps everything in a try-catch that calls `DiagnosticUtils.throwReport(e)` on exception.

Registered subcommands (as of current source):

| Key                       | Class                    |
|---------------------------|--------------------------|
| `help`                    | `Help`                   |
| `gui`                     | `GUI`                    |
| `status`                  | `Status`                 |
| `toggle`                  | `Toggle`                 |
| `disenchant-enchantments` | `DisenchantEnchantments` |
| `disenchant-worlds`       | `DisenchantWorlds`       |
| `disenchant-materials`    | `DisenchantMaterials`    |
| `disenchant-repair`       | `DisenchantRepair`       |
| `disenchant-economy`      | `DisenchantEconomy`      |
| `disenchant-sound`        | `DisenchantSound`        |
| `shatter-enchantments`    | `ShatterEnchantments`    |
| `shatter-worlds`          | `ShatterWorlds`          |
| `shatter-repair`          | `ShatterRepair`          |
| `shatter-economy`         | `ShatterEconomy`         |
| `shatter-sound`           | `ShatterSound`           |
| `diagnostic`              | `Diagnostic`             |
| `reload`                  | `Reload`                 |

---

### `CommandBuilder`

`commands/CommandBuilder.java` — a Java `record`.

```java
public record CommandBuilder(
    String name,
    PermissionGroupType permission,
    String[] args,
    boolean requireArgs,
    ICommandExecutor executor,
    ICommandCompleter completer
)
```

Fields:

- `name` — the subcommand key used in the `CommandRegister` map.
- `permission` — the `PermissionGroupType` checked before execution. If the sender lacks it, they receive
  `I18n.Messages.requiresPermission()`.
- `args` — valid second arguments (e.g. `["enable", "disable"]`). Used for validation when `requireArgs = true`.
- `requireArgs` — if `true`, the second argument must be one of `args`; otherwise `args` is informational only (for tab
  completion).
- `executor` — lambda implementing `ICommandExecutor.execute(CommandSender, String[])`.
- `completer` — lambda implementing `ICommandCompleter.onTabComplete(CommandSender, String[])`.

`execute(CommandSender sender, String[] args)` logic:

1. Checks `permission.hasPermission(sender)` — sends error message and returns if denied.
2. If `args` is empty or `requireArgs` is false, calls `executor.execute(sender, args)`.
3. If `requireArgs`, validates that `args[1]` matches one of `this.args`. Sends `invalidArgument()` if not.
4. Always returns `true`.

---

### `CommandCompleter`

`commands/CommandCompleter.java` — implements `TabCompleter`.

Looks up the entered subcommand in `CommandRegister.commands`, then calls `command.onTabComplete(sender, args)` on it.
Returns subcommand name suggestions when `args.length == 1`, delegating to each `CommandBuilder`'s completer for deeper
arguments.

---

### `ICommandExecutor` / `ICommandCompleter`

Functional interfaces:

```java
@FunctionalInterface
public interface ICommandExecutor {
    void execute(CommandSender sender, String[] args);
}

@FunctionalInterface
public interface ICommandCompleter {
    List<String> onTabComplete(CommandSender sender, String[] args);
}
```

Both are implemented inline as lambdas when constructing `CommandBuilder` instances in each `impl/` class.

---

## Individual Command Implementations (`impl/`)

Each class in `impl/` declares a `public static final CommandBuilder command` field. The register uses this static field
directly.

| Class                    | Purpose                                                                                          |
|--------------------------|--------------------------------------------------------------------------------------------------|
| `Help`                   | Sends paginated help text from `I18n.Commands.Help.pages()` (8 pages)                            |
| `GUI`                    | Opens `NavigationGUI` inventory for the sender                                                   |
| `Status`                 | Prints plugin/disenchantment/shatterment enabled state via `I18n.Commands.Status.*`              |
| `Toggle`                 | Calls `Disenchantment.onToggle(boolean)` and `Config.setPluginEnabled(boolean)`                  |
| `DisenchantEnchantments` | Lists/sets per-enchantment states for disenchantment                                             |
| `DisenchantWorlds`       | Lists/adds/removes disabled worlds for disenchantment                                            |
| `DisenchantMaterials`    | Lists/enables/disables materials for disenchantment                                              |
| `DisenchantRepair`       | Gets/sets XP cost settings (cost enabled, reset enabled, base, multiplier) for disenchantment    |
| `DisenchantEconomy`      | Gets/sets Vault economy settings for disenchantment                                              |
| `DisenchantSound`        | Gets/sets sound settings for disenchantment                                                      |
| `ShatterEnchantments`    | Lists/sets per-enchantment states for shatterment                                                |
| `ShatterWorlds`          | Lists/adds/removes disabled worlds for shatterment                                               |
| `ShatterRepair`          | Gets/sets XP cost settings for shatterment                                                       |
| `ShatterEconomy`         | Gets/sets Vault economy settings for shatterment                                                 |
| `ShatterSound`           | Gets/sets sound settings for shatterment                                                         |
| `Diagnostic`             | Generates and prints a `DiagnosticUtils.getReport(extended, sender)`                             |
| `Reload`                 | Calls `ConfigUtils.setupConfig()` + `ConfigUtils.setupLocaleConfigs()` and reloads locale config |

---

## How to Add a New Command

1. Create a new class in `core/src/main/java/com/jankominek/disenchantment/commands/impl/`.
2. Declare:
   ```java
   public static final CommandBuilder command = new CommandBuilder(
       "my-subcommand",
       PermissionGroupType.ADMIN,  // pick appropriate permission group
       new String[]{"on", "off"}, // valid second args, or empty array
       true,                       // requireArgs
       (sender, args) -> {
           // command logic here
       },
       (sender, args) -> {
           // return List<String> of suggestions
           return List.of("on", "off");
       }
   );
   ```
3. Register it in `CommandRegister.commands`:
   ```java
   put(MyCommand.command.name(), MyCommand.command);
   ```
4. Add the subcommand to `plugin.yml` if it needs Bukkit permission nodes.
5. Add any new locale strings to `I18nKeys` enum and `en.yml` / `cs.yml`.

---

## Permissions

Each `CommandBuilder` references a `PermissionGroupType`. The `PermissionGroupType` enum (in `types/`) maps to actual
permission strings defined in `plugin.yml`. Operators receive these by default.

The `hasPermission(CommandSender sender)` method on `PermissionGroupType` checks both permission state and op status.
The GUI variant `hasPermission(HumanEntity entity, boolean sendMessage)` optionally sends a denial message.
