<!-- generated-by: gsd-doc-writer -->

# Developer Environment Setup

This guide walks through setting up a complete local development environment for Disenchantment.
See [CONTRIBUTING.md](CONTRIBUTING.md) for the code style and PR process, and [ARCHITECTURE.md](ARCHITECTURE.md) for a
codebase overview.

## Prerequisites

| Tool                                                           | Version | Notes                                                                                                                               |
|----------------------------------------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------|
| [JDK](https://adoptium.net/)                                   | 21+     | JDK 21 is required; Java 17 source compatibility is set by the parent POM for `core/` and `v1_18_R1/`, but the toolchain must be 21 |
| [Apache Maven](https://maven.apache.org/download.cgi)          | 3.8+    | Must be on `PATH`                                                                                                                   |
| [Spigot BuildTools](https://www.spigotmc.org/wiki/buildtools/) | latest  | Used once to install NMS JARs into your local Maven repository                                                                      |
| Git                                                            | any     | —                                                                                                                                   |

## Step 1 — Clone the repository

```bash
git clone https://github.com/H7KZ/Disenchantment.git
cd Disenchantment
```

## Step 2 — Install Spigot NMS artifacts via BuildTools

Disenchantment compiles against Spigot's internals (`remapped-mojang` classifier). These JARs are not on Maven Central;
you must install them locally using BuildTools.

Download `BuildTools.jar` from the [SpigotMC wiki](https://www.spigotmc.org/wiki/buildtools/) and run it for each of the
five required versions. You only need to do this once (or when a new NMS module is added).

```bash
# Run each of these from a dedicated BuildTools directory (not inside the Disenchantment repo)
java -jar BuildTools.jar --rev 1.18
java -jar BuildTools.jar --rev 1.20.5
java -jar BuildTools.jar --rev 1.21
java -jar BuildTools.jar --rev 1.21.5
java -jar BuildTools.jar --rev 1.21.8
```

Each command installs the corresponding Spigot NMS artifact into `~/.m2/repository/org/spigotmc/`. This takes several
minutes per version on first run (BuildTools compiles Spigot from source).

> **Note:** Versions 1.21.8, 1.21.9, 1.21.10, and 1.21.11 all share the `v1_21_R5` module, so installing 1.21.8 is
> sufficient for that entire range.

## Step 3 — Build the plugin

```bash
mvn clean package
```

The shaded output JAR is written to `target/Disenchantment-<version>.jar` (e.g. `target/Disenchantment-6.5.11.jar`).
This is the file you drop into a server's `plugins/` folder.

If you only need to compile (no JAR output) to check for errors:

```bash
mvn compile
```

To run the unit tests only:

```bash
mvn test
```

## Step 4 — IDE setup

**IntelliJ IDEA** is the recommended IDE. Open the project as a Maven project:

1. `File → Open` and select the root `pom.xml`.
2. IntelliJ will detect the multi-module structure automatically and import all modules.
3. Set the Project SDK to JDK 21 (`File → Project Structure → Project → SDK`).
4. Maven tool window (`View → Tool Windows → Maven`) — run `Reload All Maven Projects` if any modules show errors after
   the first import.

**Module compiler settings:** The parent POM sets source/target to Java 17 globally. The `v1_21_R1`, `v1_21_R4`, and
`v1_21_R5` modules override this to Java 21 in their own POMs. IntelliJ respects these per-module settings
automatically.

**Missing NMS classes:** If the IDE shows errors on `NMS_v*_R*.java` files complaining about missing Spigot internals,
verify that BuildTools has installed all five versions (Step 2). The JARs must be in `~/.m2` before IntelliJ can resolve
them.

## Step 5 — Set up a local test server

1. Download a Paper or Spigot JAR for the Minecraft version you are
   testing. [PaperMC downloads](https://papermc.io/downloads) or [SpigotMC](https://www.spigotmc.org/).
2. Create a directory for the test server and place the server JAR there.
3. Accept the EULA: create `eula.txt` containing `eula=true`.
4. Copy `target/Disenchantment-<version>.jar` into the server's `plugins/` folder.
5. Start the server with:

```bash
java -Xmx2G -jar paper-<version>.jar --nogui
```

6. After making changes, rebuild (`mvn clean package`), replace the plugin JAR in `plugins/`, and restart the server (or
   use a hot-reload plugin for quicker iteration).

**Testing multiple MC versions:** Maintain separate server directories per version. The same plugin JAR works across all
supported versions — NMS is loaded at runtime based on the detected server version.

## Common setup errors

### `Could not find artifact org.spigotmc:spigot:jar:remapped-mojang:1.21.8-R0.1-SNAPSHOT`

BuildTools has not been run for that version. Run `java -jar BuildTools.jar --rev <version>` from a BuildTools
directory. Make sure BuildTools completes without error (watch for Java version mismatches — BuildTools itself requires
JDK 21 for 1.21.x builds).

### `BUILD FAILURE: Source option 17 is no longer supported`

Your active JDK is too old. Disenchantment requires JDK 21. Check with `java -version` and point Maven to the correct
JDK via `JAVA_HOME` or the Maven toolchains configuration.

### `java.lang.UnsupportedClassVersionError` when loading the plugin

The JAR was compiled with a newer JDK than the server is running. Ensure your test server runs on Java 21.

### `NMS setup failed` in server console

The server version is not in `MinecraftVersion.java` and did not match the LATEST fallback (version below 1.18, or an
unusual version string). Check `MinecraftVersion.java` and, if needed, add an entry.
See [ADDING_NEW_VERSION.md](ADDING_NEW_VERSION.md).

### `Locales folder does not exist`

The plugin data folder was not populated. This usually means the plugin JAR was replaced while the server was running
and the data folder was cleaned. Stop the server, replace the JAR, and start again.

### Tests fail with `NullPointerException` in `Disenchantment.plugin`

Tests must use `MockBukkit` and the `DisenchantmentTestBase` setup. See [TESTING.md](TESTING.md).

## Environment variables and JVM flags

There are no required environment variables for development. The test suite passes `argLine` JVM flags automatically via
the Maven Surefire configuration in `core/pom.xml`:

```xml
<argLine>-XX:+EnableDynamicAgentLoading -Djdk.attach.allowAttachSelf</argLine>
```

These are needed for Mockito's byte-buddy agent on JDK 21 and are applied automatically when running `mvn test`.
