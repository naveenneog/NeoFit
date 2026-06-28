package com.neofit.feature.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.domain.coach.CoachService
import com.neofit.domain.model.CoachContext
import com.neofit.domain.model.CoachMessage
import com.neofit.domain.model.CoachRole
import com.neofit.domain.model.DashboardSummary
import com.neofit.domain.repository.MealLogRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.domain.usecase.GetDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val coachService: CoachService,
    private val getDashboard: GetDashboardUseCase,
    private val userRepository: UserRepository,
    private val mealLogRepository: MealLogRepository,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<CoachMessage>>(emptyList())
    val messages: StateFlow<List<CoachMessage>> = _messages.asStateFlow()

    private val _responding = MutableStateFlow(false)
    val responding: StateFlow<Boolean> = _responding.asStateFlow()

    val engineLabel: String = coachService.engineLabel

    /** Quick-start prompts. English keywords drive the heuristic engine. */
    val suggestions: List<String> = listOf(
        "How am I doing today?",
        "What can I eat for 300 kcal?",
        "Veg high-protein dinner",
        "Plan a quick workout",
    )

    private var latestDashboard: DashboardSummary? = null

    init {
        getDashboard()
            .onEach { latestDashboard = it }
            .launchIn(viewModelScope)
    }

    fun send(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || _responding.value) return

        _messages.update { it + CoachMessage(CoachRole.USER, prompt) + CoachMessage(CoachRole.COACH, "") }
        val coachIndex = _messages.value.lastIndex
        _responding.value = true

        viewModelScope.launch {
            try {
                val dashboard = latestDashboard ?: getDashboard().first()
                val context = CoachContext(
                    profile = userRepository.getProfile(),
                    dashboard = dashboard,
                    recentMeals = mealLogRepository.recentMeals(8),
                )
                coachService.reply(prompt, context).collect { partial ->
                    _messages.update { msgs ->
                        msgs.toMutableList().also { it[coachIndex] = CoachMessage(CoachRole.COACH, partial) }
                    }
                }
            } catch (_: Exception) {
                _messages.update { msgs ->
                    msgs.toMutableList().also {
                        it[coachIndex] = CoachMessage(CoachRole.COACH, "Sorry, I couldn't answer that just now. Try again?")
                    }
                }
            } finally {
                _responding.value = false
            }
        }
    }
}
