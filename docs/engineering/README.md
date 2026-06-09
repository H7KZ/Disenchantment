<!-- generated-by: gsd-doc-writer -->
# Engineering Documentation

This directory contains technical documentation for contributors and developers working on the Disenchantment plugin codebase. If you are a server admin looking for configuration or usage help, see the root-level docs instead (`CONFIG.md`, `COMMANDS.md`, `PERMISSIONS.md`).

## Contents

| File | Description |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System design, module dependency diagram, NMS abstraction layer, event flow, and key patterns |
| [SETUP.md](SETUP.md) | Developer environment setup — JDK, Maven, Spigot BuildTools, IDE configuration |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guide — reporting issues, code style, PR process, adding custom enchantment adapters |
| [TESTING.md](TESTING.md) | Test structure, how to run tests, what is tested, and how to write new tests |
| [ADDING_NEW_VERSION.md](ADDING_NEW_VERSION.md) | Step-by-step guide for adding support for a new Minecraft version |

## Quick orientation

Disenchantment is a multi-module Maven project. The `core/` module contains all shared logic; five `v*` modules provide version-specific NMS implementations; `dist/` shades everything into the final JAR.

- **Start here for environment setup:** [SETUP.md](SETUP.md)
- **Start here to understand the codebase:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **Start here to submit a PR:** [CONTRIBUTING.md](CONTRIBUTING.md)
- **Adding a new MC version:** [ADDING_NEW_VERSION.md](ADDING_NEW_VERSION.md)

## Repository layout

```
Disenchantment/
├── core/           Shared logic (main class, config, events, listeners, GUIs, NMS interface)
├── v1_18_R1/       NMS for MC 1.18 – 1.20.4
├── v1_20_R4/       NMS for MC 1.20.5 – 1.20.6
├── v1_21_R1/       NMS for MC 1.21 – 1.21.4
├── v1_21_R4/       NMS for MC 1.21.5 – 1.21.7
├── v1_21_R5/       NMS for MC 1.21.8 – 1.21.11+ and 26.x.x (LATEST fallback)
├── dist/           Shading module — assembles the final plugin JAR
├── target/         Build output (Disenchantment-<version>.jar)
├── docs/
│   └── engineering/    ← you are here
├── COMMANDS.md     User-facing command reference
├── CONFIG.md       Configuration reference
├── CONTRIBUTING.md Root contributing guide (summary)
├── FAQ.md          Frequently asked questions
└── PERMISSIONS.md  Permission node reference
```
