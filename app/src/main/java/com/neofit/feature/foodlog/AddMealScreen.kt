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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.util.Format
import com.neofit.domain.model.CookingStyle
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.NutritionEstimate
import com.neofit.feature.common.ConfidenceChip
import com.neofit.feature.common.LabeledTextField
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.PrimaryButton
import com.neofit.feature.common.SingleChoiceChips

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    foodId: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddMealViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.food_add_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isCustom) {
                LabeledTextField(state.name, viewModel::setName, stringResource(R.string.food_dish_name))
            } else {
                Text(state.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            SingleChoiceChips(
                title = stringResource(R.string.food_meal),
                options = MealCategory.entries,
                selected = state.category,
                label = { it.label },
                onSelect = viewModel::setCategory,
            )

            SingleChoiceChips(
                title = stringResource(R.string.food_portion),
                options = state.portionOptions,
                selected = state.portion,
                label = { it.label },
                onSelect = viewModel::setPortion,
            )

            SingleChoiceChips(
                title = stringResource(R.string.food_cooking_optional),
                options = state.cookingOptions,
                selected = state.cooking,
                label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelect = { viewModel.setCooking(if (it == state.cooking) null else it) },
            )

            LabeledTextField(
                value = state.manualCalories,
                onValueChange = viewModel::setManualCalories,
                label = stringResource(R.string.food_override_calories),
                keyboardType = KeyboardType.Number,
            )

            state.estimate?.let { EstimateCard(it) }

            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { viewModel.save(onDone) },
                enabled = !state.saving && state.name.isNotBlank() && state.estimate != null,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EstimateCard(estimate: NutritionEstimate) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(Format.calories(estimate), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                ConfidenceChip(estimate.confidence)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Macro("Protein", estimate.proteinG)
                Macro("Carbs", estimate.carbsG)
                Macro("Fat", estimate.fatG)
            }
            Spacer(Modifier.height(8.dp))
            Text(estimate.basis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Macro(label: String, grams: Float) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(Format.grams(grams), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}
