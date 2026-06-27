import json, os, re, time, subprocess, urllib.request, pathlib

ROOT = pathlib.Path(r"C:\Users\navg\DailyApps\NeoFit")
LIB = (ROOT / "app/src/main/java/com/neofit/data/seed/ExerciseLibrary.kt").read_text(encoding="utf-8")
OUT = ROOT / "tooling" / "videos"; OUT.mkdir(parents=True, exist_ok=True)
LOG = ROOT / "tooling" / "video_gen.log"
ENDPOINT = "https://ai-contosohub530569751908.cognitiveservices.azure.com"
APIV = "preview"
MODEL = "sora-2"
SIZE = "720x1280"
SECONDS = "4"
CONCURRENCY = 4

def log(m):
    line = f"{time.strftime('%H:%M:%S')} {m}"
    with open(LOG, "a", encoding="utf-8") as f: f.write(line + "\n")
    print(line, flush=True)

_token = {"v": None, "t": 0}
def token():
    if time.time() - _token["t"] > 2400 or not _token["v"]:
        _token["v"] = subprocess.run(
            ["az", "account", "get-access-token", "--resource",
             "https://cognitiveservices.azure.com", "--query", "accessToken", "-o", "tsv"],
            capture_output=True, text=True, shell=True).stdout.strip()
        _token["t"] = time.time(); log("token refreshed")
    return _token["v"]

def req(method, url, body=None):
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(url, data=data, method=method)
    r.add_header("Authorization", f"Bearer {token()}")
    if body is not None: r.add_header("Content-Type", "application/json")
    return urllib.request.urlopen(r, timeout=120)

# Parse ex("id", "Name", ...) entries.
pairs = re.findall(r'ex\(\s*"([^"]+)"\s*,\s*"([^"]+)"', LIB)
seen = {}
for eid, name in pairs:
    if eid not in seen: seen[eid] = name
queue = [(eid, name) for eid, name in seen.items() if not (OUT / f"{eid}.mp4").exists()]
log(f"Exercises: {len(seen)} total, {len(queue)} to generate.")

def prompt_for(name):
    return (f"A fitness instructor performing {name}, full body visible in frame, side view, "
            f"plain light studio background, athletic wear, slow and clear correct form, neutral "
            f"soft lighting, clean exercise demonstration video, no text, no captions.")

def create(name):
    body = {"model": MODEL, "prompt": prompt_for(name), "seconds": SECONDS, "size": SIZE}
    with req("POST", f"{ENDPOINT}/openai/v1/videos?api-version={APIV}", body) as r:
        return json.loads(r.read())["id"]

def status(vid):
    with req("GET", f"{ENDPOINT}/openai/v1/videos/{vid}?api-version={APIV}") as r:
        return json.loads(r.read())

def download(vid, path):
    with req("GET", f"{ENDPOINT}/openai/v1/videos/{vid}/content?api-version={APIV}") as r:
        data = r.read()
    path.write_bytes(data); return len(data)

inflight = {}   # videoId -> (eid, name)
done = fail = 0
while queue or inflight:
    # top up in-flight jobs
    while queue and len(inflight) < CONCURRENCY:
        eid, name = queue[0]
        try:
            vid = create(name)
            inflight[vid] = (eid, name); queue.pop(0)
            log(f"submit {eid} -> {vid}")
            time.sleep(3)
        except urllib.error.HTTPError as e:
            if e.code == 429:
                log(f"429 on submit {eid}; backoff 45s"); time.sleep(45)
            elif e.code in (401, 403):
                _token["t"] = 0; time.sleep(2)
            else:
                log(f"submit ERR {eid}: {e.code} {e.read()[:200]}"); queue.pop(0); fail += 1
            break
        except Exception as e:
            log(f"submit EXC {eid}: {e}"); time.sleep(10); break
    # poll in-flight
    for vid in list(inflight.keys()):
        eid, name = inflight[vid]
        try:
            s = status(vid)
            st = s.get("status")
            if st == "completed":
                sz = download(vid, OUT / f"{eid}.mp4")
                done += 1; del inflight[vid]
                log(f"DONE {eid} ({sz} bytes) [{done} done, {len(queue)} queued]")
            elif st == "failed":
                fail += 1; del inflight[vid]
                log(f"FAILED {eid}: {s.get('error')}")
        except urllib.error.HTTPError as e:
            if e.code in (401, 403): _token["t"] = 0
            else: log(f"poll ERR {eid}: {e.code}")
        except Exception as e:
            log(f"poll EXC {eid}: {e}")
    time.sleep(10)

log(f"ALL DONE generated={done} failed={fail}")
