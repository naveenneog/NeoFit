package com.neofit.domain.coach

import com.neofit.domain.model.CoachContext
import kotlinx.coroutines.flow.Flow

/**
 * The Coach is just another implementation behind an interface — like the food
 * recognition / image services. A heuristic [com.neofit.integration.ai.RuleBasedCoachService]
 * is always available; an on-device LLM can be swapped in when a model is downloaded.
 */
interface CoachService {

    /** Short label for the active engine, e.g. "On-device AI" or "Smart tips". */
    val engineLabel: String

    /** True when a richer (on-device LLM) engine is ready; false = heuristic fallback. */
    suspend fun isSmartEngineReady(): Boolean

    /**
     * Streams a short, India-aware coaching reply grounded in [context]. Implementations
     * emit progressively longer partial strings (cumulative) so the UI can render a
     * token-by-token typing effect.
     */
    fun reply(prompt: String, context: CoachContext): Flow<String>
}
