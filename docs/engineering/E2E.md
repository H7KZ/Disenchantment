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

## Proofstone source build

The suite needs `Agent.logLines` (T-M11-01), `PlayerState.openWindow` (T-M11-02), `Agent.bootLogLines` (T-M11-03), `Client.placeBlock`/`breakBlock` and `Agent.blockType` (T-M11-04) and `ItemView.pdc` (T-M11-05), which are in Proofstone `develop` but not in the
Central 0.1.0 (the version string is still `0.1.0`). `proofstone.ref` in `e2e/pom.xml` and `PROOFSTONE_REF` in the workflow name the pinned
commit (`4f2920d`, the merge of PR #203; it also holds T-M11-01 to T-M11-04 and T-M11-06); the workflow fails the build job when they differ. One Gradle run in a
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
Proofstone is checked out at a pinned `develop` commit (`PROOFSTONE_REF` in the workflow, `4f2920d`, equal to `proofstone.ref` in the pom; tag
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

## Proofstone gaps

Scenarios the suite keeps as reasoned skips (`SkippedScenariosE2E`) or checks more weakly than it should, and the Proofstone
API each one needs. Nothing is faked in the meantime.

Closed by the pinned Proofstone (`4f2920d`): `Agent.logLines()` / `logLines(Pattern, Duration)` (T-M11-01, `SpecialAnvilE2E` checks the
reload for WARN, exceptions and stack frames), `PlayerState.openWindow` (T-M11-02, `BedrockMenuE2E`), `Agent.bootLogLines()` (T-M11-03,
`NmsModuleE2E`), `Client.placeBlock` / `breakBlock` with `Agent.blockType` (T-M11-04, `SpecialAnvilPlacementE2E`) and `ItemView.pdc`
(T-M11-05: the `disenchantment:special_anvil` byte is asserted in `SpecialAnvilE2E` and `SpecialAnvilPlacementE2E`). The chunk-PDC mark
itself is still read only by its effect (`MARKED` mode), because no API reads a block's or chunk's persistent data.

1. **Click-through on a server-side window**: `PlayerState.openWindow` is a read-only server view and `Window` is still the client's, so the
   Bedrock menu's confirm and cancel flow is not driven; only its server-side contents are asserted.
2. **`Window.drag(List<Integer> slots, DragMode mode)`**: an inventory drag across slots (left, right, middle button), answered by
   the server. Needed for the grindstone drag refresh (commit 5460349). The tick fallback and its drag listener are registered
   only on servers without a `PrepareGrindstoneEvent`, and every Paper/Folia cell has one, so that path also needs a server
   without it; `GrindstoneListenerTest` covers it in core.
3. **A fault-injection hook for write failures** (for example `Agent.failNextInventoryWrite(player)`): needed for "refund when
   writing the result fails" (commit 4352679). No installable plugin throws on demand while an item is written.
4. **Console command output**: `Agent.command` replies are best effort, so `/disenchantment reload` is checked by its effect.
   An `Agent.reload(plugin)` that runs the plugin's declared reload hook and reports failure would let `Cfg` assert the reload took
   effect instead of relying on the behaviour that follows.
