# Real-server e2e suite (Proofstone)

The `e2e/` directory is a standalone Maven project that runs the built plugin jar on real Paper and Folia servers through
[Proofstone](https://github.com/H7KZ/Proofstone) (`io.github.h7kz.proofstone:*:0.1.0`, from Maven Central). It is **not** a
module of the root reactor, so `mvn package`, `mvn test -pl core --offline` and every other existing flow never build or run it.

## What it covers

Every "nothing happens" assertion has a paired control in the same test (the same layout delivers when the switch is on, or the
same window completes once the veto or shortage is lifted), and every test uses its own player (unique name, `reset()`), so no
state leaks between tests. Config patches go through `Cfg` (see "Config isolation").

| Class | Scenarios |
|---|---|
| `DisenchantE2E` | disenchant moves the enchantments to a book and charges the `PostDisenchantEvent` cost; too little XP delivers nothing until the player can pay |
| `ShatterE2E` | a multi-enchantment book splits one enchantment onto a new book |
| `UnsafeResultClickE2E` | shift, number-key, off-hand, drop and double clicks on the anvil result slot are cancelled (disenchant and shatter); one safe click then yields exactly one book |
| `DisabledWorldE2E` | a disabled world gives the vanilla result (own class and server) |
| `GrindstoneE2E` | extract (book gets the enchantments, item stripped, book consumed, XP = Post event cost); transfer (enchantments moved, never copied); both off by default (paired controls); unsafe result clicks cancelled with no duplicated item; too little XP |
| `StatsE2E` | `/disenchantment stats` as an operator: an extract moves only "Grind extract", a transfer only "Grind transfer" (and the per-player grindstone counter), an anvil disenchant moves neither (commit fa978b1) |
| `EraseModeE2E` | erase destroys the enchantments and spends one catalyst; off by default; `consume-catalyst: false`; `erase-curses` |
| `SpecialAnvilE2E` | `BLOCK_BELOW` gating (stone, lapis, stone again); `MARKED` refuses an unmarked anvil and the hint message says why; `/disenchantment give anvil`; the crafting recipe is registered by a player-issued reload (on Folia the region-thread path of commit e2970b6) and crafts the special anvil |
| `CostFormulaE2E` | XP formula `base + sum(round(level * multiplier))` against hand-computed literals for two (base, multiply) pairs and the defaults (13); `cost: false`; `max-cost`; per-enchantment override; shatter formula |
| `RestrictionsE2E` | per-material deny/allow lists; per-enchantment `DISABLE`, `KEEP`, `DELETE`; `disenchantment.enabled` and `shatterment.enabled` off |
| `EconomyE2E` | money charged once with the Post event `economyCost` (disenchant, shatter, grindstone extract); insufficient funds deliver nothing and charge nothing; economy off moves no money |
| `PreEventCancelE2E` | a third-party listener cancels `PreDisenchantEvent`, `PreShatterEvent`, `PreGrindstoneExtractEvent`: nothing delivered or charged, no Post event; the same window then completes |
| `SkippedScenariosE2E` | reported skips, each with its reason (see "Proofstone gaps") |

Cells: `paper@1.18.2`, `paper@1.20.4` (module `v1_18_R1`), `paper@1.20.6` (`v1_20_R4`), `paper@1.21.11` and `paper@26.3`
(`v1_21_R5`, 26.x through the `LATEST` fallback), `folia@26.3`. Proofstone's `versions.yml` has no Paper row for 1.21 to 1.21.4
(`v1_21_R1`) or 1.21.5 to 1.21.7 (`v1_21_R4`), so those two modules have no cell.

## Test support plugins

Two small plugins are built from `e2e/src/main` (Java 17, compiled against the Paper 1.18.2 API with no transitive libraries: an old
snakeyaml or adventure on the test classpath breaks the Proofstone runner) into `e2e/target/support-jars/*-vault.jar` and
`*-hooks.jar`:

- **`Vault` double** (`EconomyE2E`): a plugin named `Vault` that carries the VaultAPI classes and registers one in-memory economy.
  Balances live in its config at `balances.<lowercase player name>`, so a test seeds a balance with `patchConfig` and reads the
  server's balance with `config(...)`: money is a server oracle. It is a double, not the real Vault.
- **`DisenchantmentE2eHooks`** (`PreEventCancelE2E`): a listener written like an API consumer. It resolves the `Pre*Event` classes
  through Disenchantment's class loader and cancels one while `cancel.<EventName>` is true in its live config.

## Config isolation

Disenchantment keeps the config object it read at enable, and caches enchantment states, costs and chances. Proofstone's
`patchConfig` changes the live object and saves the file; its restore writes the file back but never reaches the plugin. `Cfg`
therefore (1) records the value at each path, (2) patches, (3) runs `/disenchantment reload` (which re-reads the file and clears
the caches), and on close patches the originals back and reloads again. Use it in try-with-resources so the revert runs on failure.
The recipe test in `SpecialAnvilE2E` issues the reload as a player operator on purpose: on Folia that runs on the player's region thread.

## Run it

You need Docker, JDK 21+ for Maven, and a Proofstone checkout (it holds `versions.yml` and the agent jar, which Gradle builds
there: `./gradlew :platforms:bukkit:agent:shadowJar`). Read <https://aka.ms/MinecraftEULA>: you accept it per command with an
environment variable, and it is never written to a file.

```bash
# 1. Build the plugin jar under test from the branch you are testing (writes target/Disenchantment-<version>.jar).
mvn -B package -DskipTests

# 2. Run the suite (one Docker run at a time; Proofstone's lock script serialises machines shared with other runs).
PROOFSTONE_ACCEPT_EULA=true bash <proofstone>/scripts/dockerlock.sh mvn -B -f e2e/pom.xml verify \
  -Dproofstone.repo=<proofstone> -Dproofstone.cells=paper@26.3
```

`-Dproofstone.cells=` picks cells (`paper@26.3,folia@26.3`); without it every cell of every class runs. Results are in
`e2e/target/failsafe-reports/*.xml`: a green run shows `failures="0" errors="0"` and the skips listed above. A red run
writes a repro bundle under `e2e/build/proofstone/`. Keep `disenchantment.version` in `e2e/pom.xml` equal to `<revision>` in the
root `pom.xml`.

**Agent requirement.** `-Dproofstone.repo` must point to a Proofstone checkout built from `develop` at or after `953db61` (it
contains the event-probe class-loader fix, PR #190; an older agent turns every Disenchantment event probe into a log-guard failure
on paper@1.18.2). Build its agent with `./gradlew :platforms:bukkit:agent:shadowJar`; the suite reads
`<proofstone>/platforms/bukkit/agent/build/libs/agent-all.jar`. A control run is `-Dproofstone.controlRun=true`.

Known gap: a click the plugin refuses changes nothing, so the driver gets no sync and `Window.click` throws `server did not
answer ...`. `Fixture.refusedClick` accepts that one message for clicks that must be refused and the tests then check the
server oracle (no book, no XP charge, no Post event). Every other driver error is rethrown.

## Recorded results

- **Control run** (`-Dproofstone.controlRun=true`, paper@26.3, no plugin installed): all 6 cell tests fail with `no class named ...PostDisenchantEvent`/`PostShatterEvent`; none passes, so no test is VACUOUS. The 5 skips stay skips.
- **paper@1.18.2 is red, deterministically** (2 runs, same 4 of 6 tests): the first test per class that subscribes to a Disenchantment event fails the log guard on `WARN ProofstoneAgent: Loaded class ...PostDisenchantEvent from Disenchantment v6.5.12 which is not a depend or softdepend of this plugin`. Cause is in the Proofstone agent (`EventProbes` loads the class through the agent's own class loader), not in Disenchantment. Other cells are green (paper 1.20.4, 1.20.6, 1.21.11, 26.3 and folia 26.3). The test is not weakened; the cell stays red until the agent is fixed.
- `mvn test -pl core --offline`: 262 tests, 0 failures. The root reactor lists core, the `v*` modules and dist only, never `e2e/`.

### Re-run with the fixed Proofstone agent

The agent jar was rebuilt from Proofstone `origin/develop` at `5fdb96d` (includes PR #190, the event-probe class-loader fix), and the suite pointed at it with `-Dproofstone.repo=<that checkout>`. One invocation per cell, 11 tests each (6 cell tests, 5 reported skips): paper@1.18.2, paper@26.3 and paper@1.21.11 all show 0 failures and 0 errors. The paper@1.18.2 red above is therefore gone with that agent; it was the agent's, as diagnosed.

## Proofstone gaps

Scenarios the suite keeps as reasoned skips (`SkippedScenariosE2E`) or checks more weakly than it should, and the Proofstone
API each one needs. Nothing is faked in the meantime.

1. **`Agent.logLines()`** (or `Agent.logLines(Pattern, Duration)`): the server log lines since the test started, as plain text,
   captured from the server logger. Needed for the per-cell NMS-module expectation (`NMS module: NMS_v1_21_R5` in the startup line)
   and for asserting that no stack trace or `WARN` was logged during an operation (for example the Folia reload path).
2. **`PlayerHandle.openWindow()`** (or `PlayerState.openWindow`): the server's view of the player's open container (type, title,
   slot items). Needed to assert the Bedrock-prefix chest menu from the server side (a simulated-Bedrock client sees the menu only
   as the client does, and that cell is paper@26.3 only).
3. **`Client.placeBlock(BlockPos against, Face face)`** and **`Client.breakBlock(BlockPos)`** (a real `BlockPlaceEvent` /
   `BlockBreakEvent` from the player's held item): needed for the special anvil chunk-PDC mark (`SpecialAnvilStore`), `MARKED`
   mode on a placed special anvil, and the drop swap on breaking.
4. **`Window.drag(List<Integer> slots, DragMode mode)`**: an inventory drag across slots (left, right, middle button), answered by
   the server. Needed for the grindstone drag refresh (commit 5460349). The tick fallback and its drag listener are registered
   only on servers without a `PrepareGrindstoneEvent`, and every Paper/Folia cell has one, so that path also needs a server
   without it; `GrindstoneListenerTest` covers it in core.
5. **`ItemView.pdc()` filled by the Bukkit agent**: the API field exists but the agent never reports persistent data (`pdc=null`
   for every item). The special anvil marker is therefore identified only by its configured name and lore, not by the
   `disenchantment:special_anvil` byte.
6. **A fault-injection hook for write failures** (for example `Agent.failNextInventoryWrite(player)`): needed for "refund when
   writing the result fails" (commit 4352679). No installable plugin throws on demand while an item is written.
7. **Console command output**: `Agent.command` replies are best effort, so `/disenchantment reload` is checked by its effect.
   An `Agent.reload(plugin)` that runs the plugin's declared reload hook and reports failure would let `Cfg` assert the reload took
   effect instead of relying on the behaviour that follows.
