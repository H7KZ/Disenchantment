"""Print a Markdown summary of the e2e JUnit XML reports (read-only; used for the job summary)."""
import glob
import sys
import xml.etree.ElementTree as ET

title = sys.argv[1] if len(sys.argv) > 1 else "e2e"
rows = []
totals = [0, 0, 0, 0]
for path in sorted(glob.glob("e2e/target/failsafe-reports/TEST-*.xml") + glob.glob("e2e/target/surefire-reports/TEST-*.xml")):
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        rows.append((path, "unparseable", "", "", ""))
        continue
    vals = [int(root.get(k, 0)) for k in ("tests", "failures", "errors", "skipped")]
    totals = [a + b for a, b in zip(totals, vals)]
    rows.append((root.get("name", path), *vals))

print(f"### {title}")
if not rows:
    print("No JUnit XML found (the run failed before any test, or was cancelled).")
else:
    print(f"tests {totals[0]}, failures {totals[1]}, errors {totals[2]}, skipped {totals[3]}")
    print()
    print("| class | tests | failures | errors | skipped |")
    print("|---|---|---|---|---|")
    for r in rows:
        print("| " + " | ".join(str(c) for c in r) + " |")
