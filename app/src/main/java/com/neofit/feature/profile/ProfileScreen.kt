package com.neofit.feature.profile

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
import com.neofit.core.i18n.LocaleController
import com.neofit.core.util.Format
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.model.Goal
import com.neofit.domain.model.UserProfile
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.SectionTitle
import com.neofit.feature.common.SingleChoiceChips

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    onOpenRegionPrefs: () -> Unit,
    onOpenInsights: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.profile_title)) }) },
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
            state.profile?.let { ProfileCard(it) }
            state.goal?.let { GoalCard(it) }

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle(stringResource(R.string.profile_language))
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceChips(
                        options = AppLanguage.entries,
                        selected = state.language,
                        label = { it.nativeLabel },
                        onSelect = { lang ->
                            viewModel.setLanguage(lang)
                            // AppCompat applies + persists the locale and recreates activities.
                            LocaleController.apply(lang.localeTag)
                        },
                    )
                }
            }

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle("Theme")
                    Spacer(Modifier.height(8.dp))
                    val options = listOf<Pair<String, Boolean?>>("System" to null, "Light" to false, "Dark" to true)
                    SingleChoiceChips(
                        options = options,
                        selected = options.firstOrNull { it.second == state.darkTheme } ?: options.first(),
                        label = { it.first },
                        onSelect = { viewModel.setDarkTheme(it.second) },
                    )
                }
            }

            LinkRow(stringResource(R.string.profile_region_prefs), onOpenRegionPrefs)
            LinkRow(stringResource(R.string.insights_title), onOpenInsights)

            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.profile_disclaimer_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.disclaimer_text), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileCard(profile: UserProfile) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            Text(profile.name.ifBlank { "Your profile" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "${profile.age} yrs • ${profile.sex.name.lowercase()} • ${profile.heightCm.toInt()} cm",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${profile.dietaryPreference.label} • ${profile.goal.label} • ${profile.preferredRegion.label}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun GoalCard(goal: Goal) {
    NeoCard(Modifier.fillMaxWidth()) {
        Column {
            SectionTitle("Your targets")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Target("Calories", "${goal.dailyCalorieTarget}")
                Target("Protein", "${goal.dailyProteinTargetG} g")
                Target("Steps", "${goal.dailyStepTarget}")
            }
            Spacer(Modifier.height(10.dp))
            Text(goal.rationale, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Target(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit) {
    NeoCard(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
