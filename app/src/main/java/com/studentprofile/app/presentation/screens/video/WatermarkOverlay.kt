package com.studentprofile.app.presentation.screens.video

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Draws the watermark text diagonally across the video surface in a repeating grid.
 * Must be hosted inside PlayerView's view hierarchy (not as a Compose sibling of
 * AndroidView) so it renders above the SurfaceView's hardware surface.
 *
 * [isMoving] mirrors the superadmin-configured `position` field ("moving" vs "static") —
 * when true the grid jumps to a new random offset every few seconds (unpredictable, so it
 * can't be reliably cropped out of a screen recording); when false it stays fixed.
 */
@Composable
fun WatermarkOverlay(
    text: String,
    opacity: Float = 0.5f,
    color: String = "#FFFFFF",
    fontSize: Int = 24,
    isMoving: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return

    val fractionX = remember { Animatable(Random.nextFloat()) }
    val fractionY = remember { Animatable(Random.nextFloat()) }

    LaunchedEffect(isMoving) {
        if (!isMoving) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(2500, 4500))
            fractionX.animateTo(
                Random.nextFloat(),
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }
    LaunchedEffect(isMoving) {
        if (!isMoving) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(2500, 4500))
            fractionY.animateTo(
                Random.nextFloat(),
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(modifier = modifier) {
        val parsedColor = runCatching {
            val hex = color.trimStart('#')
            val argb = when (hex.length) {
                6 -> android.graphics.Color.parseColor("#$hex")
                8 -> android.graphics.Color.parseColor("#$hex")
                else -> android.graphics.Color.WHITE
            }
            val alpha = (opacity.coerceIn(0.05f, 0.95f) * 255).toInt()
            (argb and 0x00FFFFFF) or (alpha shl 24)
        }.getOrElse {
            Color.White.copy(alpha = opacity.coerceIn(0.05f, 0.95f)).toArgb()
        }

        val paint = Paint().apply {
            isAntiAlias = true
            textSize = fontSize.toFloat() * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            this.color = parsedColor
        }

        val textWidth = paint.measureText(text)
        val stepX = textWidth + 120f
        val stepY = fontSize.toFloat() * density * 3.5f

        // Offset within one tile step — since the grid is periodic, any offset in
        // [0, step) loops seamlessly with no visible gap at the canvas edge.
        val offsetX = if (isMoving) fractionX.value * stepX else 0f
        val offsetY = if (isMoving) fractionY.value * stepY else 0f

        rotate(degrees = -30f, pivot = Offset(size.width / 2f, size.height / 2f)) {
            translate(left = offsetX, top = offsetY) {
                var y = -size.height - stepY
                while (y < size.height * 2f + stepY) {
                    var x = -size.width - stepX
                    while (x < size.width * 2f + stepX) {
                        drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
                        x += stepX
                    }
                    y += stepY
                }
            }
        }
    }
}
