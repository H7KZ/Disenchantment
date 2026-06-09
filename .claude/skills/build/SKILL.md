---
name: build
description: Build the Disenchantment plugin with Maven. Use after making code changes to verify compilation.
disable-model-invocation: true
allowed-tools: Bash(mvn *)
---

Run the Maven build and report the result.

Execute:

```bash
mvn clean package 2>&1
```

Then report:

1. Whether the build **succeeded** or **failed**
2. If failed: the exact error messages and which module failed
3. If succeeded: the output JAR location (`target/Disenchantment-*.jar`)

Do not attempt to fix errors automatically — report them and wait for instructions.
