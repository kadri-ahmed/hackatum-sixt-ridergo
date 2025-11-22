package ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Creates a shimmer loading effect for skeleton screens
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation.value - widthOfShadowBrush, y = 0f),
        end = Offset(x = translateAnimation.value, y = angleOfAxisY)
    )

    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 20.dp
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
    )
}

/**
 * Shimmer placeholder for vehicle cards
 */
@Composable
fun VehicleCardShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Image placeholder
        ShimmerBox(height = 150.dp)
        // Title
        ShimmerBox(height = 24.dp, modifier = Modifier.fillMaxWidth(0.7f))
        // Subtitle
        ShimmerBox(height = 16.dp, modifier = Modifier.fillMaxWidth(0.5f))
        // Price
        ShimmerBox(height = 20.dp, modifier = Modifier.fillMaxWidth(0.4f))
    }
}

/**
 * Shimmer placeholder for protection package cards
 */
@Composable
fun ProtectionCardShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ShimmerBox(height = 28.dp, modifier = Modifier.fillMaxWidth(0.5f))
            ShimmerBox(height = 28.dp, modifier = Modifier.width(80.dp))
        }
        ShimmerBox(height = 16.dp)
        ShimmerBox(height = 16.dp, modifier = Modifier.fillMaxWidth(0.8f))
        ShimmerBox(height = 40.dp)
    }
}
