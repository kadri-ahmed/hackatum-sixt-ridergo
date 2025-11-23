package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dto.Deal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.theme.SixtOrange
import kotlin.math.roundToInt

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun SwipeableVehicleCard(
    deal: Deal,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    shouldAnimate: Boolean = true,
    modifier: Modifier = Modifier
) {
    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val rotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Random "jumpy" animation to indicate swipeability
    androidx.compose.runtime.LaunchedEffect(shouldAnimate) {
        if (!shouldAnimate) return@LaunchedEffect
        while (true) {
            // Wait for a random interval between 5 and 10 seconds
            kotlinx.coroutines.delay(kotlin.random.Random.nextLong(5000, 10000))
            
            // Only animate if not currently being dragged
            if (offsetX.value == 0f) {
                launch {
                    val peekDirection = if (kotlin.random.Random.nextBoolean()) 1f else -1f
                    val peekDistance = 50f * peekDirection
                    val peekRotation = 3f * peekDirection

                    // Peek out
                    val peekJobs = listOf(
                        launch {
                            offsetX.animateTo(
                                targetValue = peekDistance,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            )
                        },
                        launch {
                            rotation.animateTo(
                                targetValue = peekRotation,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            )
                        }
                    )
                    peekJobs.forEach { it.join() }

                    // Return
                    val returnJobs = listOf(
                        launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow)
                            )
                        },
                        launch {
                            rotation.animateTo(
                                targetValue = 0f,
                                animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow)
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
                            if (kotlin.math.abs(offsetX.value) > 300) {
                                // Swipe out
                                launch {
                                    offsetX.animateTo(
                                        targetValue = targetX,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = 200,
                                            easing = androidx.compose.animation.core.FastOutLinearInEasing
                                        )
                                    )
                                    if (targetX > 0) onSwipeRight() else onSwipeLeft()
                                }
                                launch {
                                    rotation.animateTo(
                                        targetValue = targetX / 20f,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = 200,
                                            easing = androidx.compose.animation.core.FastOutLinearInEasing
                                        )
                                    )
                                }
                            } else {
                                // Snap back
                                launch { offsetX.animateTo(0f, androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)) }
                                launch { offsetY.animateTo(0f, androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)) }
                                launch { rotation.animateTo(0f, androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium)) }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            rotation.snapTo(offsetX.value / 15f)
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
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {

            Box(modifier = Modifier.fillMaxSize()) {
                // Vehicle Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Gray)
                ) {
                    if (deal.vehicle.images.isNotEmpty()) {
                        AsyncImage(
                            model = deal.vehicle.images.first(),
                            contentDescription = "${deal.vehicle.brand} ${deal.vehicle.model}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    startY = 300f
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    // Brand & Model
                    Text(
                        text = deal.vehicle.brand,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deal.vehicle.model,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Features
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FeatureChip(
                            icon = Icons.Default.Person,
                            text = "${deal.vehicle.passengersCount}"
                        )
                        FeatureChip(icon = Icons.Default.Star, text = "${deal.vehicle.bagsCount}")
                        Text(
                            "More",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price & Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "+ ${deal.pricing.displayPrice.currency}${deal.pricing.displayPrice.amount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "/day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(SixtOrange, RoundedCornerShape(50))
                                .padding(horizontal = 24.dp, vertical = 12.dp),

                            ) {
                            Text(
                                text = "Select",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                // Top Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (deal.vehicle.isRecommended) {
                        Box(
                            modifier = Modifier
                                .background(SixtOrange, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { onSwipeRight }
                        ) {
                            Text(
                                text = "Recommended",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }


                // Swipe Overlay
                if (offsetX.value != 0f) {
                    val isRight = offsetX.value > 0
                    val color = if (isRight) SixtOrange else Color.Red
                    val alpha = (kotlin.math.abs(offsetX.value) / 600f).coerceIn(0f, 0.5f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}
@Composable
fun FeatureChip(icon: ImageVector, text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
