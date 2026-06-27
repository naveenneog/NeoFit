# On-device AI Coach — design notes & open-LLM options

Neo Fit's Azure resource has **`disableLocalAuth=true`**, so the app cannot call Azure GPT directly
with an API key. A *live cloud* coach would need a small backend proxy authenticating with
**Managed Identity / DefaultAzureCredential** (the app calls your endpoint, your endpoint calls
Azure). That's the cleanest production path.

This document covers the **alternative the user asked about: running a small language model (SLM)
fully on-device** — private, offline, no backend, no per-call cost — to power a fitness/nutrition
"Coach" inside Neo Fit.

---

## 1. Recommended on-device options

### A. Gemini Nano via Android AICore (zero model bundling)
- Google's on-device model, exposed through **AICore** + the **ML Kit GenAI** / AICore APIs.
- **No model file to ship** — the system provides it on supported devices (recent Pixel, some
  Samsung/Android 14+). Falls back gracefully where unavailable.
- Best when you want the smallest APK and don't want to manage weights.
- Caveat: device coverage is still limited; always provide a non-LLM fallback (Neo Fit already has
  the rule-based `RecommendationEngine`).

### B. MediaPipe / Google AI Edge — LLM Inference API (recommended, portable)
- Runs open SLMs on-device (CPU/GPU) from a single `.task` bundle via
  `com.google.mediapipe:tasks-genai`.
- Works across most Android devices (API 24+), good docs, streaming output.
- Proven small models that fit a phone (int4/int8 quantised):
  | Model | Params | License | Notes |
  |---|---|---|---|
  | **Gemma 3 1B (it)** | 1B | Gemma Terms | Great quality/size; first choice |
  | **Gemma 2 2B (it)** | 2B | Gemma Terms | Higher quality, larger |
  | **Phi-3.5-mini instruct** | 3.8B | MIT | Strong reasoning, heavier |
  | **Llama 3.2 1B / 3B instruct** | 1–3B | Llama 3.2 Community | Good, check license terms |
  | **Qwen2.5 0.5B / 1.5B instruct** | 0.5–1.5B | Apache-2.0 | Permissive, tiny |
  | **SmolLM2 360M / 1.7B** | 0.36–1.7B | Apache-2.0 | Smallest footprint |
  | **TinyLlama 1.1B** | 1.1B | Apache-2.0 | Classic tiny baseline |

### C. Other runtimes (if you outgrow MediaPipe)
- **ONNX Runtime GenAI** (`onnxruntime-genai`) — run Phi-3/Llama/Qwen ONNX int4 on-device.
- **llama.cpp (GGUF)** via a JNI/AAR wrapper — maximum model choice, more native plumbing.
- **ExecuTorch** (PyTorch Edge) — Meta's on-device runtime for Llama 3.2.
- **MLC LLM** — compiles models to Vulkan/Metal for GPU on-device inference.

### Picking one
- **Smallest APK / best device integration:** Gemini Nano (AICore).
- **Best portability + quality for ~1B:** MediaPipe LLM Inference + **Gemma 3 1B int4**
  (≈ 550 MB–1 GB weights → *download on first use*, don't bundle in the APK).
- **Most permissive license:** Qwen2.5-1.5B or SmolLM2 (Apache-2.0).

---

## 2. How it would plug into Neo Fit

The seam already exists — the Coach is just another implementation behind an interface, exactly like
the recognition/image services.

```kotlin
interface CoachService {
    /** Streams a short, India-aware coaching reply grounded in the user's data. */
    fun reply(prompt: String, context: CoachContext): Flow<String>
}

data class CoachContext(
    val profile: UserProfile,
    val today: DashboardSummary,
    val recentMeals: List<MealLog>,
)
```

- **`OnDeviceCoachService`** — wraps MediaPipe `LlmInference` (or AICore). Build a system prompt:
  *"You are a friendly Indian nutrition & fitness coach. Be concise, practical, non-medical. The
  user is {age}/{sex}, goal {goal}, region {region}, diet {diet}…"* then append today's numbers
  (calories eaten/burned/remaining, protein gap, steps, veg-day) and the question.
- **RAG grounding (optional, cheap):** retrieve the top matching `FoodItem`s / `ExercisePlan`s from
  the bundled knowledge base and inject them, so answers cite real dishes/plans and calories.
- **Fallback:** if no model is available, use the existing `RecommendationEngine` nudges so the
  Coach tab is always useful.
- **UX:** a "Coach" tab with suggested chips ("What can I eat for 400 kcal?", "Veg high-protein
  dinner", "Plan my week") + free text. Stream tokens for responsiveness. Always show the
  not-medical-advice disclaimer.

### Model delivery
- Don't bundle multi-hundred-MB weights in the APK — **download on first Coach use** (with consent),
  cache in app storage, and verify a checksum. Show a one-time "Download coach (~X MB)" prompt.
- Keep the rule-based engine as the always-available default.

### Licensing reminder
Gemma and Llama have their own model-use terms; Qwen2.5, SmolLM2, TinyLlama and Phi-3.5 are
permissively licensed (Apache-2.0 / MIT). Pick per your distribution needs and surface attributions.

---

## 3. Live cloud option (for reference)
If you prefer cloud quality, deploy a tiny **Azure Function / Container App** that holds a
`DefaultAzureCredential`, calls the Foundry chat endpoint
(`{endpoint}/openai/deployments/gpt-4o-mini/chat/completions`), and the app calls *that* function
(authenticated with the user's session). This keeps secrets server-side and respects
`disableLocalAuth=true`. The same `CoachService` interface swaps to an `AzureProxyCoachService`.
