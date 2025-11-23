package ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.theme.SixtOrange
import kotlin.math.abs
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun SwipeableChatCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Peek animation to hint swipe right
    LaunchedEffect(Unit) {
        while (true) {
            // Wait for 1 second
            delay(1000)
            
            // Only animate if not currently being dragged
            if (offsetX.value == 0f) {
                launch {
                    val peekDistance = 50f // Always peek to the right
                    val peekRotation = 3f

                    // Peek out to the right
                    val peekJobs = listOf(
                        launch {
                            offsetX.animateTo(
                                targetValue = peekDistance,
                                animationSpec = tween(
                                    300,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        },
                        launch {
                            rotation.animateTo(
                                targetValue = peekRotation,
                                animationSpec = tween(
                                    300,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    )
                    peekJobs.forEach { it.join() }

                    // Return with spring animation
                    val returnJobs = listOf(
                        launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        },
                        launch {
                            rotation.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    )
                    returnJobs.forEach { it.join() }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotation.value)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val targetX = if (offsetX.value > 0) 1500f else -1500f
                            if (abs(offsetX.value) > 300) {
                                // Swipe out with animation
                                launch {
                                    offsetX.animateTo(
                                        targetValue = targetX,
                                        animationSpec = tween(
                                            durationMillis = 200,
                                            easing = FastOutLinearInEasing
                                        )
                                    )
                                    if (targetX > 0) onSwipeRight() else onSwipeLeft()
                                }
                                launch {
                                    rotation.animateTo(
                                        targetValue = targetX / 20f,
                                        animationSpec = tween(
                                            durationMillis = 200,
                                            easing = FastOutLinearInEasing
                                        )
                                    )
                                }
                            } else {
                                // Return to center with spring
                                launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                launch {
                                    rotation.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            rotation.snapTo(offsetX.value / 10f)
                        }
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = null
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SixtOrange.copy(alpha = 0.9f),
                                SixtOrange.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Large Chat Icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Main Heading
                    Text(
                        text = "Not sure what to choose?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subheading
                    Text(
                        text = "Chat with our AI assistant to find the perfect car for your needs",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center
                    )
                }

                // Swipe feedback overlays
                if (offsetX.value > 50) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Green.copy(alpha = (offsetX.value / 300f).coerceIn(0f, 0.3f)))
                    )
                } else if (offsetX.value < -50) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = (-offsetX.value / 300f).coerceIn(0f, 0.3f)))
                    )
                }
            }
        }
    }
}
