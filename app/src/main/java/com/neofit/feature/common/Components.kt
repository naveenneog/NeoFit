package com.neofit.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.neofit.domain.model.ConfidenceLevel
import com.neofit.core.designsystem.NeoAmber
import com.neofit.core.designsystem.NeoSaffron
import com.neofit.core.designsystem.NeoGreen
import kotlin.math.abs

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        trailing?.invoke()
    }
}

@Composable
fun ConfidenceChip(level: ConfidenceLevel, modifier: Modifier = Modifier) {
    val (color, label) = when (level) {
        ConfidenceLevel.HIGH -> NeoGreen to "High confidence"
        ConfidenceLevel.MEDIUM -> NeoAmber to "Medium confidence"
        ConfidenceLevel.ROUGH -> NeoSaffron to "Rough estimate"
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50), modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Circular progress ring with a centred value/label. */
@Composable
fun RingProgress(
    progress: Float,
    color: Color,
    centerValue: String,
    centerLabel: String,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 120.dp,
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(diameter)) {
            val stroke = 14.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerValue, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(centerLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Simple horizontal bar series (e.g., weekly steps/calories). */
@Composable
fun MiniBarChart(
    values: List<Float>,
    labels: List<String>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Row(
        modifier = modifier.height(120.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { i, v ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier
                        .width(18.dp)
                        .height((90.dp.value * (v / max)).coerceAtLeast(3f).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(barColor),
                )
                Spacer(Modifier.height(4.dp))
                Text(labels.getOrElse(i) { "" }, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Line/trend chart with a zoomed y-axis (min..max with padding) plus a small
 * left axis showing the value range. Suited to metrics like weight where the
 * meaningful change is tiny relative to the absolute value, so a from-zero bar
 * chart would render every point as a near-identical full-height bar.
 */
@Composable
fun MiniLineChart(
    values: List<Float>,
    labels: List<String>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    valueFormat: (Float) -> String = { "%.1f".format(it) },
) {
    if (values.isEmpty()) return
    val minV = values.minOrNull() ?: 0f
    val maxV = values.maxOrNull() ?: 0f
    val rawRange = maxV - minV
    val pad = if (rawRange < 0.1f) 1f else rawRange * 0.3f
    val lo = minV - pad
    val span = (maxV + pad - lo).coerceAtLeast(0.1f)
    val axisWidth = 40.dp

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(120.dp)) {
            Column(
                Modifier.width(axisWidth).fillMaxHeight().padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                Text(valueFormat(maxV), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(valueFormat(minV), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                val inset = 6.dp.toPx()
                val w = (size.width - inset * 2).coerceAtLeast(1f)
                val h = (size.height - inset * 2).coerceAtLeast(1f)
                val n = values.size
                fun px(i: Int) = inset + if (n > 1) w * i / (n - 1) else w / 2f
                fun py(v: Float) = inset + h * (1f - (v - lo) / span)

                val area = Path().apply {
                    moveTo(px(0), size.height - inset)
                    values.forEachIndexed { i, v -> lineTo(px(i), py(v)) }
                    lineTo(px(n - 1), size.height - inset)
                    close()
                }
                drawPath(area, color = lineColor.copy(alpha = 0.12f))

                val line = Path().apply {
                    values.forEachIndexed { i, v ->
                        if (i == 0) moveTo(px(i), py(v)) else lineTo(px(i), py(v))
                    }
                }
                drawPath(line, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                values.forEachIndexed { i, v ->
                    drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(px(i), py(v)))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(axisWidth))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { Text(it, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
@Composable
fun EmptyState(message: String, emoji: String = "🍽️", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⚠️", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

/** Section list helper used by several screens. */
@Composable
fun ColumnSpacer(height: Int = 12) = Spacer(Modifier.height(height.dp))

internal fun signedKg(delta: Float): String {
    val sign = if (delta > 0) "+" else if (delta < 0) "-" else ""
    return "$sign${"%.1f".format(abs(delta))} kg"
}
