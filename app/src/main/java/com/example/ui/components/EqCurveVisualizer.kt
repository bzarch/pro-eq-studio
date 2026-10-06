package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioSilverMuted
import kotlin.math.log10
import kotlin.math.pow

/**
 * Real-Time Dynamic Frequency Curve Visualizer for Graphic EQ and Parametric EQ.
 * Renders the accurate mathematical transfer function H(f) evaluated directly from active DSP biquad coefficients.
 */
@Composable
fun EqCurveVisualizer(
    evaluateGainAt: (Float) -> Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(StudioCardBgElevated, RoundedCornerShape(8.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h / 2.0f

            // 1. Grid Lines (Gain: +15, +10, +5, 0, -5, -10, -15 dB)
            val dbSteps = listOf(15f, 10f, 5f, 0f, -5f, -10f, -15f)
            for (db in dbSteps) {
                // Map dB (-18 to +18 range) to Y
                val y = midY - (db / 18.0f) * midY
                val isZero = db == 0f
                drawLine(
                    color = if (isZero) Color(0xFF6E6E82) else Color(0xFF23232E),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (isZero) 1.5f else 1.0f
                )
            }

            // Frequency Grid Lines (ISO Log scale: 50, 100, 250, 500, 1k, 2k, 4k, 8k, 16kHz)
            val freqGrid = listOf(50f, 100f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
            val minLog = log10(20f)
            val maxLog = log10(20000f)
            for (f in freqGrid) {
                val normX = (log10(f) - minLog) / (maxLog - minLog)
                val x = normX * w
                drawLine(
                    color = Color(0xFF23232E),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.0f
                )
            }

            // 2. Plot True DSP Frequency Response Curve
            val path = Path()
            val fillPath = Path()
            val points = 120
            var first = true

            fillPath.moveTo(0f, midY)

            for (i in 0..points) {
                val normX = i.toFloat() / points
                val logFreq = minLog + normX * (maxLog - minLog)
                val freq = 10.0.pow(logFreq.toDouble()).toFloat()
                val gainDb = evaluateGainAt(freq).coerceIn(-18f, 18f)

                val x = normX * w
                val y = midY - (gainDb / 18.0f) * midY

                if (first) {
                    path.moveTo(x, y)
                    fillPath.lineTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(w, midY)
            fillPath.close()

            // Draw Area Fill under response curve
            drawPath(
                path = fillPath,
                color = MaroonPrimaryLight.copy(alpha = 0.18f)
            )

            // Draw Curve Stroke
            drawPath(
                path = path,
                color = MaroonPrimaryLight,
                style = Stroke(width = 2.5f)
            )
        }

        // Overlay Labels for Quick Reference
        Text(
            text = "+15 dB",
            color = StudioSilverMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            text = "0 dB",
            color = StudioSilverMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "-15 dB",
            color = StudioSilverMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart)
        )
        Text(
            text = "REAL-TIME DSP TRANSFER FUNCTION",
            color = StudioSilverMuted.copy(alpha = 0.5f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
