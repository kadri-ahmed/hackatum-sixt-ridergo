package ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Gets the platform-specific logo resource.
 * Android uses PNG (cleveride_android.png), iOS uses SVG (cleveride.svg).
 */
@Composable
expect fun getLogoPainter(): Painter
