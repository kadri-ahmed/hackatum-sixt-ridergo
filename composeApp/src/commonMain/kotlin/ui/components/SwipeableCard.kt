package ui.components

import ai.models.VehicleRecommendation
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ui.theme.SixtOrange
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    recommendation: VehicleRecommendation,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val rotation = (offsetX / 20).coerceIn(-15f, 15f)
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = 0.7f)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), offsetY.roundToInt()) }
            .rotate(rotation)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > 300 -> {
                                offsetX = 1000f
                                onSwipeRight()
                            }
                            offsetX < -300 -> {
                                offsetX = -1000f
                                onSwipeLeft()
                            }
                            else -> {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2563EB).copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Match Meter
                MatchMeter(
                    matchPercentage = recommendation.matchPercentage,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Spacer(modifier =Modifier.height(16.dp))
                
                // Vehicle Info
                Text(
                    text = "${recommendation.vehicle.brand} ${recommendation.vehicle.model}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = recommendation.vehicle.groupType.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SixtOrange
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Top Reasons
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Why this car?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    recommendation.reasons.take(3).forEach { reason ->
                        ReasonCard(reason = reason.explanation)
                    }
                }
                
                // Swipe hints
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SwipeHint(
                        icon = Icons.Default.Close,
                        text = "Skip",
                        color = Color.Red,
                        visible = offsetX < -50
                    )
                    SwipeHint(
                        icon = Icons.Default.Favorite,
                        text = "Like",
                        color = Color.Green,
                        visible = offsetX > 50
                    )
                }
            }
        }
    }
}

@Composable
private fun ReasonCard(reason: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SixtOrange.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = SixtOrange,
                modifier = Modifier.size(20.dp)
            )
            Text(
                reason,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SwipeHint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    visible: Boolean
) {
    if (visible) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
