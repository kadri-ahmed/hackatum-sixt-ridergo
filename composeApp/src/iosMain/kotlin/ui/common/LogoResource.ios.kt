package ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import ridergo.composeapp.generated.resources.Res
import ridergo.composeapp.generated.resources.cleveride

/**
 * iOS implementation: Uses SVG logo
 */
@Composable
actual fun getLogoPainter(): Painter {
    return painterResource(Res.drawable.cleveride)
}
