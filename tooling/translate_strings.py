import json, os, re, sys, urllib.request, pathlib

ROOT = pathlib.Path(r"C:\Users\navg\DailyApps\NeoFit")
SRC = (ROOT / "app/src/main/res/values/strings.xml").read_text(encoding="utf-8")
TOKEN = os.environ["AZ_TOKEN"]
ENDPOINT = "https://ai-contosohub530569751908.cognitiveservices.azure.com"
URL = f"{ENDPOINT}/openai/deployments/gpt-4o-mini/chat/completions?api-version=2024-10-21"

# The 22 scheduled languages of India (Eighth Schedule).
# folder uses Android BCP-47 'b+' qualifier for 3-letter codes.
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

def translate(name, script):
    sysmsg = (
        f"You are a professional mobile-app localizer. Translate an Android strings.xml into "
        f"natural, native {name} ({script} script). Rules: translate ONLY the human-readable text "
        f"inside each <string> element; keep every name=\"...\" attribute exactly the same; keep "
        f"app_name value as 'Neo Fit'; keep emojis and units (kg, kcal, g, cm, BMI) sensible; "
        f"preserve XML escaping (&apos; for apostrophes, &amp; for ampersands). Render any Hinglish "
        f"Latin words in the native script. Output ONLY the full strings.xml starting with the XML "
        f"declaration and nothing else."
    )
    body = json.dumps({
        "messages": [
            {"role": "system", "content": sysmsg},
            {"role": "user", "content": SRC},
        ],
        "temperature": 0.3,
        "max_tokens": 4000,
    }).encode("utf-8")
    req = urllib.request.Request(URL, data=body, method="POST")
    req.add_header("Authorization", f"Bearer {TOKEN}")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=120) as r:
        data = json.loads(r.read().decode("utf-8"))
    return data["choices"][0]["message"]["content"]

force = "--force" in sys.argv
for code, folder, name, script in LANGS:
    d = ROOT / "app/src/main/res" / folder
    target = d / "strings.xml"
    if target.exists() and not force:
        print(f"skip {folder} (exists)", flush=True); continue
    print(f"Translating -> {name} ({folder}) ...", flush=True)
    try:
        xml = translate(name, script).strip()
        xml = re.sub(r"^```xml\s*", "", xml)
        xml = re.sub(r"```$", "", xml).strip()
        if "<resources" not in xml:
            print(f"  ! no <resources> for {code}; skipping", flush=True); continue
        d.mkdir(parents=True, exist_ok=True)
        target.write_text(xml, encoding="utf-8")
        n = len(re.findall(r"<string ", xml))
        print(f"  wrote {folder}/strings.xml ({n} strings)", flush=True)
    except Exception as e:
        print(f"  ERR {code}: {e}", flush=True)
print("done")
