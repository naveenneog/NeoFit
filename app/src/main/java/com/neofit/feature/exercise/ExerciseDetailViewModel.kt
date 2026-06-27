package com.neofit.feature.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neofit.core.util.DateUtil
import com.neofit.domain.model.ExerciseItem
import com.neofit.domain.model.ExercisePlan
import com.neofit.domain.model.WorkoutSession
import com.neofit.domain.repository.ExerciseRepository
import com.neofit.domain.repository.ImageRepository
import com.neofit.domain.repository.UserRepository
import com.neofit.engine.CalorieMath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Phase { READY, EXERCISE, REST, DONE }

data class ExerciseRunnerState(
    val plan: ExercisePlan? = null,
    val started: Boolean = false,
    val currentIndex: Int = 0,
    val phase: Phase = Phase.READY,
    val secondsLeft: Int = 0,
    val running: Boolean = true,
    val completedIds: Set<String> = emptySet(),
    val caloriesBurned: Int = 0,
    val stepImageRef: String? = null,
    val generatingImage: Boolean = false,
    val voiceCue: String = "",
    val voiceNonce: Long = 0,
) {
    val currentItem: ExerciseItem?
        get() = plan?.items?.getOrNull(currentIndex)
}

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val planId: String = savedStateHandle.get<String>("planId").orEmpty()
    private val _state = MutableStateFlow(ExerciseRunnerState())
    val state: StateFlow<ExerciseRunnerState> = _state.asStateFlow()

    private var weightKg: Float = 65f
    private var sessionStart: Long = 0
    private var timerJob: Job? = null

    init {
        _state.value = _state.value.copy(plan = exerciseRepository.planById(planId))
        viewModelScope.launch {
            weightKg = userRepository.getProfile()?.currentWeightKg ?: 65f
        }
    }

    fun start() {
        val plan = _state.value.plan ?: return
        if (plan.items.isEmpty()) return
        sessionStart = DateUtil.nowMillis()
        _state.value = _state.value.copy(started = true)
        startItem(0)
    }

    fun togglePause() {
        _state.value = _state.value.copy(running = !_state.value.running)
    }

    /** For rep-based items the user taps Done; timed items auto-advance. */
    fun userDone() = completeAndRest()

    fun skip() = goNext()

    private fun startItem(index: Int) {
        val item = _state.value.plan?.items?.getOrNull(index) ?: return finish()
        _state.value = _state.value.copy(
            currentIndex = index,
            phase = Phase.EXERCISE,
            running = true,
            secondsLeft = item.durationSec ?: 0,
            voiceCue = item.voiceCue,
            voiceNonce = _state.value.voiceNonce + 1,
            stepImageRef = null,
        )
        fetchImage(item)
        if (item.durationSec != null) startCountdown { completeAndRest() }
    }

    private fun completeAndRest() {
        val item = _state.value.currentItem ?: return
        markComplete(item)
        val isLast = _state.value.currentIndex >= (_state.value.plan?.items?.lastIndex ?: 0)
        if (isLast) finish() else goRest(item)
    }

    private fun goRest(item: ExerciseItem) {
        if (item.restSec <= 0) {
            goNext()
            return
        }
        _state.value = _state.value.copy(
            phase = Phase.REST,
            secondsLeft = item.restSec,
            voiceCue = "Rest",
            voiceNonce = _state.value.voiceNonce + 1,
        )
        startCountdown { goNext() }
    }

    private fun goNext() {
        val next = _state.value.currentIndex + 1
        if (next < (_state.value.plan?.items?.size ?: 0)) startItem(next) else finish()
    }

    private fun markComplete(item: ExerciseItem) {
        val added = CalorieMath.metCalories(item.met, weightKg, item.durationSec ?: 45)
        _state.value = _state.value.copy(
            completedIds = _state.value.completedIds + item.id,
            caloriesBurned = _state.value.caloriesBurned + added,
        )
    }

    private fun finish() {
        timerJob?.cancel()
        _state.value = _state.value.copy(phase = Phase.DONE, voiceCue = "Workout complete. Great job!", voiceNonce = _state.value.voiceNonce + 1)
        val s = _state.value
        viewModelScope.launch {
            exerciseRepository.saveSession(
                WorkoutSession(
                    planId = planId,
                    startEpochMillis = if (sessionStart > 0) sessionStart else DateUtil.nowMillis(),
                    endEpochMillis = DateUtil.nowMillis(),
                    completedItemIds = s.completedIds.toList(),
                    caloriesBurned = s.caloriesBurned,
                    completed = true,
                ),
            )
        }
    }

    private fun startCountdown(onZero: () -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.secondsLeft > 0) {
                delay(1000)
                if (_state.value.running) {
                    _state.value = _state.value.copy(secondsLeft = _state.value.secondsLeft - 1)
                }
            }
            onZero()
        }
    }

    private fun fetchImage(item: ExerciseItem) {
        _state.value = _state.value.copy(generatingImage = true)
        viewModelScope.launch {
            val asset = imageRepository.getOrFetchExerciseImage(item)
            _state.value = _state.value.copy(generatingImage = false, stepImageRef = asset.bestRef())
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
