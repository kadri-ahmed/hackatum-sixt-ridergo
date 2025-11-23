package ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SlideUpComponent(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val constraintsMaxHeight = maxHeight
        val screenHeightPx = with(density) { constraintsMaxHeight.toPx() }

        
        // Anchors
        val hiddenOffset = screenHeightPx
        val halfExpandedOffset = screenHeightPx * 0.4f // 60% visible
        val expandedOffset = 0f

        // Offset state
        val offsetY = remember { Animatable(hiddenOffset) }

        LaunchedEffect(isVisible) {
            if (isVisible) {
                offsetY.animateTo(
                    targetValue = halfExpandedOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            } else {
                offsetY.animateTo(
                    targetValue = hiddenOffset,
                    animationSpec = tween(300)
                )
            }
        }

        val draggableState = rememberDraggableState { delta ->
            scope.launch {
                val newOffset = (offsetY.value + delta).coerceIn(expandedOffset, hiddenOffset)
                offsetY.snapTo(newOffset)
            }
        }

        if (isVisible || offsetY.value < hiddenOffset) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f * (1f - (offsetY.value / hiddenOffset).coerceIn(0f, 1f))))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() }
                )

                // Sheet
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter) // Align Top to allow full expansion
                        .fillMaxWidth()
                        .height(constraintsMaxHeight) // Full height
                        .offset { IntOffset(0, offsetY.value.toInt()) }
                        .draggable(
                            state = draggableState,
                            orientation = Orientation.Vertical,
                            onDragStopped = { velocity ->
                                scope.launch {
                                    val current = offsetY.value
                                    val flingThreshold = 500f // pixels per second
                                    
                                    // Determine target based on velocity and position
                                    val targetValue = when {
                                        // Fling Down
                                        velocity > flingThreshold -> {
                                            if (current < halfExpandedOffset) halfExpandedOffset else hiddenOffset
                                        }
                                        // Fling Up
                                        velocity < -flingThreshold -> {
                                            if (current > halfExpandedOffset) halfExpandedOffset else expandedOffset
                                        }
                                        // Drag (Snap to nearest)
                                        else -> {
                                            val distToExpanded = kotlin.math.abs(current - expandedOffset)
                                            val distToHalf = kotlin.math.abs(current - halfExpandedOffset)
                                            val distToHidden = kotlin.math.abs(current - hiddenOffset)
                                            
                                            if (distToHidden < distToHalf && distToHidden < distToExpanded) hiddenOffset
                                            else if (distToExpanded < distToHalf) expandedOffset
                                            else halfExpandedOffset
                                        }
                                    }

                                    offsetY.animateTo(
                                        targetValue = targetValue,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )

                                    if (targetValue == hiddenOffset) {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize() // Fill sheet
                            .padding(16.dp)
                    ) {
                        // Drag Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        
                        content()
                    }
                }
            }
        }
    }
}
