package com.weeeol.activityapp.ui.health

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

@Composable
fun RingLegend(title: String, color: Color, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = color)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (unit.isNotEmpty()) {
                Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
            }
        }
    }
}

@Composable
fun ActivityRings(
    moveProgress: Float,
    exerciseProgress: Float,
    standProgress: Float,
    modifier: Modifier = Modifier
) {
    val animMove by animateFloatAsState(
        targetValue = moveProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "moveAnim"
    )
    val animExercise by animateFloatAsState(
        targetValue = exerciseProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "exAnim"
    )
    val animStand by animateFloatAsState(
        targetValue = standProgress,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "stAnim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = size.width * 0.12f
        val spacing = 2.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)

        val r1 = (size.width - strokeWidth) / 2
        val r2 = r1 - strokeWidth - spacing
        val r3 = r2 - strokeWidth - spacing

        fun drawRing(radius: Float, progress: Float, color: Color) {
            // Background track
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            // Progress
            if (progress > 0) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (progress * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        drawRing(r1, animMove, MoveColor)
        drawRing(r2, animExercise, ExerciseColor)
        drawRing(r3, animStand, StandColor)
    }
}
