package com.neofit.feature.insights

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.designsystem.CalorieBurned
import com.neofit.core.designsystem.CalorieEaten
import com.neofit.core.util.DateUtil
import com.neofit.domain.model.Recommendation
import com.neofit.domain.model.WellnessSummary
import com.neofit.engine.WellnessScoreEngine
import com.neofit.feature.common.ConfidenceChip
import com.neofit.feature.common.LoadingState
import com.neofit.feature.common.MiniBarChart
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.SectionTitle

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val data by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.insights_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { inner ->
        val d = data
        if (d == null) {
            LoadingState(Modifier.padding(inner))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            WellnessCard(d.wellness)

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle("Calories this week")
                    Spacer(Modifier.height(10.dp))
                    MiniBarChart(
                        values = d.weeklyCalories.map { it.value.toFloat() },
                        labels = d.weeklyCalories.map { DateUtil.weekday(it.epochDay) },
                        barColor = CalorieEaten,
                    )
                }
            }

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle("Steps this week")
                    Spacer(Modifier.height(10.dp))
                    MiniBarChart(
                        values = d.weeklySteps.map { it.steps.toFloat() },
                        labels = d.weeklySteps.map { DateUtil.weekday(it.dateEpochDay) },
                        barColor = CalorieBurned,
                    )
                }
            }

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Region profile: ${d.regionInsight.inferred.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        ConfidenceChip(d.regionInsight.confidence)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(d.regionInsight.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (d.recommendations.isNotEmpty()) {
                SectionTitle("Nudges")
                d.recommendations.forEach { RecRow(it) }
            }
        }
    }
}

@Composable
private fun WellnessCard(w: WellnessSummary) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            Text("Wellness score", style = MaterialTheme.typography.labelLarge)
            Text("${w.score}/100", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Component("Consistency", w.consistencyScore, WellnessScoreEngine.CONSISTENCY_MAX)
            Component("Activity", w.activityScore, WellnessScoreEngine.ACTIVITY_MAX)
            Component("Calorie adherence", w.calorieAdherenceScore, WellnessScoreEngine.CALORIE_MAX)
            Component("Workout", w.workoutScore, WellnessScoreEngine.WORKOUT_MAX)
            Spacer(Modifier.height(8.dp))
            Text(
                "Score = consistency + activity + calorie adherence + workout. Fully transparent.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Component(label: String, value: Int, max: Int) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$value / $max", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { if (max > 0) value.toFloat() / max else 0f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RecRow(rec: Recommendation) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(rec.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
