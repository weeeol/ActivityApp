package com.weeeol.activityapp.ui.health

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val isRibbon: Boolean
)

@Composable
fun ConfettiCelebration(
    isTriggered: Boolean,
    primaryColor: Color,
    onFinished: () -> Unit
) {
    if (!isTriggered) return

    val confettiColors = remember(primaryColor) {
        listOf(
            primaryColor,
            Color(0xFFFFD700), // Gold
            Color(0xFFFA114F), // Move pink
            Color(0xFF92E01D), // Exercise lime
            Color(0xFF1DDAE2), // Stand cyan
            Color(0xFFBF5AF2), // Purple
            Color(0xFFFF9F0A)  // Orange
        )
    }

    val particles = remember {
        List(110) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = Random.nextFloat() * 650f + 250f
            ConfettiPiece(
                startX = 0.5f + (Random.nextFloat() - 0.5f) * 0.4f,
                startY = 0.35f + (Random.nextFloat() - 0.5f) * 0.2f,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed - 180f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                initialRotation = Random.nextFloat() * 360f,
                color = confettiColors[Random.nextInt(confettiColors.size)],
                width = Random.nextFloat() * 12f + 8f,
                height = Random.nextFloat() * 8f + 5f,
                isRibbon = Random.nextBoolean()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(isTriggered) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value
        val gravity = 950f * t * t
        val alpha = if (t > 0.65f) (1f - (t - 0.65f) / 0.35f).coerceIn(0f, 1f) else 1f

        particles.forEach { p ->
            val curX = (p.startX * w) + p.velocityX * t * 0.7f + sin(t * 10f + p.initialRotation) * 28f
            val curY = (p.startY * h) + p.velocityY * t * 0.7f + gravity
            val curRotation = p.initialRotation + p.rotationSpeed * t

            rotate(curRotation, pivot = Offset(curX, curY)) {
                if (p.isRibbon) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(curX - p.width / 2, curY - p.height / 2),
                        size = Size(p.width, p.height)
                    )
                } else {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.width / 2.5f,
                        center = Offset(curX, curY)
                    )
                }
            }
        }
    }
}
