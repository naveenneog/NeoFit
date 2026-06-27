import json, os, re, sys, time, urllib.request, pathlib

ROOT = pathlib.Path(r"C:\Users\navg\DailyApps\NeoFit")
RES = ROOT / "app/src/main/res"
SRC = (RES / "values/strings.xml").read_text(encoding="utf-8")
TOKEN = os.environ["AZ_TOKEN"]
ENDPOINT = "https://ai-contosohub530569751908.cognitiveservices.azure.com"
URL = f"{ENDPOINT}/openai/deployments/gpt-4o-mini/chat/completions?api-version=2024-10-21"

LANGS = [
    ("as",  "values-as",     "Assamese",          "Assamese (Bengali-Assamese script)"),
    ("bn",  "values-bn",     "Bengali",           "Bengali"),
    ("brx", "values-b+brx",  "Bodo",              "Devanagari"),
    ("doi", "values-b+doi",  "Dogri",             "Devanagari"),
    ("gu",  "values-gu",     "Gujarati",          "Gujarati"),
    ("hi",  "values-hi",     "Hindi",             "Devanagari"),
    ("kn",  "values-kn",     "Kannada",           "Kannada"),
    ("ks",  "values-ks",     "Kashmiri",          "Perso-Arabic (Nastaliq)"),
    ("kok", "values-b+kok",  "Konkani",           "Devanagari"),
    ("mai", "values-b+mai",  "Maithili",          "Devanagari"),
    ("ml",  "values-ml",     "Malayalam",         "Malayalam"),
    ("mni", "values-b+mni",  "Manipuri (Meitei)", "Bengali script"),
    ("mr",  "values-mr",     "Marathi",           "Devanagari"),
    ("ne",  "values-ne",     "Nepali",            "Devanagari"),
    ("or",  "values-or",     "Odia",              "Odia"),
    ("pa",  "values-pa",     "Punjabi",           "Gurmukhi"),
    ("sa",  "values-sa",     "Sanskrit",          "Devanagari"),
    ("sat", "values-b+sat",  "Santali",           "Ol Chiki"),
    ("sd",  "values-sd",     "Sindhi",            "Perso-Arabic"),
    ("ta",  "values-ta",     "Tamil",             "Tamil"),
    ("te",  "values-te",     "Telugu",            "Telugu"),
    ("ur",  "values-ur",     "Urdu",              "Perso-Arabic (Nastaliq)"),
]


def default_keys_in_order():
    return re.findall(r'<string name="([^"]+)"', SRC)


def src_element(key):
    """Return the full <string ...>...</string> element for key from the default file."""
    m = re.search(r'(<string name="' + re.escape(key) + r'"[^>]*>.*?</string>)', SRC, re.DOTALL)
    return m.group(1) if m else None


def locale_keys(path):
    if not path.exists():
        return []
    return re.findall(r'<string name="([^"]+)"', path.read_text(encoding="utf-8"))


def translate_fragment(name, script, fragment):
    sysmsg = (
        f"You are a professional mobile-app localizer. Translate the human-readable text of these "
        f"Android <string> elements into natural, native {name} ({script} script). "
        f"STRICT RULES: keep every name=\"...\" attribute byte-for-byte identical; translate ONLY the "
        f"text between <string> and </string>; keep format placeholders like %1$s, %2$d, %% EXACTLY as-is; "
        f"keep emojis (✓, 🎉, etc.) and units (kg, kcal, g, cm, BMI) as-is; use the Android backslash "
        f"escape \\' for any apostrophe; render Latin loan-words in the native {script} script where natural. "
        f"Output ONLY the translated <string> elements, one per line, nothing else (no <resources>, no code fences)."
    )
    body = json.dumps({
        "messages": [
            {"role": "system", "content": sysmsg},
            {"role": "user", "content": fragment},
        ],
        "temperature": 0.2,
        "max_tokens": 1500,
    }).encode("utf-8")
    req = urllib.request.Request(URL, data=body, method="POST")
    req.add_header("Authorization", f"Bearer {TOKEN}")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=120) as r:
        data = json.loads(r.read().decode("utf-8"))
    return data["choices"][0]["message"]["content"]


def parse_strings(xml_text):
    out = {}
    for m in re.finditer(r'<string name="([^"]+)"[^>]*>(.*?)</string>', xml_text, re.DOTALL):
        out[m.group(1)] = m.group(0)
    return out


def main():
    dkeys = default_keys_in_order()
    summary = []
    for code, folder, name, script in LANGS:
        target = RES / folder / "strings.xml"
        existing = locale_keys(target)
        existing_set = set(existing)
        missing = [k for k in dkeys if k not in existing_set]
        if not missing:
            print(f"skip {folder} (complete)", flush=True)
            summary.append((folder, 0, 0))
            continue
        fragment = "\n".join(filter(None, (src_element(k) for k in missing)))
        print(f"Translating {len(missing)} -> {name} ({folder}) ...", flush=True)
        try:
            resp = translate_fragment(name, script, fragment).strip()
            resp = re.sub(r"^```[a-zA-Z]*\s*", "", resp)
            resp = re.sub(r"```$", "", resp).strip()
            translated = parse_strings(resp)
            # Keep only the keys we asked for, in default order
            to_add = [translated[k] for k in missing if k in translated]
            got = len(to_add)
            if got == 0:
                print(f"  ! no usable strings for {code}; skipping", flush=True)
                summary.append((folder, 0, len(missing)))
                continue
            txt = target.read_text(encoding="utf-8")
            block = "    " + "\n    ".join(to_add) + "\n"
            # Insert before the final </resources>
            idx = txt.rfind("</resources>")
            new_txt = txt[:idx].rstrip() + "\n" + block + "</resources>\n"
            target.write_text(new_txt, encoding="utf-8")
            print(f"  merged {got}/{len(missing)} into {folder}/strings.xml", flush=True)
            summary.append((folder, got, len(missing)))
        except Exception as e:
            print(f"  ERR {code}: {e}", flush=True)
            summary.append((folder, -1, len(missing)))
        time.sleep(0.5)

    print("\n=== SUMMARY ===")
    for folder, got, miss in summary:
        print(f"  {folder}: added {got} (missing was {miss})")
    print("done")


if __name__ == "__main__":
    main()
