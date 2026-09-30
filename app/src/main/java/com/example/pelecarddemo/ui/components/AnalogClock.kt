package com.example.pelecarddemo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pelecarddemo.R
import com.example.pelecarddemo.domain.ClockTime
import kotlin.math.cos
import kotlin.math.sin

/** Stateless analog clock: it only draws the time it is given. */
@Composable
fun AnalogClock(time: ClockTime, modifier: Modifier = Modifier) {
    val faceColor = MaterialTheme.colorScheme.onSurface
    val secondHandColor = MaterialTheme.colorScheme.secondary
    val description = stringResource(R.string.clock_description, time.formatted())

    Canvas(
        modifier = modifier
            .size(CLOCK_SIZE)
            .semantics { contentDescription = description },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val faceStroke = 3.dp.toPx()
        val radius = size.minDimension / 2f - faceStroke / 2f

        drawCircle(color = faceColor, radius = radius, center = center, style = Stroke(width = faceStroke))

        // Hour marks
        for (mark in 0 until 12) {
            val degrees = mark * 30f
            drawLine(
                color = faceColor,
                start = center + pointOnCircle(degrees, radius * 0.86f),
                end = center + pointOnCircle(degrees, radius * 0.95f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val hourDegrees = ((time.hour % 12) + time.minute / 60f + time.second / 3600f) * 30f
        val minuteDegrees = (time.minute + time.second / 60f) * 6f
        val secondDegrees = time.second * 6f

        drawLine(
            color = faceColor,
            start = center,
            end = center + pointOnCircle(hourDegrees, radius * 0.5f),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = faceColor,
            start = center,
            end = center + pointOnCircle(minuteDegrees, radius * 0.75f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = secondHandColor,
            start = center,
            end = center + pointOnCircle(secondDegrees, radius * 0.85f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(color = secondHandColor, radius = 4.dp.toPx(), center = center)
    }
}

/** Offset from the center for a clock angle: 0 degrees points up, angles grow clockwise. */
private fun pointOnCircle(degrees: Float, length: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(
        x = (sin(radians) * length).toFloat(),
        y = (-cos(radians) * length).toFloat(),
    )
}

private val CLOCK_SIZE = 160.dp
