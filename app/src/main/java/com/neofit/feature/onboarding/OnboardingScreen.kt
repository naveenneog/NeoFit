package com.neofit.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.designsystem.NeoCardSpacingDp
import com.neofit.domain.model.ActivityLevel
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.Sex
import com.neofit.domain.model.WellnessGoal
import com.neofit.feature.common.LabeledTextField
import com.neofit.feature.common.MultiChoiceChips
import com.neofit.feature.common.NumberField
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.PrimaryButton
import com.neofit.feature.common.SecondaryButton
import com.neofit.feature.common.SingleChoiceChips
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { (state.step + 1f) / state.totalSteps },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            when (state.step) {
                0 -> StepLanguage(state.language) { viewModel.update { d -> d.copy(language = it) } }
                1 -> StepBasics(state, viewModel)
                2 -> StepBody(state, viewModel)
                3 -> StepActivity(state.activityLevel) { viewModel.update { d -> d.copy(activityLevel = it) } }
                4 -> StepDiet(state, viewModel)
                5 -> StepGoal(state.goal) { viewModel.update { d -> d.copy(goal = it) } }
                6 -> StepRegion(state.region) { viewModel.update { d -> d.copy(region = it) } }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.step > 0) {
                SecondaryButton(stringResource(R.string.action_back), onClick = viewModel::back, modifier = Modifier.weight(1f))
            }
            PrimaryButton(
                text = if (state.step == state.totalSteps - 1) stringResource(R.string.onb_finish) else stringResource(R.string.action_next),
                onClick = viewModel::next,
                enabled = state.canProceed && !state.saving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    if (subtitle != null) {
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun StepLanguage(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Column {
        StepHeader(stringResource(R.string.onb_welcome_title), stringResource(R.string.onb_welcome_sub))
        SingleChoiceChips(
            title = stringResource(R.string.onb_choose_language),
            options = AppLanguage.entries,
            selected = selected,
            label = { it.nativeLabel },
            onSelect = onSelect,
        )
    }
}

@Composable
private fun StepBasics(state: OnboardingDraft, vm: OnboardingViewModel) {
    Column {
        StepHeader("Tell us about you")
        LabeledTextField(state.name, { v -> vm.update { it.copy(name = v) } }, stringResource(R.string.onb_name))
        Spacer(Modifier.height(NeoCardSpacingDp))
        NumberField(state.age, { v -> vm.update { it.copy(age = v) } }, stringResource(R.string.onb_age))
        Spacer(Modifier.height(NeoCardSpacingDp))
        SingleChoiceChips(
            title = stringResource(R.string.onb_sex),
            options = Sex.entries,
            selected = state.sex,
            label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onSelect = { s -> vm.update { it.copy(sex = s) } },
        )
    }
}

@Composable
private fun StepBody(state: OnboardingDraft, vm: OnboardingViewModel) {
    Column {
        StepHeader("Your measurements")
        NumberField(state.heightCm, { v -> vm.update { it.copy(heightCm = v) } }, stringResource(R.string.onb_height))
        Spacer(Modifier.height(NeoCardSpacingDp))
        NumberField(state.currentWeightKg, { v -> vm.update { it.copy(currentWeightKg = v) } }, stringResource(R.string.onb_weight))
        Spacer(Modifier.height(NeoCardSpacingDp))
        NumberField(state.targetWeightKg, { v -> vm.update { it.copy(targetWeightKg = v) } }, stringResource(R.string.onb_target_weight))
    }
}

@Composable
private fun StepActivity(selected: ActivityLevel, onSelect: (ActivityLevel) -> Unit) {
    Column {
        StepHeader(stringResource(R.string.onb_activity))
        SingleChoiceChips(ActivityLevel.entries, selected, { it.label }, onSelect)
    }
}

@Composable
private fun StepDiet(state: OnboardingDraft, vm: OnboardingViewModel) {
    Column {
        StepHeader(stringResource(R.string.onb_diet))
        SingleChoiceChips(DietaryPreference.entries, state.diet, { it.label }, { d -> vm.update { it.copy(diet = d) } })

        if (state.diet != DietaryPreference.VEGETARIAN && state.diet != DietaryPreference.VEGAN) {
            Spacer(Modifier.height(NeoCardSpacingDp))
            MultiChoiceChips(
                title = "Veg-only days (optional)",
                options = (1..7).toList(),
                selected = state.vegDays,
                label = { weekdayLabel(it) },
                onToggle = { day ->
                    vm.update {
                        val s = if (day in it.vegDays) it.vegDays - day else it.vegDays + day
                        it.copy(vegDays = s)
                    }
                },
            )
            Text(
                "On these days Neo Fit suggests vegetarian meals (e.g. many people keep Tuesday or Thursday veg).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Spacer(Modifier.height(NeoCardSpacingDp))
        LabeledTextField(
            state.restrictions,
            { v -> vm.update { it.copy(restrictions = v) } },
            "Restrictions (comma separated, e.g. no onion garlic)",
            keyboardType = KeyboardType.Text,
        )
    }
}

private fun weekdayLabel(day: Int): String = when (day) {
    1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; else -> "Sun"
}

@Composable
private fun StepGoal(selected: WellnessGoal, onSelect: (WellnessGoal) -> Unit) {
    Column {
        StepHeader(stringResource(R.string.onb_goal))
        SingleChoiceChips(WellnessGoal.entries, selected, { it.label }, onSelect)
    }
}

@Composable
private fun StepRegion(selected: FoodRegion, onSelect: (FoodRegion) -> Unit) {
    Column {
        StepHeader(stringResource(R.string.onb_region), "Pick your usual food region — advisory only, you can change or auto-detect later.")
        SingleChoiceChips(FoodRegion.entries, selected, { it.label }, onSelect)
        Spacer(Modifier.height(16.dp))
        NeoCard {
            Text(
                stringResource(R.string.disclaimer_text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
