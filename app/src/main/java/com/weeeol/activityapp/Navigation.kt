package com.weeeol.activityapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FloatingNavigationBar(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier,
    navPosition: Animatable<Float, AnimationVector1D>? = null
) {
    val navItems = remember { NavItem.entries }
    val selectedIndex = navItems.indexOf(selectedItem)

    val position = navPosition ?: remember { Animatable(selectedIndex.toFloat()) }

    var barWidthPx by remember { mutableFloatStateOf(0f) }
    var isHoldingOrDragging by remember { mutableStateOf(false) }

    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var lastHapticIndex by remember { mutableIntStateOf(selectedIndex) }

    val pillSpringSpec = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMediumLow
    )

    // Dynamic expansion states when held or dragged (pure GPU transforms - no relayout)
    val pillScaleX by animateFloatAsState(
        targetValue = if (isHoldingOrDragging) 1.14f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillScaleX"
    )

    val pillScaleY by animateFloatAsState(
        targetValue = if (isHoldingOrDragging) 1.10f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillScaleY"
    )

    val pillAlpha by animateFloatAsState(
        targetValue = if (isHoldingOrDragging) 0.85f else 0.55f,
        animationSpec = tween(180),
        label = "pillAlpha"
    )

    LaunchedEffect(selectedIndex) {
        if (!isHoldingOrDragging) {
            val target = selectedIndex.toFloat()
            if (kotlin.math.abs(position.value - target) > 0.001f) {
                position.animateTo(
                    targetValue = target,
                    animationSpec = pillSpringSpec
                )
            }
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .height(66.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(33.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(33.dp)
            )
            .onSizeChanged { size -> barWidthPx = size.width.toFloat() }
            .pointerInput(barWidthPx) {
                if (barWidthPx <= 0f) return@pointerInput
                val tabWidth = barWidthPx / navItems.size
                val maxIndex = (navItems.size - 1).toFloat()
                val touchSlop = viewConfiguration.touchSlop

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isHoldingOrDragging = true
                    lastHapticIndex = position.value.roundToInt().coerceIn(0, navItems.size - 1)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    var totalDragDistance = 0f
                    var isDragStarted = false

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.pressed) {
                            val dragAmount = change.position.x - change.previousPosition.x
                            totalDragDistance += kotlin.math.abs(dragAmount)

                            if (!isDragStarted && totalDragDistance > touchSlop) {
                                isDragStarted = true
                            }

                            if (isDragStarted && dragAmount != 0f) {
                                change.consume()
                                val deltaFraction = dragAmount / tabWidth
                                val newPosition = (position.value + deltaFraction).coerceIn(-0.12f, maxIndex + 0.12f)
                                coroutineScope.launch {
                                    position.snapTo(newPosition)
                                }

                                val currentHapticIndex = newPosition.roundToInt().coerceIn(0, navItems.size - 1)
                                if (currentHapticIndex != lastHapticIndex) {
                                    lastHapticIndex = currentHapticIndex
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        } else {
                            break
                        }
                    } while (event.changes.any { it.pressed })

                    isHoldingOrDragging = false

                    if (isDragStarted) {
                        val closestIndex = position.value.roundToInt().coerceIn(0, navItems.size - 1)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentOnItemSelected(navItems[closestIndex])

                        coroutineScope.launch {
                            position.animateTo(
                                targetValue = closestIndex.toFloat(),
                                animationSpec = pillSpringSpec
                            )
                        }
                    }
                }
            }
    ) {
        // Fluid Sliding Indicator Pill (Apple Style with interactive expansion)
        if (barWidthPx > 0) {
            val tabWidth = barWidthPx / navItems.size
            val pillOffsetPx = (position.value * tabWidth).roundToInt()

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f / navItems.size)
                    .offset { IntOffset(pillOffsetPx, 0) }
                    .graphicsLayer {
                        scaleX = pillScaleX
                        scaleY = pillScaleY
                    }
                    .padding(6.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = pillAlpha))
            )
        }

        // Navigation Items (Stable layout, no jitter)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val activeIndex = position.value.roundToInt().coerceIn(0, navItems.size - 1)
                val isSelected = activeIndex == index

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    label = "navItemColor"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.7f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "navItemScale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (item != selectedItem) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentOnItemSelected(item)
                                    coroutineScope.launch {
                                        position.animateTo(
                                            targetValue = index.toFloat(),
                                            animationSpec = pillSpringSpec
                                        )
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.scale(scale)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}