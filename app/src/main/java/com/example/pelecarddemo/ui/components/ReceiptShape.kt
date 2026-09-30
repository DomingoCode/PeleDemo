package com.example.pelecarddemo.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A card with rounded top corners and a torn-paper (zig-zag) bottom edge, like a receipt.
 * [toothWidth]/[toothDepth] describe each tooth of the zig-zag.
 */
class ReceiptShape(
    private val cornerRadius: Dp,
    private val toothWidth: Dp,
    private val toothDepth: Dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { cornerRadius.toPx() }.coerceAtMost(size.height / 2f)
        val toothWidthPx = with(density) { toothWidth.toPx() }
        val toothDepthPx = with(density) { toothDepth.toPx() }
        val width = size.width
        val bottomY = size.height
        val topOfTeethY = size.height - toothDepthPx

        val toothCount = max(1, (width / toothWidthPx).roundToInt())
        val segments = toothCount * 2
        val segmentWidth = width / segments

        val path = Path().apply {
            moveTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            lineTo(width - r, 0f)
            arcTo(Rect(width - 2 * r, 0f, width, 2 * r), 270f, 90f, false)
            lineTo(width, topOfTeethY)
            for (s in 1..segments) {
                val x = width - s * segmentWidth
                val y = if (s % 2 == 1) bottomY else topOfTeethY
                lineTo(x, y)
            }
            lineTo(0f, r)
            close()
        }
        return Outline.Generic(path)
    }
}
