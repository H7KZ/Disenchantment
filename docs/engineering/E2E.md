# Real-server e2e suite (Proofstone)

The `e2e/` directory is a standalone Maven project that runs the built plugin jar on real Paper and Folia servers through
[Proofstone](https://github.com/H7KZ/Proofstone), built from a pinned source checkout (see "Proofstone source build": the new APIs
`Agent.logLines`, `PlayerState.openWindow` and `Agent.bootLogLines` are not in the Central 0.1.0). It is **not** a
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
| `SpecialAnvilE2E` | `BLOCK_BELOW` gating (stone, lapis, stone again); `MARKED` refuses an unmarked anvil and the hint message says why; `/disenchantment give anvil`; the crafting recipe is registered by a player-issued reload (on Folia the region-thread path of commit e2970b6) and crafts the special anvil; after that reload `Agent.logLines` shows the `Disenchantment reloaded.` line and no WARN, exception or stack frame since the test started |
| `CostFormulaE2E` | XP formula `base + sum(round(level * multiplier))` against hand-computed literals for two (base, multiply) pairs and the defaults (13); `cost: false`; `max-cost`; per-enchantment override; shatter formula |
| `RestrictionsE2E` | per-material deny/allow lists; per-enchantment `DISABLE`, `KEEP`, `DELETE`; `disenchantment.enabled` and `shatterment.enabled` off |
| `EconomyE2E` | money charged once with the Post event `economyCost` (disenchant, shatter, grindstone extract); insufficient funds deliver nothing and charge nothing; economy off moves no money |
| `PreEventCancelE2E` | a third-party listener cancels `PreDisenchantEvent`, `PreShatterEvent`, `PreGrindstoneExtractEvent`: nothing delivered or charged, no Post event; the same window then completes |
| `BedrockMenuE2E` | paper@26.3 and folia@26.3: `/disenchantment anvil` with PREFIX detection opens the chest menu, asserted from the server side with `PlayerState.openWindow` (type `chest`, title, 27 slots, button items); paired controls: fallback off, and a name without a configured prefix, each leave no window open |
| `NmsModuleE2E` | every cell: the plugin's start-up line `NMS module: <class>`, read with `Agent.bootLogLines()`, names the module that cell's Minecraft version maps to (1.18.2 and 1.20.4 `NMS_v1_18_R1`, 1.20.6 `NMS_v1_20_R4`, 1.21.11 and 26.3 `NMS_v1_21_R5`) |
| `SpecialAnvilPlacementE2E` | paper@26.3, paper@1.21.11, folia@26.3: a special anvil really placed by the player (`placeBlock` against stone, `blockType` as oracle): `MARKED` mode works only on an anvil placed from the special item (paired with a plain anvil at the same spot, before and after); breaking a marked anvil in survival drops the special item (name and PDC byte), a plain anvil drops plain |
| `RefundOnWriteFailureE2E` | paper@26.3, folia@26.3: the refund when writing the result fails (commit 4352679). The hooks plugin makes the plugin's anvil write throw after the charge (see "Test support plugins"); the money is refunded, no Post event, the injection is proven by the hooks counters; control: with the failure off the same window charges once. The levels-and-items test (`aFailedWriteLeavesTheLevelsAndTheItemsAsTheyWere`) found a plugin bug, fixed by PR #88, and is enabled (see "Write-failure injection") |
| `SkippedScenariosE2E` | reported skips, each with its reason (see "Proofstone gaps") |

Cells: `paper@1.18.2`, `paper@1.20.4` (module `v1_18_R1`), `paper@1.20.6` (`v1_20_R4`), `paper@1.21.11` and `paper@26.3`
(`v1_21_R5`, 26.x through the `LATEST` fallback), `folia@26.3`. Proofstone's `versions.yml` has no Paper row for 1.21 to 1.21.4
(`v1_21_R1`) or 1.21.5 to 1.21.7 (`v1_21_R4`), so those two modules have no cell.

**Known cell limits.** The survival-break scenario of `SpecialAnvilPlacementE2E` (`breakingAMarkedAnvilDropsTheSpecialItem`) makes the break instant with the block-break-speed attribute: `minecraft:block_break_speed` from 1.21.2 (and 26.x), `minecraft:player.block_break_speed` on 1.20.5 to 1.21.1, none on 1.18.2 and 1.20.4. The test picks the id from `Cell.version()` (`Agent.command`'s reply is best effort and must not be asserted on) and, where there is none, is a reported JUnit skip (`Assumptions`), never a pass. The plugin's behaviour for breaking a marked anvil in survival is therefore covered only on cells that have the attribute; the creative-mode placement scenarios run on every cell the class is run on.

## Test support plugins

Two small plugins are built from `e2e/src/main` (Java 17, compiled against the Paper 1.18.2 API with no transitive libraries: an old
snakeyaml or adventure on the test classpath breaks the Proofstone runner) into `e2e/target/support-jars/*-vault.jar` and
`*-hooks.jar`:

- **`Vault` double** (`EconomyE2E`): a plugin named `Vault` that carries the VaultAPI classes and registers one in-memory economy.
  Balances live in its config at `balances.<lowercase player name>`, so a test seeds a balance with `patchConfig` and reads the
  server's balance with `config(...)`: money is a server oracle. It is a double, not the real Vault.
- **`DisenchantmentE2eHooks`** (`PreEventCancelE2E`): a listener written like an API consumer. It resolves the `Pre*Event` classes
  through Disenchantment's class loader and cancels one while `cancel.<EventName>` is true in its live config. It can also make the
  plugin's next result write fail while `failWrite.PreDisenchantEvent` is true (`RefundOnWriteFailureE2E`, see "Write-failure injection").
  Its `config.yml` holds the switches and the `observed.*` counters (a test zeroes the counters first).

Compile rules of the hooks plugin (Java 17, `e2e/src/main`): `release 17` against paper-api 1.18.2 only (no API newer than 1.18.2, no
Java 18+ library methods such as `List.getFirst`, no pattern-matching switch); adventure-api is `provided` compile-only and no library is
shaded (an old snakeyaml or adventure on the runtime classpath breaks the Proofstone runner); no compile dependency on Disenchantment or
on net.minecraft (event classes via Disenchantment's class loader, server internals only by reflection); no mapped or obfuscated names
(CraftBukkit's public `getInventory()`, fields found by type: the `NonNullList` by its simple name, the `java.util.List` by type), so the same
code runs on every cell; plugin.yml has `folia-supported: true`, and the injection runs on the thread that fired the event.

## Write-failure injection

Proofstone's ADR-0002 forbids the agent from injecting faults, and its spike found no event on the plugin's `setItem` or `setItemOnCursor`
writes, so there is no generic API (No-go). The supported workaround is the hooks plugin, written like an API consumer. From a
`PreDisenchantEvent` listener (fired before the charge and before the write) it swaps the `java.util.List` inside the anvil input
container's `NonNullList` for one whose next `set(...)` throws once and then restores the original list. The plugin writes slot 0 first, so
the write fails after the payment and the refund path runs. The final field is written with `setAccessible`; no Java 26 final-field
warning appeared on paper@26.3 or folia@26.3, and the Folia thread is the player's region thread for both the event and the write, so no
thread issue arose. The plugin reports the exception through its crash report (a SEVERE record plus a "Crash report saved to" line),
which Proofstone's log guard would read as an unexpected error; the hooks plugin drops exactly that pair from the plugin's logger and
counts it, and every test asserts the counts (armed 1, thrown 1, 1 report, 1 notice), so the expected failure is proven, not hidden.

**Finding (fixed by PR #88).** After the refund the plugin's click handler rethrew into `onEvent`, which reported and returned without
cancelling the click, so vanilla then completed the take: the player paid vanilla's XP (30 to 17), the sword was consumed and the book
landed on the cursor, with the money refunded. `aFailedWriteLeavesTheLevelsAndTheItemsAsTheyWere` asserts the strict outcome (levels
unchanged, sword and blank book intact, no book) and was red on both cells before the fix (`C:\pw\dis-ev-m9\explore2-paper*`,
`explore2-folia*`). PR #88 (roll back partial writes, cancel only on write failure) fixed it; the test was re-enabled with its assertions
unchanged (results under "Write-failure fix").

## Config isolation

Disenchantment keeps the config object it read at enable, and caches enchantment states, costs and chances. Proofstone's
`patchConfig` changes the live object and saves the file; its restore writes the file back but never reaches the plugin. `Cfg`
therefore (1) records the value at each path, (2) patches, (3) runs `/disenchantment reload` (which re-reads the file and clears
the caches), and on close patches the originals back and reloads again. Use it in try-with-resources so the revert runs on failure.
The recipe test in `SpecialAnvilE2E` issues the reload as a player operator on purpose: on Folia that runs on the player's region thread.

## Reading the client window

Never read the client cursor one-shot after a click: `Window.cursor()` is the last synced value, and `click` returns on the first window sync,
while the server sends the delivered item in a `SetCursorItem` packet just after it (seen on folia@26.3). Use `Window.awaitCursor(predicate)`, and
`awaitSlot` for slots, then assert on the returned view. An empty-cursor check after a refused click comes after the server-side oracle
(`player.state().cursor()`), then `awaitCursor(ItemView::isEmpty)`. No sleeps.

## Proofstone source build

The suite needs `Agent.logLines` (T-M11-01), `PlayerState.openWindow` (T-M11-02), `Agent.bootLogLines` (T-M11-03), `Client.placeBlock`/`breakBlock` and `Agent.blockType` (T-M11-04) `ItemView.pdc` (T-M11-05) `Window.drag` (T-M11-09, not used by a test yet, see Proofstone gaps item 2) and `Window.awaitCursor` (T-M11-11), which are in Proofstone `develop` but not in the
Central 0.1.0 (the source build publishes Proofstone's catalog version, `0.2.0` since the release bump; `proofstone.version` in `e2e/pom.xml` is kept equal to it). `proofstone.ref` in `e2e/pom.xml` and `PROOFSTONE_REF` in the workflow name the pinned
commit (`f5957cc`, the merge of PR #209; it also holds T-M11-01 to T-M11-11); the workflow fails the build job when they differ. One Gradle run in a
checkout of that commit produces both inputs:

```bash
cd <proofstone>   # checked out at proofstone.ref
./gradlew publishToMavenLocal :platforms:bukkit:agent:shadowJar --no-daemon
```

`publishToMavenLocal` installs `proofstone-api`, `-core`, `-protocol` and `-drivers-mcpl` into the local Maven repository, where the e2e
build finds them (no `<repository>` serves them); signing is skipped without a key. The shadow jar is the agent. CI does this once in the
`build` job and hands the Maven artefacts to the cell jobs as artifact `proofstone-m2`. A Central 0.1.0 left in `~/.m2` is not silently
used wrongly: the tests do not compile against it. Use `-Dmaven.repo.local=<dir>` on both the Gradle and the Maven command to keep the
source build apart from your normal `~/.m2`.

## Run it

You need Docker, JDK 21+ for Maven, and a Proofstone checkout at `proofstone.ref` built as above (it holds `versions.yml` and the agent jar). Read <https://aka.ms/MinecraftEULA>: you accept it per command with an
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

**Agent requirement.** `-Dproofstone.repo` must point to the Proofstone checkout at `proofstone.ref` (it contains the event-probe
class-loader fix, PR #190, which an older agent turns into a log-guard failure on paper@1.18.2, and the APIs above), built as in
"Proofstone source build". The suite reads
`<proofstone>/platforms/bukkit/agent/build/libs/agent-all.jar`. A control run is `-Dproofstone.controlRun=true`.

Known gap: a click the plugin refuses changes nothing, so the driver gets no sync and `Window.click` throws `server did not
answer ...`. `Fixture.refusedClick` accepts that one message for clicks that must be refused and the tests then check the
server oracle (no book, no XP charge, no Post event). Every other driver error is rethrown.

## CI

`.github/workflows/e2e.yml` runs the suite on a hosted `ubuntu-latest` runner (Docker and JDK 21 are there). It runs on `pull_request`
(paths `e2e/**`, `core/**`, `v1_*/**`, `dist/**`, `pom.xml`, the workflow), nightly (03:17 UTC, default branch only) and `workflow_dispatch`.
Jobs: `preflight` (EULA check) -> `build` (plugin jar via the same Spigot BuildTools cache as `ci.yml`, plus the Proofstone agent jar) ->
`e2e` (one matrix cell each: paper@26.3, paper@1.21.11, paper@1.20.6, paper@1.20.4, paper@1.18.2, folia@26.3; `fail-fast: false`).
Permissions are `contents: read`; third-party actions are pinned by commit SHA; stale PR runs are cancelled by a concurrency group.
Proofstone is checked out at a pinned `develop` commit (`PROOFSTONE_REF` in the workflow, `f5957cc`, equal to `proofstone.ref` in the pom; tag
`v0.1.0` predates the class-loader fix and the new APIs, see "Proofstone source build"). The `build` job builds Proofstone from that checkout
(agent jar and `publishToMavenLocal`) and uploads artifacts `proofstone-agent` and `proofstone-m2`. Bump the pin deliberately, in both places.

**Set the EULA variable (once, by a repo admin).** Read <https://aka.ms/MinecraftEULA>. Setting the variable is your acceptance:
Settings > Secrets and variables > Actions > Variables > New repository variable, name `PROOFSTONE_ACCEPT_EULA`, value `true`
(CLI: `gh variable set PROOFSTONE_ACCEPT_EULA --body true`). The workflow maps it to an environment variable only; it is never written
to a file. Unset, the run ends green with a "e2e skipped" notice and runs nothing (fork PRs never see repository variables, so they skip too).

**No retries.** A red cell stays red. Reports are always uploaded: artifact `e2e-reports-<cell>` holds `failsafe-reports/` and
`surefire-reports/` XML plus `e2e/build/proofstone/` (repro bundles with the server logs of a red run); each job also writes a counts
table to the job summary.

**Flake report (manual).** Actions > e2e > Run workflow, set `repeat` (3 to 20) and `repeat-cell`. This replaces the matrix with one cell
run with `-Dproofstone.repeat=N`; Proofstone marks a test `FLAKY` (and the job fails) when verdicts differ across repeats. It reports, it
never retries a failure into green. Artifact: `e2e-flake-report-<cell>`.

**Run locally** (same steps as CI, see "Run it"): `mvn -B package -DskipTests`, build Proofstone (agent jar and publishToMavenLocal) in a checkout at the pinned
commit, then `PROOFSTONE_ACCEPT_EULA=true mvn -B -f e2e/pom.xml verify -Dproofstone.repo=<proofstone> -Dproofstone.cells=paper@26.3`.

**Verification status.** The workflow was linted (actionlint 1.7.7, clean) but a workflow can only be proven on GitHub: the first hosted run
(cold BuildTools cache, Gradle and Docker image pulls on the runner) is the real check.

## Recorded results

- **Control run** (`-Dproofstone.controlRun=true`, paper@26.3, no plugin installed): all 6 cell tests fail with `no class named ...PostDisenchantEvent`/`PostShatterEvent`; none passes, so no test is VACUOUS. The 5 skips stay skips.
- **paper@1.18.2 is red, deterministically** (2 runs, same 4 of 6 tests): the first test per class that subscribes to a Disenchantment event fails the log guard on `WARN ProofstoneAgent: Loaded class ...PostDisenchantEvent from Disenchantment v6.5.12 which is not a depend or softdepend of this plugin`. Cause is in the Proofstone agent (`EventProbes` loads the class through the agent's own class loader), not in Disenchantment. Other cells are green (paper 1.20.4, 1.20.6, 1.21.11, 26.3 and folia 26.3). The test is not weakened; the cell stays red until the agent is fixed.
- `mvn test -pl core --offline`: 262 tests, 0 failures. The root reactor lists core, the `v*` modules and dist only, never `e2e/`.

### Re-run with the fixed Proofstone agent

The agent jar was rebuilt from Proofstone `origin/develop` at `5fdb96d` (includes PR #190, the event-probe class-loader fix), and the suite pointed at it with `-Dproofstone.repo=<that checkout>`. One invocation per cell, 11 tests each (6 cell tests, 5 reported skips): paper@1.18.2, paper@26.3 and paper@1.21.11 all show 0 failures and 0 errors. The paper@1.18.2 red above is therefore gone with that agent; it was the agent's, as diagnosed.

### Audit-branch acceptance (Proofstone develop a5e431f)

Agent rebuilt from develop `a5e431f`. 13 classes, 50 tests per cell (45 run, 5 reported skips), 0 failures, 0 errors: paper@26.3 (three separate invocations), paper@1.21.11, folia@26.3. `mvn test -pl core --offline`: 1199 tests, 0 failures.

### Proofstone APIs upgrade (Proofstone develop f97b953)

Agent and Maven artefacts built from `f97b953`. 15 classes, 49 tests per cell (46 run, 3 reported skips). paper@26.3, three separate invocations: 0 failures, 0 errors each. folia@26.3, one invocation: **1 failure**, `PreEventCancelE2E.aCancelledPreDisenchantEventChangesNothing` (`DISAGREE: present in the inventory ... expected present after 10 s`: after the veto is lifted the second click delivers no book on Folia). It was red the same way at the previous pin (`2f64d38`) and is not hidden or retried; it is open. Not yet diagnosed whether it is the plugin, the hooks double or the agent. A clean build of the base branch also failed to compile the support plugins (paper-api needs adventure on the compile classpath); `e2e/pom.xml` now declares adventure-api as `provided`.

### Placement, PDC and inventory fix (Proofstone develop 4f2920d)

Agent and Maven artefacts built from `4f2920d`. 16 classes, 50 tests per cell (48 run, 2 reported skips: the drag and the write-failure scenario). Evidence under `C:\pw\dis-ev-apis2\`.

- paper@26.3, three separate invocations: 3 of 3 green. paper@1.21.11, one invocation: green.
- folia@26.3, three separate invocations: **1 of 3 green**. Red: `RestrictionsE2E.aDeletedEnchantmentIsDestroyed` (run 1) and `DisenchantE2E.tooLittleExperienceDeliversNothingUntilThePlayerCanPay` (run 3), both `DISAGREE: present in the inventory of item matching the predicate: expected present after 10 s`, the same signature as the earlier `PreEventCancelE2E` red. The new `SpecialAnvilPlacementE2E` and `NmsModuleE2E` passed on all three folia runs. So the client-inventory flake on Folia is not fixed by T-M11-06 (it showed in different tests each time, 2 of 3 invocations here, 1 of 1 at each of the two earlier pins); the cause is still open and nothing was retried or weakened.
- `SpecialAnvilPlacementE2E` and the PDC assertions ran on paper@26.3, paper@1.21.11 and folia@26.3 only. `ItemView.pdc` is verified by Proofstone on 1.18.2 and 26.3; on 1.20.4 and 1.20.6 the `SpecialAnvilE2E` PDC assertion is not yet proven, CI will show it.
- Test bugs found and fixed on the way (all in the new test, before the counted runs): a held sword instead of the anvil, a drop that drifted out of pickup range, a teleport onto the placement spot, and `item replace entity` doing nothing on Folia (the pickaxe is now given with `player.give`).

### Folia client-inventory fix (Proofstone develop 0466587)

Agent and Maven artefacts rebuilt from `0466587` (T-M11-07: a window packet that arrives after the client closed the window is handed to window 0 instead of being dropped, the cause of the Folia `DISAGREE ... present in the inventory` flake). 16 classes, 50 tests per cell (48 run, 2 reported skips). Evidence under `C:\pw\dis-ev-apis3\`.

- folia@26.3, five separate invocations: **5 of 5 green** (previous state at `4f2920d`: 1 of 3).
- paper@26.3, three separate invocations: 3 of 3 green. paper@1.21.11, one invocation: green.
- No retry inside any run. Five clean folia invocations lower the flake rate but do not prove it gone.

### Write-failure injection acceptance (Proofstone develop 0466587)

`RefundOnWriteFailureE2E`, 3 tests per cell (2 run, 1 reported skip), one invocation each, no retries: paper@26.3 3 of 3 green, folia@26.3 3 of 3 green. Mutation M9 on paper@26.3: RED, clean code green. Evidence under `C:\pw\dis-ev-m9\`.

### Proofstone pin bump to 11df8e0 (Window.drag) and the grindstone-drag decision

Agent and Maven artefacts rebuilt from Proofstone `11df8e0` (PR #208). The grindstone drag scenario stays a reported skip, with the reason
updated (see "Proofstone gaps" item 2): the drag fallback is not registered on any Paper/Folia cell, so no honest test or mutation exists.
16 classes, 52 tests per cell (50 run, 2 reported skips). Evidence under `C:\pw\dis-ev-drag\`.

- paper@26.3, three separate invocations: **3 of 3 green**.
- folia@26.3, three separate invocations: **2 of 3 green**. Run 2 is red: `EraseModeE2E.eraseDestroysTheEnchantmentsAndSpendsOneCatalyst`,
  `EraseModeE2E.java:54` (`anvil.cursor().is("diamond_sword")` was false right after `awaitSlot(0, isEmpty)`; the client cursor read raced the
  result take). Not retried, not weakened, open: not yet diagnosed whether it is the plugin, the test's cursor read or the agent.
  Bundle: `C:\pw\dis-ev-dragolia263-run2-bundle`.

### Client cursor await (Proofstone develop f5957cc, Window.awaitCursor)

Every client cursor read after a click now uses `awaitCursor` (see "Reading the client window"); pin bumped from `11df8e0` to `f5957cc` (PR #209), agent and Maven artefacts rebuilt. 16 classes, 52 tests per cell (50 run, 2 reported skips), one invocation per run, no retries, no sleeps, no timeout changes:

- folia@26.3, five separate invocations: **5 of 5 green** (the cell that raced before, see the EraseModeE2E failure on folia).
- paper@26.3, three separate invocations: **3 of 3 green**.
- paper@1.21.11, one invocation: **1 of 1 green**.

Evidence under `C:\pw\dis-ev-cursor\`.

### Write-failure fix (PR #88 merged, strict test re-enabled)

`origin/fix/audit-findings` (PR #88: roll back partial writes, cancel only on write failure) merged into this branch and
`aFailedWriteLeavesTheLevelsAndTheItemsAsTheyWere` re-enabled with its assertions unchanged. Pin `f5957cc`, plugin jar built from the merged tip
(contains `AnvilOperations$WriteFailedException`). 16 classes, 52 tests per cell (51 run, 1 reported skip: the drag). One invocation per run, no
retries, no sleeps, no timeout changes. Evidence under `C:\pw\dis-ev-final\`.

- paper@26.3, three separate invocations: **3 of 3 green** (the strict test passes, 0 failures, 0 errors).
- folia@26.3, five separate invocations: **0 of 5 green, inconclusive (host fault)**. Run 1: one failure, `UnsafeResultClickE2E.unsafeShatterResultClicksDeliverNothing`,
  only the log guard (851 records, all `FoliaWatchdogThread` "tick region has not responded" dumps stuck in vanilla `NbtIo.writeCompressed` disk writes); the strict
  test passed in that run. Run 2: all 52 skipped, `Docker is not available` (not a pass). Runs 3 to 5: server start timeouts
  (`Failed to download mojang_26.3.jar ... Connection timed out`), conformance-client join stalls and more watchdog dumps. The Docker/WSL host was degraded
  (disk I/O stalls, container network outage); runs 3 and 4 took over an hour against ~12 minutes normally. No `WriteFailedException`, snapshot or restore message in any red log.
- paper@1.21.11, one invocation: **red, same host fault** (13 errors: `curl exited 28` timeouts, `RefundOnWriteFailureE2E` errored at server start, not at an assertion).

The folia and 1.21.11 acceptance has to be repeated on a healthy host; it is not claimed green here.

### Old cells: break-speed attribute (pin f5957cc)

Quiet-host acceptance had paper@1.20.6, paper@1.20.4 and paper@1.18.2 red on `breakingAMarkedAnvilDropsTheSpecialItem` (`the anvil was broken`): `minecraft:block_break_speed` does not exist before 1.21.2. The test now uses `minecraft:player.block_break_speed` on 1.20.5 to 1.21.1 and skips with `Assumptions` on 1.18.2 and 1.20.4. Full suite, 52 tests, one invocation per run, no retries, evidence `C:\pw\dis-ev-oldcells\`:

| cell | runs | result | skips |
|---|---|---|---|
| paper@1.20.6 | 2 | 2 of 2 green (0 failures, 0 errors) | the grindstone drag only (the break scenario runs and passes) |
| paper@1.20.4 | 1 | green | the grindstone drag, the survival-break scenario (no attribute) |
| paper@1.18.2 | 1 | green | the grindstone drag, the survival-break scenario (no attribute) |

The earlier paper@1.18.2 agent event-class red (`Loaded class ... PostDisenchantEvent ... not a depend or softdepend`) did not recur. The earlier paper@1.20.6 `JoinTimeout` at `CONFIGURATION_STALLED` in `EconomyE2E` (Proofstone join-stall flake class) did not reproduce in 2 runs; it is open, nothing changed for it.

## Proofstone gaps

Scenarios the suite keeps as reasoned skips (`SkippedScenariosE2E`) or checks more weakly than it should, and the Proofstone
API each one needs. Nothing is faked in the meantime.

Closed by the pinned Proofstone (`11df8e0`): `Agent.logLines()` / `logLines(Pattern, Duration)` (T-M11-01, `SpecialAnvilE2E` checks the
reload for WARN, exceptions and stack frames), `PlayerState.openWindow` (T-M11-02, `BedrockMenuE2E`), `Agent.bootLogLines()` (T-M11-03,
`NmsModuleE2E`), `Client.placeBlock` / `breakBlock` with `Agent.blockType` (T-M11-04, `SpecialAnvilPlacementE2E`) and `ItemView.pdc`
(T-M11-05: the `disenchantment:special_anvil` byte is asserted in `SpecialAnvilE2E` and `SpecialAnvilPlacementE2E`). The chunk-PDC mark
itself is still read only by its effect (`MARKED` mode), because no API reads a block's or chunk's persistent data.

1. **Click-through on a server-side window**: `PlayerState.openWindow` is a read-only server view and `Window` is still the client's, so the
   Bedrock menu's confirm and cancel flow is not driven; only its server-side contents are asserted.
2. **A cell without `PrepareGrindstoneEvent`** (was: `Window.drag`, closed by T-M11-09 in the pinned Proofstone, `11df8e0`). The grindstone
   drag refresh (commit 5460349) is a tick fallback with its own `InventoryDragEvent` listener, registered only on servers without a
   `PrepareGrindstoneEvent`; every Paper/Folia cell has one (Paper ships its own since 1.16). On those cells a drag into the grindstone
   is handled by the normal prepare-event path, so a drag test would be green with or without the fallback and no mutation of the
   fallback could turn it red. It stays a reported skip (`SkippedScenariosE2E`); it needs a Spigot-below-1.19.4 cell. `GrindstoneListenerTest`
   covers the fallback in core.
3. **A fault-injection hook for write failures**: No-go in Proofstone (ADR-0002 keeps fault injection out of the agent; the spike found no
   event on the plugin's inventory writes). Closed on this side by the hooks-plugin injection (`RefundOnWriteFailureE2E`, see "Write-failure
   injection"): the refund and the strict levels-and-items outcome (plugin bug fixed by PR #88) are covered.
4. **Console command output**: `Agent.command` replies are best effort, so `/disenchantment reload` is checked by its effect.
   An `Agent.reload(plugin)` that runs the plugin's declared reload hook and reports failure would let `Cfg` assert the reload took
   effect instead of relying on the behaviour that follows.

## Mutation control checks

Proof that the suite goes red on bugs a real regression would resemble. `e2e/mutation-check.py` applies ONE production-code mutation to a
scratch worktree, runs `clean package`, runs only the e2e classes that should notice it on `paper@26.3` (one invocation per mutation, under
`dockerlock.sh`, Proofstone `0466587`), records the verdict and reverts the file with `git checkout`. RED = a targeted test failed
(good); GREEN = the suite missed the mutation (a coverage hole). No mutation is ever committed; this branch holds only the script and this
section. A first attempt without `clean` reused a stale shaded jar (the mutation was not in the jar, so M1 read GREEN wrongly); the script now
always cleans. Evidence (logs, failsafe XML, repro bundles, `results.tsv`): `C:\pw\dis-ev-mutation\<id>\`.

| Id | Mutation (file) | Classes run | Tests / failed | Result | Failing tests |
|---|---|---|---|---|---|
| M1 | disenchant XP cost drops the base: `totalCost = 0` (`AnvilCostUtils`) | CostFormulaE2E, DisenchantE2E | 9 / 4 | RED | CostFormulaE2E default13, base7/mult1, base4/mult.5, per-enchantment override |
| M2 | disenchant leaves the enchantments on the source (`AnvilOperations` removal skipped) | DisenchantE2E | 2 / 2 | RED | disenchantMovesEnchants..., tooLittleExperience... |
| M3 | shatter does not remove the split enchantment from the source book (same line) | ShatterE2E | 1 / 1 | RED | shatterSplitsOneEnchantmentOffTheBook |
| M4 | economy withdraw skipped, balance check kept (`AnvilEventGuards.processEconomyCost`, shared by all three operations) | EconomyE2E | 5 / 4 | RED | disenchant, shatter, grindstone-extract charges, insufficientFunds |
| M5 | `PreDisenchantEvent` cancel ignored (`AnvilOperations.settleBook`) | PreEventCancelE2E | 3 / 1 | RED | aCancelledPreDisenchantEventChangesNothing (shatter and grindstone tests stay green, correctly) |
| M6 | per-world disable ignored (`AnvilEventGuards.isWorldBlocked`) | DisabledWorldE2E | 1 / 1 | RED | aDisabledWorldGivesTheVanillaResult |
| M7 | erase keeps the enchantments (`EraseUtils.buildResult`) | EraseModeE2E | 4 / 2 | RED | eraseDestroys..., eraseCurses... |
| M8 | `MARKED` mode treats an unmarked anvil as special (`AnvilEventGuards.isSpecialBlock`) | SpecialAnvilE2E, SpecialAnvilPlacementE2E | 6 / 2 | RED | markedModeRefusesAnUnmarkedAnvil..., markedModeWorksOnlyOnAnAnvilPlacedFromTheSpecialItem |
| M9 | no money refund when writing the result fails (`AnvilOperations.refund`) | RefundOnWriteFailureE2E | 3 / 1 (1 skipped, then) | RED | aFailedWriteRefundsTheMoney (balance 75.0, expected 100.0); the control and the clean code stay green. Was GREEN (hole) before the hooks-plugin injection |
| M10 | stats: disenchant counter not incremented (`StatsCache.record`) | StatsE2E | 2 / 1 | RED | anAnvilDisenchantDoesNotTouchTheGrindstoneCounters |
| M11 | grindstone transfer copies instead of moves (`GrindstoneOps.stripFromSource`) | GrindstoneE2E | 6 / 4 | RED | extract strips item, transfer moves, unsafe clicks, too little XP |

### GREEN holes

None left for M1 to M11 (M9 closed by `RefundOnWriteFailureE2E`, results above; evidence `C:\pw\dis-ev-m9\mutation\M9`). The old M9 note is kept for history:

- **M9, refund on a failed write.** Known gap 3 under "Proofstone gaps": `SkippedScenariosE2E` skips it, so the mutation is invisible. Not
  cheap to close with an e2e test today: it needs a fault-injection hook (for example `Agent.failNextInventoryWrite(player)`) or a test plugin
  that makes the item write throw on demand. Proposed test once the hook exists: with economy on and a balance of 100, force the write of
  the disenchant result to fail, click, then assert the server balance is back to 100, the player's levels are unchanged and no
  `PostDisenchantEvent` fired. `DisenchantClickListener`-level unit coverage of the refund in `core` is the interim guard.
- Observation, not a hole: M10 is caught by one test only (the one that reads the counters after an anvil disenchant), and M5 only by the
  disenchant test by design. A dedicated "an anvil disenchant moves the Disenchants counter by one" assertion would make M10 fail in a test
  named for it.

### Result-slot race (hosted paper@1.20.6)

The first hosted run (45191523643) had paper@1.20.6 red on `SpecialAnvilPlacementE2E.markedModeWorksOnlyOnAnAnvilPlacedFromTheSpecialItem`
("special anvil placed in MARKED mode" expected true). The driver transcript shows the window closed before the result packet for slot 2
arrived: the test read the slot right after the second move was confirmed. The plugin was fine. An expected book is now awaited
(`awaitSlot`); an absent one is still read at once, since nothing can be awaited for it (it can only falsely pass). Three unfixed local
runs of paper@1.20.6 were green (52 tests, 0 failures, 1 skip), so the race needs host load. Fixed code, paper@1.20.6 and paper@26.3, one
invocation: 103 tests, 0 failures, 0 errors, 1 reported skip. Evidence `C:\pw\dis-ev-1206\`.

### Cells paper@1.21.4 and paper@1.21.7 (pin 7a684e9)

Both are added to every `@Matrix`, to the `NmsModuleE2E` map (1.21.4 `NMS_v1_21_R1`, 1.21.7 `NMS_v1_21_R4`, the two modules that had no cell) and to the workflow cell lists.
Local evidence, 3 separate invocations each, 52 tests, 1 reported skip: paper@1.21.4 3 of 3 clean; paper@1.21.7 2 of 3 clean, the third a
`CONFIGURATION_STALLED` join stall in `RestrictionsE2E` (harness join-stall class, kept as a finding, not retried). Proofstone keeps both rows
`experimental` (ADR-0015 needs a per-row conformance run). Evidence under `C:\pw\dis-ev-rows\`. Mutation check, all 11 mutations: RED (`C:\pw\dis-ev-mut\`).

Run it (scratch worktree only):

```bash
BASH_EXE="C:/Program Files/Git/usr/bin/bash.exe" MVN=<mvn> JAVA_HOME=<jdk21> PROOFSTONE_REPO=<proofstone> \
PROOFSTONE_LOCK=<proofstone>/scripts/dockerlock.sh PROOFSTONE_ACCEPT_EULA=true python e2e/mutation-check.py all <evidence-dir>
```
