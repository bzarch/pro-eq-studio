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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioSilverMuted
import kotlin.math.log10

/**
 * Real-Time Fast Fourier Transform (FFT) Spectrum Visualizer and Waveform Scope.
 */
@Composable
fun FftSpectrumVisualizer(
    magnitudesDb: FloatArray,
    waveform: FloatArray,
    showWaveform: Boolean = false,
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

            // Grid: -72 dB, -48 dB, -24 dB, 0 dB
            val dbs = listOf(0f, -18f, -36f, -54f, -72f)
            for (db in dbs) {
                val normY = (0f - db) / 80f
                val y = normY * h
                drawLine(
                    color = Color(0xFF23232E),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.0f
                )
            }

            // Freq grid
            val freqs = listOf(50f, 100f, 250f, 500f, 1000f, 2500f, 5000f, 10000f, 20000f)
            val minLog = log10(20f)
            val maxLog = log10(20000f)
            for (f in freqs) {
                val normX = (log10(f) - minLog) / (maxLog - minLog)
                val x = normX * w
                drawLine(
                    color = Color(0xFF20202A),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.0f
                )
            }

            if (!showWaveform && magnitudesDb.isNotEmpty()) {
                // Plot FFT Spectrum (Logarithmic frequency mapping)
                val binCount = magnitudesDb.size
                val path = Path()
                val fillPath = Path()
                var first = true

                fillPath.moveTo(0f, h)

                val sampleRate = 48000f
                val nyquist = sampleRate / 2.0f
                val numPlotPoints = 96

                for (p in 0 until numPlotPoints) {
                    val normFrac = p.toFloat() / (numPlotPoints - 1)
                    val freq = Math.pow(10.0, (minLog + normFrac * (maxLog - minLog)).toDouble()).toFloat()
                    val binIdx = ((freq / nyquist) * binCount).toInt().coerceIn(0, binCount - 1)
                    val magDb = magnitudesDb[binIdx].coerceIn(-80f, 6f)

                    val x = normFrac * w
                    val y = ((0f - magDb) / 80.0f).coerceIn(0f, 1f) * h

                    if (first) {
                        path.moveTo(x, y)
                        fillPath.lineTo(x, y)
                        first = false
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                fillPath.lineTo(w, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    color = CyanAccent.copy(alpha = 0.22f)
                )
                drawPath(
                    path = path,
                    color = CyanAccent,
                    style = Stroke(width = 2.0f)
                )
            } else if (showWaveform && waveform.isNotEmpty()) {
                // Waveform oscilloscope mode
                val midY = h / 2.0f
                val path = Path()
                for (i in waveform.indices) {
                    val x = (i.toFloat() / (waveform.size - 1)) * w
                    val y = midY - waveform[i].coerceIn(-1f, 1f) * (h * 0.45f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = MaroonPrimaryLight,
                    style = Stroke(width = 2.0f)
                )
            }
        }

        Text(
            text = if (showWaveform) "REAL-TIME OSCILLOSCOPE" else "1024-POINT FFT SPECTRUM ANALYZER",
            color = StudioSilverMuted.copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
