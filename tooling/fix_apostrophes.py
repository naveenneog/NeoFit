import re, pathlib

RES = pathlib.Path(r"C:\Users\navg\DailyApps\NeoFit\app/src/main/res")

# Escape any straight apostrophe (U+0027) not already backslash-escaped.
pat = re.compile(r"(?<!\\)'")

total = 0
for d in sorted(RES.glob("values*")):
    f = d / "strings.xml"
    if not f.exists():
        continue
    txt = f.read_text(encoding="utf-8")
    fixed = pat.sub(r"\\'", txt)
    if fixed != txt:
        n = len(pat.findall(txt))
        f.write_text(fixed, encoding="utf-8")
        print(f"{d.name}: escaped {n} apostrophe(s)")
        total += n
print(f"total escaped: {total}")
