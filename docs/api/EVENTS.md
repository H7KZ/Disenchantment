<!-- generated-by: gsd-doc-writer -->

# API Events

Disenchantment fires four public Bukkit events that other plugins can listen to. Two events are fired for the disenchant
operation (removing enchantments from gear onto a book) and two for the shatter operation (splitting a multi-enchantment
book).

All events are in the package `com.jankominek.disenchantment.events.api`.

---

## PreDisenchantEvent

**Package:** `com.jankominek.disenchantment.events.api`
**Cancellable:** Yes **When it fires:** Immediately before a disenchant operation executes. Cancelling this event
prevents the enchantments from being transferred and the XP cost from being applied.

### Methods

| Return type                | Method                         | Description                                           |
|----------------------------|--------------------------------|-------------------------------------------------------|
| `Player`                   | `getPlayer()`                  | The player who triggered the disenchant               |
| `ItemStack`                | `getSourceItem()`              | The enchanted item placed in the anvil's first slot   |
| `List<IPluginEnchantment>` | `getEnchantments()`            | The enchantments that will be transferred to the book |
| `boolean`                  | `isCancelled()`                | Whether the event has been cancelled                  |
| `void`                     | `setCancelled(boolean cancel)` | Cancel or un-cancel the event                         |

### Code Example

```java
import com.jankominek.disenchantment.events.api.PreDisenchantEvent;
import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    @EventHandler
    public void onPreDisenchant(PreDisenchantEvent event) {
        // Example: block disenchanting in the Nether
        if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NETHER) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot disenchant in the Nether.");
            return;
        }

        // Example: log the enchantments that are about to be transferred
        for (IPluginEnchantment enchant : event.getEnchantments()) {
            getLogger().info(event.getPlayer().getName()
                + " is disenchanting: " + enchant.getKey()
                + " level " + enchant.getLevel());
        }
    }
}
```

### Use Cases

- Restricting disenchanting in specific worlds or regions (e.g. using a region guard plugin)
- Blocking disenchanting of specific enchantments (inspect `getEnchantments()` and cancel if a forbidden key is present)
- Permission checks beyond the built-in `disenchantment.disenchant` node
- Logging or auditing which enchantments players are removing and from which items
- Charging a custom currency cost before the operation via an economy plugin

---

## PostDisenchantEvent

**Package:** `com.jankominek.disenchantment.events.api`
**Cancellable:** No **When it fires:** After a disenchant operation has completed successfully. The result book and
modified source item have already been placed into the player's inventory.

### Methods

| Return type | Method                    | Description                                             |
|-------------|---------------------------|---------------------------------------------------------|
| `Player`    | `getPlayer()`             | The player who performed the disenchant                 |
| `ItemStack` | `getResultBook()`         | The enchanted book produced by the operation            |
| `ItemStack` | `getModifiedSourceItem()` | The source item after enchantments were removed from it |

### Code Example

```java
import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    @EventHandler
    public void onPostDisenchant(PostDisenchantEvent event) {
        // Example: record the transaction in a database or log file
        logger.info("[Disenchant] "
            + event.getPlayer().getName()
            + " produced book: " + event.getResultBook().getItemMeta().getDisplayName()
            + " | Source item type: " + event.getModifiedSourceItem().getType());
    }
}
```

### Use Cases

- Logging completed disenchant operations for analytics or audit trails
- Giving bonus rewards after a successful disenchant (e.g. extra XP, tokens)
- Updating an external database or economy balance after the operation resolves
- Triggering quest or achievement progress in a quest plugin

---

## PreShatterEvent

**Package:** `com.jankominek.disenchantment.events.api`
**Cancellable:** Yes **When it fires:** Immediately before a shatter operation executes. Shattering splits one
enchantment off a multi-enchantment book onto a blank book. Cancelling this event prevents the split from occurring.

### Methods

| Return type                | Method                         | Description                                         |
|----------------------------|--------------------------------|-----------------------------------------------------|
| `Player`                   | `getPlayer()`                  | The player who triggered the shatter                |
| `ItemStack`                | `getSourceItem()`              | The enchanted book placed in the anvil's first slot |
| `List<IPluginEnchantment>` | `getEnchantments()`            | The enchantments present on the source book         |
| `boolean`                  | `isCancelled()`                | Whether the event has been cancelled                |
| `void`                     | `setCancelled(boolean cancel)` | Cancel or un-cancel the event                       |

### Code Example

```java
import com.jankominek.disenchantment.events.api.PreShatterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    @EventHandler
    public void onPreShatter(PreShatterEvent event) {
        // Example: require a permission to shatter specific enchantments
        if (!event.getPlayer().hasPermission("myplugin.shatter.unlimited")) {
            long rareCount = event.getEnchantments().stream()
                .filter(e -> RARE_ENCHANTS.contains(e.getKey()))
                .count();
            if (rareCount > 0) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You need the 'myplugin.shatter.unlimited' permission to shatter rare enchantments.");
            }
        }
    }
}
```

### Use Cases

- Restricting book-splitting in specific worlds or regions
- Blocking splits of certain high-value enchantments as a server economy protection measure
- Custom permission checks on top of the built-in `disenchantment.shatter` node
- Rate-limiting how often a player can shatter books

---

## PostShatterEvent

**Package:** `com.jankominek.disenchantment.events.api`
**Cancellable:** No **When it fires:** After a shatter operation has completed successfully. The result book containing
the split enchantment and the modified source book have already been placed into the player's inventory.

### Methods

| Return type | Method                    | Description                                                 |
|-------------|---------------------------|-------------------------------------------------------------|
| `Player`    | `getPlayer()`             | The player who performed the shatter                        |
| `ItemStack` | `getResultBook()`         | The new book produced, containing the split enchantment     |
| `ItemStack` | `getModifiedSourceItem()` | The original book after the enchantment was removed from it |

### Code Example

```java
import com.jankominek.disenchantment.events.api.PostShatterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {

    @EventHandler
    public void onPostShatter(PostShatterEvent event) {
        // Example: award a "Book Splitter" achievement
        achievementPlugin.grant(event.getPlayer(), "book_splitter");
    }
}
```

### Use Cases

- Logging completed shatter operations
- Awarding achievements or quest progress
- Giving bonus rewards after a successful shatter
- Updating an economy balance or tracking resource usage

---

## IPluginEnchantment

**Package:** `com.jankominek.disenchantment.plugins`

`IPluginEnchantment` is the interface used in event payloads to represent a single enchantment, regardless of whether it
originates from vanilla Bukkit or a third-party custom enchantment plugin (AdvancedEnchantments, EcoEnchants, etc.).

### Methods

| Return type | Method                           | Description                                                                                                                                                                                             |
|-------------|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `String`    | `getKey()`                       | The unique key identifying this enchantment. For vanilla enchantments this matches the Bukkit namespaced key (e.g. `minecraft:sharpness`). For custom plugin enchantments the format is plugin-defined. |
| `int`       | `getLevel()`                     | The level of this enchantment on the item it was read from                                                                                                                                              |
| `ItemStack` | `addToBook(ItemStack book)`      | Applies this enchantment to an enchanted book and returns the modified book                                                                                                                             |
| `ItemStack` | `removeFromBook(ItemStack book)` | Removes this enchantment from an enchanted book and returns the modified book                                                                                                                           |
| `ItemStack` | `addToItem(ItemStack item)`      | Applies this enchantment to an item and returns the modified item                                                                                                                                       |
| `ItemStack` | `removeFromItem(ItemStack item)` | Removes this enchantment from an item and returns the modified item                                                                                                                                     |

The `addToBook` and `removeFromBook` methods default to `addToItem`/`removeFromItem` respectively. Custom plugin adapter
implementations override all four methods with plugin-specific logic.

In typical API usage you only need `getKey()` and `getLevel()` to inspect which enchantments are involved in an
operation.
