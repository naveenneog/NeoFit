package com.neofit.feature.foodlog

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddMealViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    val attemptBack: () -> Unit = { if (state.isDirty) showDiscardDialog = true else onBack() }

    BackHandler(enabled = state.isDirty) { showDiscardDialog = true }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                    Text(stringResource(R.string.discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.discard_keep))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            when {
                                state.isEditing -> R.string.food_edit_title
                                state.isCustom -> R.string.food_custom_entry
                                else -> R.string.food_add_title
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = attemptBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .imePadding()
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
                onClick = {
                    val msg = if (state.isEditing) R.string.meal_updated_toast else R.string.meal_added_toast
                    viewModel.save {
                        Toast.makeText(context, context.getString(msg), Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                },
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
