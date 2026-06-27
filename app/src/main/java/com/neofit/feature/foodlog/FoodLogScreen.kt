package com.neofit.feature.foodlog

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.i18n.LocalAppLanguage
import com.neofit.core.util.DateUtil
import com.neofit.core.util.Format
import com.neofit.domain.model.FoodItem
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
    onSearch: () -> Unit,
    onPhoto: () -> Unit,
    onOpenMeal: (Long) -> Unit,
    viewModel: FoodLogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun added(name: String) =
        Toast.makeText(context, context.getString(R.string.food_quick_added, name), Toast.LENGTH_SHORT).show()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.food_log_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSearch,
                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                text = { Text(stringResource(R.string.dash_add_food)) },
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

            if (state.recents.isNotEmpty() || state.favourites.isNotEmpty()) {
                item {
                    QuickAddStrip(
                        state = state,
                        onRecent = { meal -> added(meal.name); viewModel.quickAddRecent(meal) },
                        onFavourite = { food -> added(food.nameEn); viewModel.quickAddFavourite(food) },
                    )
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
private fun QuickAddStrip(
    state: FoodLogState,
    onRecent: (MealLog) -> Unit,
    onFavourite: (FoodItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.favourites.isNotEmpty()) {
            SectionTitle(stringResource(R.string.food_favourites))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.favourites, key = { "fav-${it.id}" }) { food ->
                    QuickAddChip(
                        imageRef = foodAssetUri(food.id),
                        name = food.nameEn,
                        kcal = food.caloriesKcal,
                        onClick = { onFavourite(food) },
                    )
                }
            }
        }
        if (state.recents.isNotEmpty()) {
            SectionTitle(stringResource(R.string.food_recents))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.recents, key = { "rec-${it.id}" }) { meal ->
                    QuickAddChip(
                        imageRef = meal.imageRef ?: meal.foodId?.let { foodAssetUri(it) },
                        name = meal.name,
                        kcal = meal.estimate.caloriesKcal,
                        onClick = { onRecent(meal) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAddChip(imageRef: String?, name: String, kcal: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(116.dp).clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
    ) {
        Column(Modifier.padding(8.dp)) {
            DishImage(imageRef, name, modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(10.dp)), cornerRadiusDp = 10)
            Spacer(Modifier.height(6.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(2.dp))
                Text("~$kcal kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
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
