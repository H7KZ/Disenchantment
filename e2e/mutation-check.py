#!/usr/bin/env python3
"""Mutation control checks for the e2e suite (docs/engineering/E2E.md, "Mutation control checks").

Applies ONE production-code mutation at a time to the working tree, builds the plugin jar, runs only the e2e classes
that should notice it, records RED (a targeted test failed: good) or GREEN (the suite missed it: a coverage hole),
then reverts the file with `git checkout`. Run it only in a scratch worktree, never commit a mutation.

usage: python e2e/mutation-check.py <id|all> <evidence-dir> [cell]   (from the repo root; env as in E2E.md)
env:   MVN (path to mvn), JAVA_HOME (JDK 21), PROOFSTONE_REPO, PROOFSTONE_LOCK (dockerlock.sh), MAVEN_REPO_LOCAL
       (optional), BASH_EXE (Git Bash on Windows), PROOFSTONE_ACCEPT_EULA=true (you accept https://aka.ms/MinecraftEULA)
"""
import os, subprocess, sys, shutil, glob, re

SRC = "core/src/main/java/com/jankominek/disenchantment/"
G = SRC + "events/AnvilEventGuards.java"
O = SRC + "events/AnvilOperations.java"
REMOVE = "finalFirstItem = EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants());"

# id: (description, file, old, new, count, it.test classes)
MUTATIONS = {
    "M1": ("disenchant XP cost drops the base (level*multiply only)", SRC + "utils/AnvilCostUtils.java",
           "totalCost = Config.Disenchantment.Anvil.Repair.getBaseCost();", "totalCost = 0;", 1,
           "CostFormulaE2E,DisenchantE2E"),
    "M2": ("disenchant leaves the enchantments on the source item", O,
           REMOVE, "if (!disenchant) " + REMOVE, 1, "DisenchantE2E"),
    "M3": ("shatter does not remove the split enchantment from the source book", O,
           REMOVE, "if (disenchant) " + REMOVE, 1, "ShatterE2E"),
    "M4": ("economy withdraw skipped (balance check kept, no money taken)", G,
           "EconomyUtils.withdraw(p, cost);",
           "new net.milkbowl.vault.economy.EconomyResponse(cost, 0, net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS, null);",
           1, "EconomyE2E"),
    "M5": ("PreDisenchantEvent cancel ignored", O,
           "if (preEvent.isCancelled()) return Outcome.of(Status.CANCELLED);",
           "if (false) return Outcome.of(Status.CANCELLED);", 1, "PreEventCancelE2E"),
    "M6": ("per-world disable ignored", G,
           "if (!disabledForWorld) return false;", "if (true) return false;", 1, "DisabledWorldE2E"),
    "M7": ("erase keeps the enchantments (no removal in the result)", SRC + "utils/EraseUtils.java",
           "result = enchantment.removeFromItem(result);", "/* mutated: nothing removed */", 1, "EraseModeE2E"),
    "M8": ("MARKED mode treats an unmarked anvil as special", G,
           "if (mode == SpecialAnvilMode.MARKED) return SpecialAnvilStore.isMarked(block);",
           "if (mode == SpecialAnvilMode.MARKED) return true;", 1, "SpecialAnvilE2E,SpecialAnvilPlacementE2E"),
    "M9": ("no money refund when writing the result fails", O,
           "AnvilEventGuards.refundEconomy(p, kind.economy(), payment.economyCost());",
           "/* mutated: no refund */", 1, "EconomyE2E,SkippedScenariosE2E"),
    "M10": ("stats: the disenchant counter is not incremented", SRC + "stats/StatsCache.java",
            "if (type == OperationType.DISENCHANT) totalDisenchants.increment();",
            "if (type == OperationType.DISENCHANT) { }", 1, "StatsE2E"),
    "M11": ("grindstone transfer copies instead of moving (source keeps the enchantments)", SRC + "events/GrindstoneOps.java",
            "result = enchantment.removeFromItem(result);", "/* mutated: nothing removed */", 1, "GrindstoneE2E"),
}


def sh(cmd, log, cwd=None, env=None):
    with open(log, "w", encoding="utf-8", errors="replace") as f:
        return subprocess.call(cmd, stdout=f, stderr=subprocess.STDOUT, cwd=cwd, env=env)


def run(mid, ev, cell):
    desc, path, old, new, count, classes = MUTATIONS[mid]
    text = open(path, encoding="utf-8").read()
    assert old in text, f"{mid}: pattern not found in {path}"
    open(path, "w", encoding="utf-8", newline="").write(text.replace(old, new, count))
    out = os.path.join(ev, mid)
    shutil.rmtree(out, ignore_errors=True)
    os.makedirs(out)
    try:
        env = dict(os.environ)
        mvn = env.get("MVN", "mvn")
        rc = sh([mvn + ".cmd" if os.name == "nt" else mvn, "-o", "-q", "-B", "clean", "package", "-DskipTests"], os.path.join(out, "build.log"), env=env)
        if rc != 0:
            return mid, "BUILD-FAIL", 0, 0
        shutil.rmtree("e2e/target/failsafe-reports", ignore_errors=True)
        shutil.rmtree("e2e/build/proofstone", ignore_errors=True)
        cmd = [env.get("BASH_EXE", "bash"), env["PROOFSTONE_LOCK"], mvn, "-B", "--no-transfer-progress"]
        if env.get("MAVEN_REPO_LOCAL"):
            cmd.append("-Dmaven.repo.local=" + env["MAVEN_REPO_LOCAL"])
        cmd += ["-f", "e2e/pom.xml", "verify", "-Dproofstone.repo=" + env["PROOFSTONE_REPO"],
                "-Dproofstone.cells=" + cell, "-Dit.test=" + classes, "-Dfailsafe.failIfNoSpecifiedTests=false"]
        rc = sh(cmd, os.path.join(out, "run.log"), env=env)
        xml = os.path.join(out, "xml")
        shutil.copytree("e2e/target/failsafe-reports", xml, dirs_exist_ok=True) if os.path.isdir("e2e/target/failsafe-reports") else None
        if os.path.isdir("e2e/build/proofstone"):
            shutil.copytree("e2e/build/proofstone", os.path.join(out, "bundle"), dirs_exist_ok=True)
        tests = fail = 0
        names = []
        for f in glob.glob(os.path.join(xml, "TEST-*.xml")):
            s = open(f, encoding="utf-8", errors="replace").read()
            m = re.search(r'<testsuite [^>]*', s)
            if m:
                tests += int(re.search(r'tests="(\d+)"', m.group(0)).group(1))
                fail += int(re.search(r'failures="(\d+)"', m.group(0)).group(1)) + int(re.search(r'errors="(\d+)"', m.group(0)).group(1))
            for tc in re.finditer(r'<testcase name="([^"]+)" classname="([^"]+)"[^>]*>\s*<(?:failure|error)', s):
                names.append(tc.group(2).split(".")[-1] + "." + tc.group(1))
        open(os.path.join(out, "failed-tests.txt"), "w").write("\n".join(names))
        verdict = "RED" if fail else ("GREEN" if tests and rc == 0 else "NO-RESULT")
        return mid, verdict, tests, fail
    finally:
        subprocess.call(["git", "checkout", "--", path])


if __name__ == "__main__":
    which, ev = sys.argv[1], sys.argv[2]
    cell = sys.argv[3] if len(sys.argv) > 3 else "paper@26.3"
    ids = list(MUTATIONS) if which == "all" else which.split(",")
    for mid in ids:
        r = run(mid, ev, cell)
        print(r, MUTATIONS[mid][0], flush=True)
        open(os.path.join(ev, "results.tsv"), "a").write("\t".join(map(str, r)) + "\t" + MUTATIONS[mid][0] + "\n")
