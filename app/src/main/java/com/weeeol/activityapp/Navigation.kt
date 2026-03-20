package com.weeeol.activityapp

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

@Composable
fun FloatingNavigationBar(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = NavItem.entries
    val selectedIndex = navItems.indexOf(selectedItem)

    var barWidthPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)

    val indicatorOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex, barWidthPx) {
        if (!isDragging && barWidthPx > 0) {
            val targetOffset = (barWidthPx / navItems.size) * selectedIndex
            indicatorOffset.animateTo(
                targetValue = targetOffset,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    val glowColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current

    // THE FIX: We calculate the heavy blur effect exactly ONCE and remember it!
    val blurPaint = remember(glowColor, density) {
        Paint().apply {
            color = glowColor
            style = PaintingStyle.Stroke
            with(density) {
                strokeWidth = 6.dp.toPx()
                asFrameworkPaint().maskFilter =
                    android.graphics.BlurMaskFilter(16.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .height(72.dp)
            .fillMaxWidth()

            // 1. THE OUTLINE BLOOM MAGIC
            .drawBehind {
                drawIntoCanvas { canvas ->


                    // Draw the glowing outline exactly where the border will be
                    canvas.drawRoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        radiusX = 36.dp.toPx(),
                        radiusY = 36.dp.toPx(),
                        paint = blurPaint
                    )

                }
            }

            // 2. Your glass background
            .clip(RoundedCornerShape(36.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))

            // 3. I updated the border color here to match the glow slightly so it blends better!
            .border(1.dp, glowColor.copy(alpha = 0.5f), RoundedCornerShape(36.dp))

            // 4. Everything else stays the same
            .onSizeChanged { size -> barWidthPx = size.width.toFloat() }
            .pointerInput(barWidthPx) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onHorizontalDrag = { _, dragAmount ->
                        if (barWidthPx > 0) {
                            coroutineScope.launch {
                                val tabWidthPx = barWidthPx / navItems.size
                                val maxOffset = barWidthPx - tabWidthPx
                                val newOffset = (indicatorOffset.value + dragAmount).coerceIn(0f, maxOffset)
                                indicatorOffset.snapTo(newOffset)
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        if (barWidthPx > 0) {
                            val tabWidthPx = barWidthPx / navItems.size
                            val centerPx = indicatorOffset.value + (tabWidthPx / 2)
                            val closestIndex = (centerPx / tabWidthPx).toInt().coerceIn(0, navItems.size - 1)

                            currentOnItemSelected(navItems[closestIndex])

                            coroutineScope.launch {
                                indicatorOffset.animateTo(
                                    targetValue = tabWidthPx * closestIndex,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        if (barWidthPx > 0) {
                            coroutineScope.launch {
                                indicatorOffset.animateTo(
                                    targetValue = (barWidthPx / navItems.size) * currentSelectedIndex,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                        }
                    }
                )
            }
    ) {

        // THE FIX 3: The sliding pill. Uses the text color at 15% opacity.
        if (barWidthPx > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(1f / navItems.size)
                    .offset { IntOffset(indicatorOffset.value.roundToInt(), 0) }
                    .padding(8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            navItems.forEach { item ->
                val isSelected = selectedItem == item

                // THE FIX 4: Icon & Text Colors.
                // Solid black/white if selected, 40% transparent if unselected.
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(item) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}