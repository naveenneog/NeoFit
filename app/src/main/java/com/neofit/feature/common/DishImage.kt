package com.neofit.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.absoluteValue

private val gradientPairs = listOf(
    Color(0xFFFF8A33) to Color(0xFFE8505B),
    Color(0xFF1B998B) to Color(0xFF38BDF8),
    Color(0xFF8B5CF6) to Color(0xFFEC4899),
    Color(0xFFFBBF24) to Color(0xFFFF7A1A),
    Color(0xFF34D399) to Color(0xFF1B998B),
)

/**
 * Displays a dish/exercise image. Falls back to a colourful gradient tile with
 * the item's initials when no image (cached, web, or generated) is available.
 *
 * `imageRef` may be a local file path or an http(s) URL.
 */
@Composable
fun DishImage(
    imageRef: String?,
    label: String,
    modifier: Modifier = Modifier,
    cornerRadiusDp: Int = 16,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadiusDp.dp())),
        contentAlignment = Alignment.Center,
    ) {
        // Always render the placeholder behind, so image-load failures degrade gracefully.
        val pair = gradientPairs[label.hashCode().absoluteValue % gradientPairs.size]
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(pair.first, pair.second))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials(label),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        if (!imageRef.isNullOrBlank()) {
            AsyncImage(
                model = when {
                    imageRef.startsWith("http") || imageRef.startsWith("file:///android_asset") -> imageRef
                    else -> File(imageRef)
                },
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Asset URI for a bundled gpt-image-2 generated dish photo (falls back to placeholder if absent). */
fun foodAssetUri(foodId: String): String = "file:///android_asset/food/$foodId.jpg"

private fun initials(label: String): String =
    label.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "🍲" }

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())
