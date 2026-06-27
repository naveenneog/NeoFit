package com.neofit.feature.profile

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neofit.R
import com.neofit.core.util.Format
import com.neofit.domain.model.FoodRegion
import com.neofit.feature.common.ConfidenceChip
import com.neofit.feature.common.NeoCard
import com.neofit.feature.common.SectionTitle
import com.neofit.feature.common.SingleChoiceChips

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RegionPreferencesScreen(
    onBack: () -> Unit,
    viewModel: RegionPreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_region_prefs)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NeoCard(Modifier.fillMaxWidth()) {
                Column {
                    SectionTitle(stringResource(R.string.onb_region))
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceChips(
                        options = FoodRegion.entries,
                        selected = state.profile?.preferredRegion,
                        label = { it.label },
                        onSelect = viewModel::setRegion,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Region is advisory only — it personalises suggestions but never restricts what you can log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            state.insight?.let { insight ->
                NeoCard(Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Detected: ${insight.inferred.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            ConfidenceChip(insight.confidence)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(insight.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (insight.distribution.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            insight.distribution.entries.sortedByDescending { it.value }.forEach { (region, count) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(region.label, style = MaterialTheme.typography.bodyMedium)
                                    Text("$count meals", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
