import re, pathlib

ROOT = pathlib.Path(r"C:\Users\navg\DailyApps\NeoFit")
RES = ROOT / "app/src/main/res"

def keys(path):
    if not path.exists():
        return []
    txt = path.read_text(encoding="utf-8")
    return re.findall(r'<string name="([^"]+)"', txt)

default_keys = keys(RES / "values/strings.xml")
print(f"default: {len(default_keys)} keys")

locales = [d.name for d in RES.iterdir() if d.is_dir() and d.name.startswith("values-") and d.name != "values-night"]
locales.sort()

all_missing = {}
for loc in locales:
    lk = set(keys(RES / loc / "strings.xml"))
    missing = [k for k in default_keys if k not in lk]
    all_missing[loc] = missing

# Show the union of missing keys and per-locale counts
union = []
for loc in locales:
    for k in all_missing[loc]:
        if k not in union:
            union.append(k)

print("\nMissing-key counts per locale:")
for loc in locales:
    print(f"  {loc}: {len(all_missing[loc])} missing")

print(f"\nUnion of missing keys ({len(union)}):")
for k in union:
    print(f"  {k}")

# Check whether all locales miss the same set
same = all(set(all_missing[loc]) == set(union) for loc in locales)
print(f"\nAll locales miss the same set: {same}")
