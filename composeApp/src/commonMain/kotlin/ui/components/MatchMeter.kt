package ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MatchMeter(
    matchPercentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true
) {
    var targetValue by remember { mutableStateOf(if (animated) 0 else matchPercentage) }
    
    LaunchedEffect(matchPercentage) {
        targetValue = matchPercentage
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = targetValue / 100f,
        animationSpec = if (animated) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            snap()
        }
    )
    
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = if (animated) {
            tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        } else {
            snap()
        }
    )
    
    // Pulsing animation for high matches
    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (matchPercentage >= 80) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth) / 2
            
            // Background circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(
                    (this.size.width - radius * 2) / 2,
                    (this.size.height - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc with gradient
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFEF4444), // Red
                    Color(0xFFFBBF24), // Yellow
                    Color(0xFF10B981)  // Green
                )
            )
            
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(
                    (this.size.width - radius * 2) / 2,
                    (this.size.height - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Percentage text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$animatedValue%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = getMatchColor(animatedValue)
            )
            Text(
                text = "Match",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getMatchColor(percentage: Int): Color {
    return when {
        percentage >= 80 -> Color(0xFF10B981) // Green
        percentage >= 50 -> Color(0xFFFBBF24) // Yellow
        else -> Color(0xFFEF4444) // Red
    }
}
