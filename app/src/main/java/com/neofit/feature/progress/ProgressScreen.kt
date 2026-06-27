package com.neofit.feature.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import com.neofit.core.designsystem.NeoSaffron
import com.neofit.core.util.DateUtil
import com.neofit.core.util.Format
import com.neofit.feature.common.MiniBarChart
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.SectionTitle

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    contentPadding: PaddingValues,
    onOpenWeight: () -> Unit,
    onOpenInsights: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.prog_title)) }) },
    ) { inner ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = contentPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                NeoCard(Modifier.weight(1f)) {
                    Column {
                        Text(stringResource(R.string.prog_bmi), style = MaterialTheme.typography.labelLarge)
                        Text(Format.bmi(state.bmi), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(Format.bmiCategory(state.bmi), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                NeoCard(Modifier.weight(1f)) {
                    Column {
                        Text(stringResource(R.string.dash_weight), style = MaterialTheme.typography.labelLarge)
                        Text(Format.weight(state.currentWeightKg), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Goal ${Format.weight(state.targetWeightKg)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle(stringResource(R.string.prog_weight_history))
                    Spacer(Modifier.height(10.dp))
                    if (state.weights.size >= 2) {
                        MiniBarChart(
                            values = state.weights.takeLast(7).map { it.weightKg },
                            labels = state.weights.takeLast(7).map { DateUtil.shortDate(it.dateEpochDay) },
                            barColor = NeoSaffron,
                        )
                    } else {
                        Text("Log your weight over time to see a trend.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().clickable { onOpenWeight() }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.prog_log_weight), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle("Weekly movement")
                    Spacer(Modifier.height(10.dp))
                    MiniBarChart(
                        values = state.weeklySteps.map { it.steps.toFloat() },
                        labels = state.weeklySteps.map { DateUtil.weekday(it.dateEpochDay) },
                        barColor = CalorieBurned,
                    )
                }
            }

            NeoCard(Modifier.fillMaxWidth().clickable { onOpenInsights() }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.insights_title), fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
