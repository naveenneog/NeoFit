package com.neofit.feature.foodlog

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.core.util.DateUtil
import com.neofit.core.util.Format
import com.neofit.domain.model.MealLog
import com.neofit.feature.common.ConfidenceChip
import com.neofit.feature.common.DishImage
import com.neofit.feature.common.LoadingState
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.PrimaryButton
import com.neofit.feature.common.SectionTitle

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(
    mealId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MealDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val meal = state.meal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(meal?.name ?: "Meal") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { onEdit(mealId) }) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
                    if (state.canFavourite) {
                        IconButton(onClick = viewModel::toggleFavourite) {
                            if (state.isFavourite) {
                                Icon(Icons.Filled.Star, contentDescription = "Remove from favourites", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Filled.StarBorder, contentDescription = "Add to favourites")
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.delete(onBack) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                },
            )
        },
    ) { inner ->
        if (meal == null) {
            LoadingState(Modifier.padding(inner))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(inner).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DishImage(state.imageRef, meal.name, modifier = Modifier.fillMaxWidth().height(200.dp))

            // Only offer generation when no photo is available yet (e.g. custom meals);
            // library dishes already show a bundled gpt-image-2 photo.
            if (state.imageRef == null) {
                PrimaryButton(
                    text = if (state.generating) "Generating…" else "Generate dish image (Azure)",
                    onClick = viewModel::generateImage,
                    enabled = !state.generating,
                )
                if (state.generating) CircularProgressIndicator()
                state.imageMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            EstimateBreakdown(meal)

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle("Details")
                    Spacer(Modifier.height(8.dp))
                    DetailRow("Category", meal.category.label)
                    DetailRow("Region", meal.region.label)
                    DetailRow("Portion", meal.portion.label)
                    DetailRow("Logged", DateUtil.timeLabel(meal.timestampEpochMillis))
                    if (meal.manuallyCorrected) DetailRow("Note", "Manually corrected")
                }
            }
        }
    }
}

@Composable
private fun EstimateBreakdown(meal: MealLog) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(Format.calories(meal.estimate), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.weight(1f))
                ConfidenceChip(meal.estimate.confidence)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                MacroBox("Protein", meal.estimate.proteinG)
                MacroBox("Carbs", meal.estimate.carbsG)
                MacroBox("Fat", meal.estimate.fatG)
                MacroBox("Fiber", meal.estimate.fiberG)
            }
            Spacer(Modifier.height(10.dp))
            Text(meal.estimate.basis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MacroBox(label: String, grams: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(Format.grams(grams), style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
