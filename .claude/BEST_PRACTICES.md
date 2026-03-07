# Claude Code Best Practices — Disenchantment

A practical guide for using Claude Code effectively in this Minecraft plugin project.

## Table of Contents

1. [Core Principles](#core-principles)
2. [CLAUDE.md — Project Memory](#claudemd--project-memory)
3. [Skills — Reusable Workflows](#skills--reusable-workflows)
4. [Subagents — Isolated Task Workers](#subagents--isolated-task-workers)
5. [Agent Teams — Parallel NMS Work](#agent-teams--parallel-nms-work)
6. [Hooks — Automation](#hooks--automation)
7. [MCP Servers — External Tools](#mcp-servers--external-tools)
8. [Output Styles](#output-styles)
9. [Headless Mode — CI/CD](#headless-mode--cicd)
10. [Plugins](#plugins)
11. [Project-Specific Patterns](#project-specific-patterns)

---

## Core Principles

- **Scope requests tightly.** Ask for one thing at a time (e.g., "add 1.21.12 to MinecraftVersion.java" not "add full
  1.21.12 support").
- **Let Claude read before writing.** Never ask Claude to modify a file it hasn't read. Ask it to read the relevant NMS
  class first.
- **Prefer existing patterns.** This codebase has strong conventions (tabs, builder pattern, EventExecutor). Always tell
  Claude to match the existing code style.
- **Verify before committing.** Run `mvn clean package` after every set of changes. Use the `/build` skill for this.

---

## CLAUDE.md — Project Memory

`CLAUDE.md` (`.claude/CLAUDE.md`) is loaded automatically at every session start. It is the single most important
configuration file.

### What belongs in CLAUDE.md

- Architecture overview (already present)
- Build commands
- Module structure
- Code conventions
- Common gotchas (e.g., "v1_21_R1+ uses Bukkit API directly, no NBT package")

### What does NOT belong in CLAUDE.md

- Session-specific tasks (use the conversation instead)
- Things that change per feature (use skills)
- Large reference docs (link to them or use skills with supporting files)

### Best practice

Keep CLAUDE.md under 200 lines — Claude loads the first 200 lines automatically. Currently it is well within this limit.
Put deeper reference material in `.claude/skills/` supporting files instead.

---

## Skills — Reusable Workflows

Skills live in `.claude/skills/<name>/SKILL.md`. They are invoked with `/name` or loaded automatically by Claude when
relevant.

### Installed project skills

| Skill         | Command        | Purpose                                                   |
|---------------|----------------|-----------------------------------------------------------|
| `build`       | `/build`       | Run `mvn clean package` and report results                |
| `new-version` | `/new-version` | Step-by-step checklist for adding a new Minecraft version |

### Writing effective skills

**Use `disable-model-invocation: true`** for anything with side effects (build, deploy, commit). You don't want Claude
triggering a build automatically.

**Use `$ARGUMENTS`** to make skills dynamic:

```yaml
---
name: fix-issue
description: Fix a specific GitHub issue by number
disable-model-invocation: true
---
Fix GitHub issue #$ARGUMENTS following the code conventions in CLAUDE.md.
```

**Keep SKILL.md focused.** Put detailed checklists or reference content in supporting files within the skill directory.

### Skill invocation control

| Frontmatter                      | Who can invoke             |
|----------------------------------|----------------------------|
| (default)                        | Both you and Claude        |
| `disable-model-invocation: true` | Only you, via `/name`      |
| `user-invocable: false`          | Only Claude, automatically |

---

## Subagents — Isolated Task Workers

Subagents run in their own context window. Use them to:

- Keep verbose output (test runs, build logs) out of your main conversation
- Enforce read-only exploration with limited tool access
- Perform parallel research on independent questions

### Installed project agents

| Agent             | Purpose                                                                   |
|-------------------|---------------------------------------------------------------------------|
| `code-reviewer`   | Reviews Java/Spigot code for correctness, style, and security. Read-only. |
| `nms-implementer` | Guides implementation of a new NMS module. Has full tool access.          |

### When to use subagents vs. main conversation

Use **subagents** when:

- Running Maven builds (lots of output)
- Exploring multiple NMS modules simultaneously
- You want a focused, domain-specific reviewer

Use the **main conversation** when:

- Making iterative edits (planning -> implementation -> testing)
- You need frequent back-and-forth clarification
- The task is a quick, targeted change

### Invoking an agent explicitly

```
Use the code-reviewer agent to review my recent changes to v1_21_R5
```

### Read-only exploration

The built-in `Explore` agent (Haiku model) is ideal for codebase searches:

```
Use a subagent to explore how enchantment registry access differs between v1_18_R1 and v1_21_R1
```

---

## Agent Teams — Parallel NMS Work

Agent teams (experimental) are best for this project when you need to implement the same feature across multiple NMS
modules simultaneously.

Enable in settings:

```json
{
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1"
  }
}
```

### Ideal use case: cross-version feature implementation

```
Create an agent team with 5 teammates to add [feature] to all NMS modules in parallel.
Each teammate owns one module: v1_18_R1, v1_20_R4, v1_21_R1, v1_21_R4, v1_21_R5.
Require plan approval before any teammate makes changes.
```

### Warnings

- Each teammate has its own context window — token costs scale linearly with team size.
- Teammates do NOT inherit conversation history. Include module-specific context in the spawn prompt.
- Avoid having two teammates edit the same file. Assign each to a separate NMS module directory.
- Always clean up: `Clean up the team` when done.

---

## Hooks — Automation

Hooks run shell commands automatically at lifecycle points. Configure in `.claude/settings.json` (project-wide) or
`~/.claude/settings.json` (personal).

### Recommended hooks for this project

#### 1. Notify when Claude needs input (personal)

Add to `~/.claude/settings.json`:

```json
{
  "hooks": {
    "Notification": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "powershell.exe -Command \"[System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms'); [System.Windows.Forms.MessageBox]::Show('Claude Code needs your attention', 'Claude Code')\""
          }
        ]
      }
    ]
  }
}
```

#### 2. Re-inject context after compaction

Maven builds, NMS exploration, and long debugging sessions can fill the context window. After compaction, Claude loses
context about which NMS module you were working in.

Add to `.claude/settings.json`:

```json
{
  "hooks": {
    "SessionStart": [
      {
        "matcher": "compact",
        "hooks": [
          {
            "type": "command",
            "command": "echo 'Reminder: Use tabs for indentation. v1_21_R1+ uses Bukkit API directly (no NBT package). Run mvn clean package to verify changes. Static import Disenchantment.plugin, .logger, .nms, .config.'"
          }
        ]
      }
    ]
  }
}
```

#### 3. Block edits to critical files

Prevent accidental modification of the shading configuration:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "jq -r '.tool_input.file_path // empty' | grep -q 'dist/pom.xml' && echo 'dist/pom.xml is sensitive — confirm this edit is intentional' >&2 && exit 2 || exit 0"
          }
        ]
      }
    ]
  }
}
```

### Hook debugging

Toggle verbose mode with `Ctrl+O` to see hook output inline. Use `/hooks` to manage hooks interactively.

---

## MCP Servers — External Tools

MCP servers give Claude access to external tools and data. Add at project scope (shared) or local scope (personal).

### Useful MCP servers for this project

#### GitHub (PR reviews, issue tracking)

```bash
claude mcp add --transport http github https://api.githubcopilot.com/mcp/
```

Authenticate with `/mcp` after adding. Enables:

- "Review PR #123 for correctness across all NMS modules"
- "Create an issue for the bug we just found in v1_21_R5"

#### Sentry (error monitoring, if used)

```bash
claude mcp add --transport http sentry https://mcp.sentry.dev/mcp
```

### Scope guidance

| Scope                   | Use for                                      |
|-------------------------|----------------------------------------------|
| `local` (default)       | Personal tokens, experimental servers        |
| `project` (`.mcp.json`) | Team-shared servers checked into git         |
| `user`                  | Personal tools available across all projects |

### Project-scoped MCP config

Add to `.mcp.json` for team-shared servers (safe to commit):

```json
{
  "mcpServers": {
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/"
    }
  }
}
```

---

## Output Styles

Change how Claude responds during a session with `/output-style`.

| Style         | When to use                             |
|---------------|-----------------------------------------|
| `default`     | Normal coding work                      |
| `explanatory` | Learning how an NMS version's API works |
| `learning`    | Hands-on practice implementing adapters |

Set with:

```
/output-style explanatory
```

Changes are saved to `.claude/settings.local.json` at the project level.

---

## Headless Mode — CI/CD

Run Claude non-interactively with the `-p` flag. Useful for automated code reviews or checks in CI pipelines.

### Automated build verification after changes

```bash
claude -p "Run mvn clean package and report whether the build succeeded or failed. Output only the result." \
  --allowedTools "Bash(mvn *)" \
  --output-format json | jq -r '.result'
```

### Review a PR for NMS correctness

```bash
gh pr diff "$PR_NUMBER" | claude -p \
  --append-system-prompt "You are a Spigot/Paper NMS expert. Review for correctness, focusing on version-specific API usage and the ISupportedPlugin adapter pattern." \
  --output-format json | jq -r '.result'
```

### Continue a multi-step workflow

```bash
SESSION=$(claude -p "Analyze the v1_21_R5 module for missing enchantment handlers" --output-format json | jq -r '.session_id')
claude -p "Now compare with v1_21_R4 and list the differences" --resume "$SESSION"
```

---

## Plugins

Plugins bundle skills, agents, hooks, and MCP servers for distribution. Currently enabled:

- `huggingface-skills@claude-plugins-official`

### Installing plugins

```
/plugin install commit-commands@anthropics-claude-code
```

This adds convenient `/commit-commands:commit` for git commits.

### Useful official plugins

- **`commit-commands`** — git commit, push, PR creation workflows
- **`pr-review-toolkit`** — specialized PR review agents

Browse available plugins:

```
/plugin
```

Then go to the **Discover** tab.

### Creating a project plugin

If you want to share skills/agents with contributors, convert `.claude/` configurations into a plugin:

1. Create `my-plugin/.claude-plugin/plugin.json`
2. Copy `skills/` and `agents/` into the plugin root
3. Distribute via a GitHub repository marketplace

---

## Project-Specific Patterns

### Adding support for a new Minecraft version

Use the `/new-version` skill:

```
/new-version 1.21.12
```

This walks through all required steps from CONTRIBUTING.md in a structured checklist.

### Debugging enchantment handling across versions

Spawn parallel subagents for comparison:

```
Research how getEnchantmentLevel is implemented in v1_18_R1 and v1_21_R5 using separate subagents, then summarize the key differences
```

### Reviewing a third-party plugin adapter

```
Use the code-reviewer agent to review the Zenchantments adapter in v1_21_R5 for correctness and completeness compared to the v1_21_R4 implementation
```

### Fixing a bug that affects multiple NMS modules

1. Use the main conversation to identify the root cause in one module
2. Spawn parallel subagents to apply the same fix across all affected modules:
   ```
   Apply the fix from v1_21_R5/plugins/ZenchantmentsAdapter.java to all other NMS modules with Zenchantments support, using separate subagents for v1_21_R4 and v1_21_R1
   ```

### Working with the build system

Always verify with Maven after changes:

```
/build
```

For diagnosing Maven issues, use Bash directly:

```bash
mvn clean package -pl core,v1_21_R5,dist --also-make 2>&1 | tail -50
```

### Keeping context focused during long sessions

- Use `/compact` when context gets large (typically after exploring 3+ NMS modules)
- Use subagents for exploration tasks to keep the main conversation clean
- Use the `SessionStart compact` hook (see Hooks section) to re-inject key reminders after compaction
