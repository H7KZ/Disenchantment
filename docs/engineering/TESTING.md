<!-- generated-by: gsd-doc-writer -->

# Testing

This document describes the test structure, how to run tests, what is covered, and how to write new tests.
See [SETUP.md](SETUP.md) for environment prerequisites.

## Test framework and location

| Component         | Version                 |
|-------------------|-------------------------|
| JUnit 5 (Jupiter) | 5.10.2                  |
| MockBukkit        | 4.39.0 (Paper 1.21 API) |
| Mockito           | 5.18.0                  |

All unit tests live in:

```
core/src/test/java/com/jankominek/disenchantment/
```

There are no tests in the `v*` NMS modules — NMS behaviour is abstracted behind `MockNMS` so that all logic tests run
against the `core/` module only.

## Running tests

```bash
# Run all tests
mvn test

# Run tests for core only (same thing — no tests exist in other modules)
mvn test -pl core

# Run a specific test class
mvn test -pl core -Dtest=AnvilCostUtilsTest

# Run a specific test method
mvn test -pl core -Dtest=AnvilCostUtilsTest#givenSharpnessV_defaultConfig_whenCountCost_thenCorrectTotal
```

The Surefire plugin configuration in `core/pom.xml` applies the following JVM flags automatically when running tests:

```
-XX:+EnableDynamicAgentLoading -Djdk.attach.allowAttachSelf
```

These are required for Mockito's byte-buddy agent on JDK 21 and do not need to be set manually.

## Test base class

`DisenchantmentTestBase` is the abstract base that all test classes extend. It sets up the full plugin environment
before each test and tears it down after:

```java
@BeforeEach
void setUpBase() {
    server = MockBukkit.mock();       // start MockBukkit server
    mockNMS = new MockNMS();          // create stub NMS

    // Mock the static NMSMapper so plugin startup uses MockNMS instead of real NMS
    nmsMapperMock = Mockito.mockStatic(NMSMapper.class);
    nmsMapperMock.when(NMSMapper::setup).thenReturn(mockNMS);
    nmsMapperMock.when(NMSMapper::hasNMS).thenReturn(true);

    plugin = MockBukkit.load(Disenchantment.class); // triggers onEnable()
}

@AfterEach
void tearDownBase() {
    SupportedPluginManager.deactivateAllPlugins();
    mockNMS.clearSupportedPlugins();
    nmsMapperMock.close();
    MockBukkit.unmock();

    // Reset static globals so they do not leak between tests
    Disenchantment.nms = null;
    Disenchantment.config = null;
    Disenchantment.localeConfig = null;
    Disenchantment.enabled = true;

    clearEnchantmentStateCaches();
}
```

Helper methods available in subclasses:

| Method                                         | Description                                                      |
|------------------------------------------------|------------------------------------------------------------------|
| `setConfig(key, value)`                        | Sets a config value and clears enchantment state caches          |
| `setDisenchantEnchantmentStates(List<String>)` | Convenience wrapper for disenchantment enchantment states config |
| `setShatterEnchantmentStates(List<String>)`    | Convenience wrapper for shatterment enchantment states config    |
| `enchantment(String key)`                      | Looks up a vanilla enchantment by its key (e.g. `"sharpness"`)   |
| `mockEnchant(String key, int level)`           | Creates an `IPluginEnchantment` wrapping a vanilla enchantment   |
| `world()`                                      | Returns the default test world (adds one if not present)         |
| `activateMockPlugin(MockPluginAdapter)`        | Registers an adapter in `MockNMS` and activates it               |
| `buildAnvilEvent(player, slot0, slot1)`        | Constructs a mocked `PrepareAnvilEvent` with the given items     |

## MockNMS

`MockNMS` (`core/src/test/java/.../nms/MockNMS.java`) is a test-only implementation of the `NMS` interface that uses
only the Bukkit API — no NMS internals, no reflection:

- `canItemBeEnchanted` — returns `true` for any item that is not a book or air.
- `getRegisteredEnchantments` — iterates `Registry.ENCHANTMENT`.
- `getMaterials` — returns `Arrays.asList(Material.values())`.
- `getSupportedPlugins` — returns the list of plugins explicitly added via `addSupportedPlugin(plugin)`.
- `getRepairCost` / `setAnvilRepairCost` — delegates to `AnvilInventory.getRepairCost()` / `setRepairCost()`.
- `setItemRepairCost` — no-op (no NBT in tests, intentional).
- `setTexture` — returns the `HeadBuilder` unchanged.

## MockPluginAdapter

`MockPluginAdapter` (`core/src/test/java/.../plugins/MockPluginAdapter.java`) is a minimal `ISupportedPlugin` stub:

- Accepts a name in its constructor.
- Returns empty enchantment lists by default.
- Tracks whether `activate()` was called (`wasActivateCalled()`).
- Can be configured with custom enchantment lists for tests that exercise adapter integration.

## What is tested

### `AnvilCostUtilsTest`

Verifies the XP cost formula `base + sum(level_n * multiply * position_multiplier)`:

- Zero enchantments returns the base cost (default `10`).
- A single enchantment (Sharpness V) with default config produces the expected total.
- Two enchantments are sorted descending by level before computing multipliers.
- Per-enchantment cost overrides take precedence over the formula.
- The formula respects custom `base` and `multiply` config values.
- Disabling `disenchantment.anvil.repair.cost` returns `0` regardless of enchantments.

### `DisenchantEventTest` / `ShatterEventTest`

Verifies `PrepareAnvilEvent` handling end-to-end:

- Correct item combinations produce an `ENCHANTED_BOOK` result.
- Invalid combinations (no book, wrong slots, disabled world) produce no result.
- Enchantment state overrides (`DISABLED`, `FORCED`) are respected.
- The result book contains exactly the expected enchantments.

### `DisenchantEventAdapterTest` / `ShatterEventAdapterTest`

Verifies that activated third-party adapters are consulted during enchantment collection:

- When a `MockPluginAdapter` is active and returns enchantments, those enchantments appear in the result.
- When no adapter is active, vanilla enchantments are used.

### `EventUtilsDisenchantTest` / `EventUtilsShatterTest`

Unit-tests the eligibility logic in `EventUtils` that determines which enchantments transfer:

- Disabled enchantments are filtered out.
- Forced enchantments are always included regardless of `disabled-materials` or `disabled-worlds`.
- Enchantments on a book in slot 0 with a blank book in slot 1 are detected correctly.
- Edge cases: item with no enchantments, item with all enchantments disabled.

### `EventUtilsAdapterDisenchantTest` / `EventUtilsAdapterShatterTest`

Adapter-specific variants of the above, using `MockPluginAdapter` to inject custom enchantments.

### `SupportedPluginManagerTest`

Verifies adapter lifecycle:

- An adapter for a loaded plugin is activated; the `activate()` lifecycle hook is called.
- An unknown plugin name is not activated.
- `deactivateAllPlugins()` clears the active list.
- `getSupportedPluginByName` returns the correct adapter or `null`.

## Writing new tests

### Follow the naming convention

Test class: `<ClassUnderTest>Test.java`
Test method: `given<State>_when<Action>_then<ExpectedResult>()`

Examples: `givenSharpnessV_defaultConfig_whenCountCost_thenCorrectTotal`,
`givenDisabledWorld_whenPrepareAnvil_thenNoResult`.

### Extend DisenchantmentTestBase

```java
class MyFeatureTest extends DisenchantmentTestBase {

    @Test
    void givenSomeCondition_whenAction_thenResult() {
        // Arrange — use helpers from DisenchantmentTestBase
        setConfig("disenchantment.enabled", true);
        PlayerMock player = server.addPlayer();

        ItemStack enchantedSword = new ItemStack(Material.DIAMOND_SWORD);
        enchantedSword.addEnchantment(enchantment("sharpness"), 5);

        PrepareAnvilEvent event = buildAnvilEvent(player,
                enchantedSword,
                new ItemStack(Material.BOOK));

        // Act
        DisenchantEvent.onEvent(event);

        // Assert
        assertNotNull(event.getResult());
        assertEquals(Material.ENCHANTED_BOOK, event.getResult().getType());
    }
}
```

### Mocking NMS behaviour

`MockNMS` is already injected by `DisenchantmentTestBase`. If a specific test needs to override a method:

```java
mockNMS = Mockito.spy(new MockNMS());
Mockito.when(mockNMS.canItemBeEnchanted(Mockito.any())).thenReturn(false);
// Re-set Disenchantment.nms after overriding
Disenchantment.nms = mockNMS;
```

### Testing with third-party adapters

```java
MockPluginAdapter myAdapter = new MockPluginAdapter("MyPlugin");
// Configure what enchantments the adapter returns for a given item
// (see MockPluginAdapter for the API)
activateMockPlugin(myAdapter);
// From here, SupportedPluginManager.getAllActivatedPlugins() will include myAdapter
```

### Testing config variations

```java
setConfig("disenchantment.anvil.repair.base", 20.0);
setConfig("disenchantment.anvil.repair.multiply", 0.5);
// Then exercise the feature — config is read fresh by Config.*
```

## Integration testing on a local server

Unit tests cover logic in isolation. For integration testing:

1. Build the plugin: `mvn clean package`.
2. Copy `target/Disenchantment-<version>.jar` to your test server's `plugins/` folder.
3. Start the server and verify the startup log shows the correct NMS module and activated adapters.
4. Use the in-game anvil to test disenchanting and shattering manually.
5. For economy testing: install a Vault-compatible economy plugin (e.g. EssentialsX + VaultUnlocked).
6. For third-party enchantment testing: install the relevant enchantment plugin alongside Disenchantment.

Enable `logging.level: DEBUG` in `config.yml` to get full traces in the console for every anvil interaction:

```yaml
logging:
  level: DEBUG
```

This logs `[DEBUG][DISENCHANT]`, `[DEBUG][SHATTER]`, `[DEBUG][ADAPTER]`, and `[DEBUG][ECONOMY]` entries for every
relevant event.
