package com.neofit.feature.exercise

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.domain.model.ExerciseItem
import com.neofit.domain.model.ExercisePlan
import com.neofit.feature.common.DishImage
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.PrimaryButton
import com.neofit.feature.common.SecondaryButton
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    planId: String,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val plan = state.plan
    var voiceOn by remember { mutableStateOf(true) }
    val tts = rememberTts()

    // Speak the current cue whenever it changes (and voice is enabled).
    LaunchedEffect(state.voiceNonce) {
        if (voiceOn && state.voiceCue.isNotBlank()) {
            tts.value?.speak(state.voiceCue, TextToSpeech.QUEUE_FLUSH, null, "neo")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan?.title ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { voiceOn = !voiceOn }) {
                        Icon(
                            if (voiceOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Voice",
                        )
                    }
                },
            )
        },
    ) { inner ->
        if (plan == null) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { Text("Plan not found") }
            return@Scaffold
        }
        val modifier = Modifier.fillMaxSize().padding(inner).padding(16.dp)
        when {
            !state.started -> PlanOverview(plan, modifier, onStart = viewModel::start)
            state.phase == Phase.DONE -> WorkoutSummary(state.caloriesBurned, state.completedIds.size, plan.items.size, modifier, onBack)
            else -> RunnerView(state, modifier, onPause = viewModel::togglePause, onDone = viewModel::userDone, onSkip = viewModel::skip)
        }
    }
}

@Composable
private fun PlanOverview(plan: ExercisePlan, modifier: Modifier, onStart: () -> Unit) {
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(plan.description, style = MaterialTheme.typography.bodyLarge)
        Text(
            "${plan.difficulty.label} • ${plan.durationMin} min • ${plan.schedule}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("Equipment: ${plan.requiredEquipment.joinToString()}", style = MaterialTheme.typography.bodyMedium)
        NeoCard(Modifier.fillMaxWidth()) {
            Text("⚠️ ${plan.safetyNote}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Exercises (${plan.items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        plan.items.forEach { item ->
            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        item.reps ?: "${item.durationSec ?: 0}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (item.targetMuscles.isNotEmpty()) {
                        Text("Targets: ${item.targetMuscles.joinToString()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        PrimaryButton("Start workout", onStart)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RunnerView(
    state: ExerciseRunnerState,
    modifier: Modifier,
    onPause: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
) {
    val item = state.currentItem ?: return
    Column(modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Step ${state.currentIndex + 1} of ${state.plan?.items?.size ?: 0}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            if (state.phase == Phase.REST) "Rest" else item.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        ExerciseMedia(
            videoUrl = item.videoUrl,
            imageRef = state.stepImageRef,
            label = item.name,
            modifier = Modifier.fillMaxWidth().height(200.dp),
        )

        if (state.phase == Phase.REST || item.durationSec != null) {
            Text("${state.secondsLeft}s", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        } else {
            Text(item.reps ?: "Do your reps", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }

        if (state.phase == Phase.EXERCISE) {
            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    item.instructions.forEachIndexed { i, step ->
                        Text("${i + 1}. $step", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }

        Text("🔥 ${state.caloriesBurned} kcal burned", style = MaterialTheme.typography.labelLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(if (state.running) "Pause" else "Resume", onPause, modifier = Modifier.weight(1f))
            if (state.phase == Phase.EXERCISE && item.durationSec == null) {
                PrimaryButton("Done", onDone, modifier = Modifier.weight(1f))
            } else {
                PrimaryButton("Skip", onSkip, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WorkoutSummary(calories: Int, completed: Int, total: Int, modifier: Modifier, onBack: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("🎉", style = MaterialTheme.typography.displayMedium)
        Text("Workout complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("$completed of $total exercises done", style = MaterialTheme.typography.titleMedium)
        Text("🔥 ~$calories kcal burned", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        PrimaryButton("Done", onBack)
    }
}

@Composable
private fun rememberTts(): androidx.compose.runtime.State<TextToSpeech?> {
    val context = LocalContext.current
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.ENGLISH
                ttsState.value = engine
            }
        }
        onDispose {
            engine?.stop()
            engine?.shutdown()
            ttsState.value = null
        }
    }
    return ttsState
}
