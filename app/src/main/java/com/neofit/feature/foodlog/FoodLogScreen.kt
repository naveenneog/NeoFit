package com.neofit.feature.foodlog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.i18n.LocalAppLanguage
import com.neofit.core.util.DateUtil
import com.neofit.core.util.Format
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealLog
import com.neofit.feature.common.ConfidenceChip
import com.neofit.feature.common.DishImage
import com.neofit.feature.common.EmptyState
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.SectionTitle
import com.neofit.feature.common.foodAssetUri

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    contentPadding: PaddingValues,
    onAddMeal: () -> Unit,
    onSearch: () -> Unit,
    onPhoto: () -> Unit,
    onOpenMeal: (Long) -> Unit,
    viewModel: FoodLogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.food_log_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMeal,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add)) },
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(inner),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, contentPadding.calculateBottomPadding() + 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NeoCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Today", style = MaterialTheme.typography.labelLarge)
                            Text("${state.totalKcal} kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = "Search") }
                        IconButton(onClick = onPhoto) { Icon(Icons.Filled.CameraAlt, contentDescription = "Photo") }
                        IconButton(onClick = viewModel::repeatLast) { Icon(Icons.Filled.Refresh, contentDescription = "Repeat last") }
                    }
                }
            }

            if (state.meals.isEmpty()) {
                item { EmptyState(stringResource(R.string.state_empty_food)) }
            } else {
                MealCategory.entries.forEach { category ->
                    val meals = state.byCategory[category].orEmpty()
                    if (meals.isNotEmpty()) {
                        item { SectionTitle(categoryLabel(category)) }
                        items(meals, key = { it.id }) { meal ->
                            MealRow(meal, onClick = { onOpenMeal(meal.id) }, onDelete = { viewModel.delete(meal.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: MealCategory): String = when (category) {
    MealCategory.BREAKFAST -> stringResource(R.string.food_breakfast)
    MealCategory.LUNCH -> stringResource(R.string.food_lunch)
    MealCategory.DINNER -> stringResource(R.string.food_dinner)
    MealCategory.SNACK -> stringResource(R.string.food_snack)
}

@Composable
private fun MealRow(meal: MealLog, onClick: () -> Unit, onDelete: () -> Unit) {
    NeoCard(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DishImage(meal.imageRef ?: meal.foodId?.let { foodAssetUri(it) }, meal.name, modifier = Modifier.size(54.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(meal.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${meal.portion.label} • ${DateUtil.timeLabel(meal.timestampEpochMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                ConfidenceChip(meal.estimate.confidence)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(Format.calories(meal.estimate), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
            }
        }
    }
}
