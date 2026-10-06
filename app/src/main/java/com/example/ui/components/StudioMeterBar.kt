package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.MeterData
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioSilverMuted

/**
 * Professional Studio Audio VU / Peak & RMS Meter with clip indicator.
 */
@Composable
fun StudioMeterBar(
    title: String,
    meterData: MeterData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(StudioCardBgElevated, RoundedCornerShape(6.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = StudioSilverMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            // Clip LED Indicator
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(6.dp)
                    .background(
                        if (meterData.isClipping) MeterRed else Color(0xFF331111),
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SingleChannelMeter(
                channelLabel = "L",
                peakLinear = meterData.peakL,
                rmsLinear = meterData.rmsL,
                peakDb = meterData.peakDbL,
                modifier = Modifier.weight(1f)
            )
            SingleChannelMeter(
                channelLabel = "R",
                peakLinear = meterData.peakR,
                rmsLinear = meterData.rmsR,
                peakDb = meterData.peakDbR,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SingleChannelMeter(
    channelLabel: String,
    peakLinear: Float,
    rmsLinear: Float,
    peakDb: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFF0A0A0D), RoundedCornerShape(3.dp))
                .border(0.5.dp, Color(0xFF22222E), RoundedCornerShape(3.dp))
        ) {
            val w = size.width
            val h = size.height

            // Segments: 0% to 70% Green, 70% to 88% Yellow, 88% to 100% Red
            val pWidth = (peakLinear.coerceIn(0f, 1f)) * w
            val rWidth = (rmsLinear.coerceIn(0f, 1f)) * w

            // Draw RMS block
            drawRect(
                color = when {
                    rmsLinear > 0.88f -> MeterRed.copy(alpha = 0.5f)
                    rmsLinear > 0.70f -> MeterYellow.copy(alpha = 0.5f)
                    else -> MeterGreen.copy(alpha = 0.5f)
                },
                size = Size(rWidth, h)
            )

            // Draw Peak bar
            drawRect(
                color = when {
                    peakLinear >= 0.98f -> MeterRed
                    peakLinear > 0.70f -> MeterYellow
                    else -> MeterGreen
                },
                size = Size(pWidth, h)
            )

            // 0 dB mark tick
            val markX = w * 0.92f
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(markX, 0f),
                end = Offset(markX, h),
                strokeWidth = 1.5f
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = channelLabel,
                color = StudioSilverMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${"%.1f".format(peakDb)} dB",
                color = if (peakDb >= -0.5f) MeterRed else StudioSilverMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
