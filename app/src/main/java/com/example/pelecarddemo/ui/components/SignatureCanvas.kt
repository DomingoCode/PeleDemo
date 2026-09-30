package com.example.pelecarddemo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.Signature
import com.example.pelecarddemo.domain.SignaturePoint

/** The pad and the receipt preview use the same shape, so a signature looks identical in both. */
const val SIGNATURE_ASPECT_RATIO = 2f

private val SIGNATURE_STROKE_WIDTH = 3.dp

/** Interactive pad. Stateless: strokes come from the ViewModel, touches are reported back. */
@Composable
fun SignaturePad(
    strokes: List<List<SignaturePoint>>,
    onStrokeStart: (SignaturePoint) -> Unit,
    onStrokePoint: (SignaturePoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnStrokeStart by rememberUpdatedState(onStrokeStart)
    val currentOnStrokePoint by rememberUpdatedState(onStrokePoint)
    val inkColor = MaterialTheme.colorScheme.onSurface
    val strokeWidth = with(LocalDensity.current) { SIGNATURE_STROKE_WIDTH.toPx() }
    val description = stringResource(R.string.signature_pad_description)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> currentOnStrokeStart(offset.normalizedIn(size)) },
                    onDrag = { change, _ ->
                        change.consume()
                        currentOnStrokePoint(change.position.normalizedIn(size))
                    },
                )
            },
    ) {
        drawSignature(strokes, inkColor, strokeWidth)
    }
}

/** Read-only signature, used on the receipt. */
@Composable
fun SignatureView(signature: Signature, modifier: Modifier = Modifier) {
    val inkColor = MaterialTheme.colorScheme.onSurface
    val strokeWidth = with(LocalDensity.current) { SIGNATURE_STROKE_WIDTH.toPx() }
    val description = stringResource(R.string.signature_image_description)

    Canvas(modifier = modifier.semantics { contentDescription = description }) {
        drawSignature(signature.strokes, inkColor, strokeWidth)
    }
}

private fun Offset.normalizedIn(size: IntSize): SignaturePoint = SignaturePoint(
    x = (x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f),
    y = (y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f),
)

private fun DrawScope.drawSignature(
    strokes: List<List<SignaturePoint>>,
    color: Color,
    strokeWidth: Float,
) {
    strokes.forEach { points ->
        if (points.isEmpty()) return@forEach
        val first = points.first().toOffset(size.width, size.height)
        if (points.size == 1) {
            drawCircle(color = color, radius = strokeWidth / 2f, center = first)
            return@forEach
        }
        val path = Path().apply {
            moveTo(first.x, first.y)
            for (index in 1 until points.size) {
                val next = points[index].toOffset(size.width, size.height)
                lineTo(next.x, next.y)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

private fun SignaturePoint.toOffset(width: Float, height: Float) = Offset(x * width, y * height)
