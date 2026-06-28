package com.neofit.feature.dashboard

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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.common.UiState
import com.neofit.domain.model.StepSource
import com.neofit.core.designsystem.CalorieBurned
import com.neofit.core.designsystem.CalorieEaten
import com.neofit.core.designsystem.CalorieRemaining
import com.neofit.core.designsystem.CarbColor
import com.neofit.core.designsystem.FatColor
import com.neofit.core.designsystem.NeoGreen
import com.neofit.core.designsystem.ProteinColor
import com.neofit.core.i18n.LocalAppLanguage
import com.neofit.domain.model.DashboardSummary
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.Recommendation
import com.neofit.feature.common.DishImage
import com.neofit.feature.common.LoadingState
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.RingProgress
import com.neofit.feature.common.SectionTitle
import com.neofit.feature.common.foodAssetUri
import com.neofit.feature.common.signedKg
import com.neofit.feature.navigation.Routes
import com.neofit.core.util.Format

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> DashboardContent(s.data, contentPadding, onNavigate, viewModel::addWater)
        else -> LoadingState()
    }
}

@Composable
private fun DashboardContent(
    data: DashboardSummary,
    contentPadding: PaddingValues,
    onNavigate: (String) -> Unit,
    onAddWater: () -> Unit,
) {
    val language = LocalAppLanguage.current
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    "${stringResourceGreeting()} ${data.userName}".trim(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.dash_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (data.isVegDayToday) {
                    Spacer(Modifier.height(6.dp))
                    Surface(color = NeoGreen.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                        Text(
                            stringResource(R.string.dash_veg_day),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = NeoGreen,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }

        item { CalorieCard(data) }
        item { MacroCard(data) }
        item { QuickActions(onNavigate) }
        item { StepsWellnessRow(data, onSync = { onNavigate(Routes.PROGRESS) }) }
        item { HydrationWeightRow(data, onAddWater) }

        if (data.recommendations.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.dash_for_you)) }
            items(data.recommendations) { rec -> RecommendationCard(rec, onNavigate) }
        }

        if (data.recommendedMeals.isNotEmpty()) {
            item { SectionTitle(stringResourceRecommended()) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(data.recommendedMeals) { food ->
                        RecommendedMealCard(food, food.displayName(language)) {
                            onNavigate(Routes.foodAdd(food.id))
                        }
                    }
                }
            }
        }

        item {
            Text(
                stringResourceDisclaimer(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable private fun stringResourceGreeting() = androidx.compose.ui.res.stringResource(R.string.dash_greeting)
@Composable private fun stringResourceRecommended() = androidx.compose.ui.res.stringResource(R.string.dash_recommended)
@Composable private fun stringResourceDisclaimer() = androidx.compose.ui.res.stringResource(R.string.disclaimer_text)

@Composable
private fun CalorieCard(data: DashboardSummary) {
    NeoCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RingProgress(
                progress = data.calorieProgress,
                color = CalorieRemaining,
                centerValue = "${data.caloriesRemaining}",
                centerLabel = stringResource(R.string.dash_kcal_left),
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricLine(CalorieEaten, androidx.compose.ui.res.stringResource(R.string.dash_calories_consumed), "${data.caloriesConsumed}")
                MetricLine(CalorieBurned, androidx.compose.ui.res.stringResource(R.string.dash_calories_burned), "${data.caloriesBurned}")
                MetricLine(MaterialTheme.colorScheme.onSurfaceVariant, stringResource(R.string.label_target), "${data.calorieTarget}")
            }
        }
    }
}

@Composable
private fun MetricLine(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color, shape = RoundedCornerShape(50), modifier = Modifier.size(10.dp)) {}
        Spacer(Modifier.width(8.dp))
        Text("$label  ", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MacroCard(data: DashboardSummary) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(stringResource(R.string.dash_macros_today))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                // Carb/fat guidance targets from a standard split of the calorie target
                // (≈50% carbs, ≈28% fat) so all three macros show a target consistently.
                val carbTarget = data.calorieTarget * 0.5f / 4f
                val fatTarget = data.calorieTarget * 0.28f / 9f
                MacroPill(stringResource(R.string.macro_protein), data.proteinConsumedG, data.proteinTargetG.toFloat(), ProteinColor)
                MacroPill(stringResource(R.string.macro_carbs), data.carbsConsumedG, carbTarget, CarbColor)
                MacroPill(stringResource(R.string.macro_fat), data.fatConsumedG, fatTarget, FatColor)
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: Float, target: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        Text("${value.toInt()} g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (target > 0) Text("/ ${target.toInt()} g", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickActions(onNavigate: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        QuickAction(Icons.Filled.Search, androidx.compose.ui.res.stringResource(R.string.dash_add_food), Modifier.weight(1f)) { onNavigate(Routes.FOOD_SEARCH) }
        QuickAction(Icons.Filled.CameraAlt, androidx.compose.ui.res.stringResource(R.string.food_photo_log), Modifier.weight(1f)) { onNavigate(Routes.FOOD_PHOTO) }
        QuickAction(Icons.Filled.FitnessCenter, androidx.compose.ui.res.stringResource(R.string.dash_quick_workout), Modifier.weight(1f)) { onNavigate(Routes.EXERCISE_PLANS) }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(84.dp),
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun StepsWellnessRow(data: DashboardSummary, onSync: () -> Unit) {
    val realSteps = data.steps > 0 || data.stepSource == StepSource.HEALTH_CONNECT
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        NeoCard(Modifier.weight(1f).clickable { onSync() }) {
            Column {
                Text(androidx.compose.ui.res.stringResource(R.string.dash_steps), style = MaterialTheme.typography.labelLarge)
                Text("${data.steps}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "of ${data.stepTarget}" + if (realSteps) " • ${stepSourceLabel(data.stepSource)}" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        NeoCard(Modifier.weight(1f)) {
            Column {
                Text(androidx.compose.ui.res.stringResource(R.string.dash_wellness), style = MaterialTheme.typography.labelLarge)
                Text("${data.wellness.score}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("🔥 ${data.streakDays} day streak", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun stepSourceLabel(source: StepSource): String = when (source) {
    StepSource.HEALTH_CONNECT -> "Health Connect"
    StepSource.MANUAL -> "manual"
    StepSource.ESTIMATED -> "estimated"
    StepSource.NONE -> ""
}

@Composable
private fun HydrationWeightRow(data: DashboardSummary, onAddWater: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        NeoCard(Modifier.weight(1f).clickable { onAddWater() }) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = FatColor)
                    Spacer(Modifier.width(6.dp))
                    Text(androidx.compose.ui.res.stringResource(R.string.dash_hydration), style = MaterialTheme.typography.labelLarge)
                }
                Text("${data.waterGlasses} / ${data.waterTarget}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(androidx.compose.ui.res.stringResource(R.string.dash_water_glass), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        NeoCard(Modifier.weight(1f)) {
            Column {
                Text(androidx.compose.ui.res.stringResource(R.string.dash_weight), style = MaterialTheme.typography.labelLarge)
                Text(Format.weight(data.currentWeightKg), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(signedKg(data.weeklyWeightDeltaKg) + " / wk", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RecommendationCard(rec: Recommendation, onNavigate: (String) -> Unit) {
    NeoCard(Modifier.fillMaxWidth().clickable { rec.actionRoute?.let(onNavigate) }) {
        Column {
            Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(rec.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecommendedMealCard(food: FoodItem, displayName: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(140.dp),
    ) {
        Column(Modifier.padding(8.dp)) {
            DishImage(imageRef = foodAssetUri(food.id), label = food.nameEn, modifier = Modifier.fillMaxWidth().height(90.dp))
            Spacer(Modifier.height(6.dp))
            Text(displayName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("~${food.caloriesKcal} kcal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
