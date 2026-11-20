# Real-server e2e suite (Proofstone)

The `e2e/` directory is a standalone Maven project that runs the built plugin jar on real Paper and Folia servers through
[Proofstone](https://github.com/H7KZ/Proofstone) (`io.github.h7kz.proofstone:*:0.1.0`, from Maven Central). It is **not** a
module of the root reactor, so `mvn package`, `mvn test -pl core --offline` and every other existing flow never build or run it.

## What it covers

| Class | Scenarios |
|---|---|
| `DisenchantE2E` | disenchant moves the enchantments to a book and charges the `PostDisenchantEvent` cost; too little XP delivers nothing until the player can pay |
| `DisabledWorldE2E` | a disabled world gives the vanilla result (own class and server: Proofstone's config restore does not reach Disenchantment's cached config, so a patch would leak into later tests) |
| `ShatterE2E` | a multi-enchantment book splits one enchantment onto a new book |
| `UnsafeResultClickE2E` | shift, number-key, off-hand, drop and double clicks on the result slot are cancelled (disenchant and shatter); one safe click then yields exactly one book |
| `SkippedScenariosE2E` | reported skips, each with its reason: grindstone, erase mode, special anvils, Bedrock-prefix chest menu (features not on `master`), and the per-cell NMS-module expectation (the module is only in the startup log line, which Proofstone 0.1.0 cannot read) |

Cells: `paper@1.18.2`, `paper@1.20.4` (module `v1_18_R1`), `paper@1.20.6` (`v1_20_R4`), `paper@1.21.11` and `paper@26.3`
(`v1_21_R5`, 26.x through the `LATEST` fallback), `folia@26.3`. Proofstone's `versions.yml` has no Paper row for 1.21 to 1.21.4
(`v1_21_R1`) or 1.21.5 to 1.21.7 (`v1_21_R4`), so those two modules have no cell.

## Run it

You need Docker, JDK 21+ for Maven, and a Proofstone checkout (it holds `versions.yml` and the agent jar, which Gradle builds
there: `./gradlew :platforms:bukkit:agent:shadowJar`). Read <https://aka.ms/MinecraftEULA>: you accept it per command with an
environment variable, and it is never written to a file.

```bash
# 1. Build the plugin jar under test (writes target/Disenchantment-<version>.jar).
mvn -B package -DskipTests

# 2. Run the suite (one Docker run at a time; Proofstone's lock script serialises machines shared with other runs).
PROOFSTONE_ACCEPT_EULA=true bash <proofstone>/scripts/dockerlock.sh mvn -B -f e2e/pom.xml verify \
  -Dproofstone.repo=<proofstone> -Dproofstone.cells=paper@26.3
```

`-Dproofstone.cells=` picks cells (`paper@26.3,folia@26.3`); without it every cell of every class runs. Results are in
`e2e/target/failsafe-reports/*.xml`: a green run shows `failures="0" errors="0"` and the skips listed above. A red run
writes a repro bundle under `e2e/build/proofstone/`. Keep `disenchantment.version` in `e2e/pom.xml` equal to `<revision>` in the
root `pom.xml`.

Known gap: a click the plugin refuses changes nothing, so the driver gets no sync and `Window.click` throws `server did not
answer ...`. `Fixture.refusedClick` accepts that one message for clicks that must be refused and the tests then check the
server oracle (no book, no XP charge, no Post event). Every other driver error is rethrown.

## Recorded results

- **Control run** (`-Dproofstone.controlRun=true`, paper@26.3, no plugin installed): all 6 cell tests fail with `no class named ...PostDisenchantEvent`/`PostShatterEvent`; none passes, so no test is VACUOUS. The 5 skips stay skips.
- **paper@1.18.2 is red, deterministically** (2 runs, same 4 of 6 tests): the first test per class that subscribes to a Disenchantment event fails the log guard on `WARN ProofstoneAgent: Loaded class ...PostDisenchantEvent from Disenchantment v6.5.12 which is not a depend or softdepend of this plugin`. Cause is in the Proofstone agent (`EventProbes` loads the class through the agent's own class loader), not in Disenchantment. Other cells are green (paper 1.20.4, 1.20.6, 1.21.11, 26.3 and folia 26.3). The test is not weakened; the cell stays red until the agent is fixed.
- `mvn test -pl core --offline`: 262 tests, 0 failures. The root reactor lists core, the `v*` modules and dist only, never `e2e/`.
