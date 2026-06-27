package com.neofit.feature.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neofit.R
import com.neofit.domain.model.ExercisePlan
import com.neofit.feature.common.NeoCard

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExercisePlansScreen(
    contentPadding: PaddingValues,
    onOpenPlan: (String) -> Unit,
    viewModel: ExercisePlansViewModel = hiltViewModel(),
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.ex_plans_title)) }) },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(inner),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, contentPadding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(viewModel.plans, key = { it.id }) { plan ->
                PlanCard(plan) { onOpenPlan(plan.id) }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: ExercisePlan, onClick: () -> Unit) {
    NeoCard(Modifier.fillMaxWidth().clickable { onClick() }) {
        Column {
            Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(plan.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.padding(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onClick,
                    label = { Text(plan.difficulty.label) },
                )
                AssistChip(
                    onClick = onClick,
                    label = { Text("${plan.durationMin} min") },
                    leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.width(18.dp)) },
                )
                AssistChip(
                    onClick = onClick,
                    label = { Text("~${plan.estimatedCalories} kcal") },
                    leadingIcon = { Icon(Icons.Filled.Whatshot, contentDescription = null, modifier = Modifier.width(18.dp)) },
                )
            }
        }
    }
}
