package com.flymero.mifimanager.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GaugeChart(
    value: Float,
    maxValue: Float,
    label: String,
    unit: String = "",
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedValue by animateFloatAsState(
        targetValue = (value / maxValue).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "gauge"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = 240f
            val startAngle = 150f
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val arcSize = Size(this.size.width, this.size.height)

            drawArc(
                color = backgroundColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke,
                size = arcSize
            )

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedValue,
                useCenter = false,
                style = stroke,
                size = arcSize
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (unit.isNotEmpty()) "${"%.0f".format(value)}$unit" else "%.0f".format(value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
