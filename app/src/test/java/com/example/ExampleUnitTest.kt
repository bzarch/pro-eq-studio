package com.example

import com.example.dsp.BiquadFilter
import com.example.dsp.Compressor
import com.example.dsp.FilterType
import com.example.dsp.GraphicEq
import com.example.dsp.GraphicEqBandCount
import com.example.dsp.Limiter
import com.example.dsp.RealFft
import com.example.dsp.RoutingMode
import com.example.dsp.StereoProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DspUnitTest {

    @Test
    fun testBiquadPeakingCoefficients() {
        val filter = BiquadFilter()
        filter.configure(FilterType.PEAK, 1000f, 6.0f, 1.414f, 48000)

        // Evaluate magnitude at center frequency (1000 Hz) should be approx +6 dB
        val gainAtCenter = filter.evaluateGainAt(1000f, 48000)
        assertTrue("Gain at center freq should be around 6.0 dB, got: $gainAtCenter", abs(gainAtCenter - 6.0f) < 0.5f)

        // Evaluate far away should be approx 0 dB
        val gainAtFar = filter.evaluateGainAt(50f, 48000)
        assertTrue("Gain far from center should be near 0 dB, got: $gainAtFar", abs(gainAtFar) < 0.5f)
    }

    @Test
    fun testGraphicEqBands() {
        val eq = GraphicEq()
        eq.bandMode = GraphicEqBandCount.BANDS_10
        eq.updateFilters(48000)

        // Boost 1 kHz band to +12 dB
        val idx1k = eq.isoFrequencies10.indexOfFirst { it == 1000f }
        assertTrue(idx1k != -1)
        eq.setBandGain(idx1k, 12.0f, 48000)

        val totalGainAt1k = eq.evaluateGainAt(1000f, 48000)
        assertTrue("Total gain at 1kHz should be boosted near 12dB, got $totalGainAt1k", totalGainAt1k > 8.0f)
    }

    @Test
    fun testSafeMonoSumming() {
        val processor = StereoProcessor()
        processor.mode = RoutingMode.MONO

        // Feeding 1.0 on L and 1.0 on R with safe -3dB (0.7071) summing
        val out = processor.process(1.0f, 1.0f)
        val expected = (1.0f + 1.0f) * 0.70710678f // ~1.414f
        assertTrue("L and R should be identical in Mono", out.first == out.second)
        assertTrue("Mono sum should be correctly scaled", abs(out.first - expected) < 0.01f)
    }

    @Test
    fun testStereoWidthZeroIsMono() {
        val processor = StereoProcessor()
        processor.mode = RoutingMode.STEREO
        processor.stereoWidth = 0.0f // Pure mono collapse

        val out = processor.process(1.0f, 0.0f)
        // Mid is 0.5, Side is 0.5 * 0 = 0 -> Both L and R become 0.5
        assertEquals(0.5f, out.first, 0.01f)
        assertEquals(0.5f, out.second, 0.01f)
    }

    @Test
    fun testCompressorReduction() {
        val comp = Compressor()
        comp.isEnabled = true
        comp.thresholdDb = -20f
        comp.ratio = 4.0f
        comp.attackMs = 1.0f // very fast
        comp.releaseMs = 100f

        // Feed strong signal of 1.0 (0 dBFS) for 1000 samples to let attack settle
        var lastOutL = 0f
        for (i in 0 until 1000) {
            val res = comp.process(1.0f, 1.0f, 48000)
            lastOutL = res.first
        }
        assertTrue("Compressor should reduce gain on high signal, output: $lastOutL", lastOutL < 0.7f)
        assertTrue("Gain reduction should be detected", comp.gainReductionDb > 0.5f)
    }

    @Test
    fun testLimiterCeiling() {
        val limiter = Limiter()
        limiter.isEnabled = true
        limiter.ceilingDb = -0.1f // max linear approx 0.988

        // Feed excessive signal 3.0
        val out = limiter.process(3.0f, 3.0f, 48000)
        assertTrue("Limiter must clamp below ceiling", out.first <= 0.99f && out.second <= 0.99f)
    }

    @Test
    fun testRealFft() {
        val fft = RealFft(512)
        val input = FloatArray(512)
        // Generate pure 1 kHz sine at 48000 Hz sample rate
        for (i in 0 until 512) {
            input[i] = kotlin.math.sin(2.0 * Math.PI * 1000.0 * i / 48000.0).toFloat()
        }
        fft.compute(input)

        // Peak bin should correspond to ~1000 Hz (Bin freq = bin * 48000 / 512 = bin * 93.75 Hz)
        // 1000 / 93.75 ~ bin 10 or 11
        var maxBin = 0
        var maxMag = -999f
        for (i in 0 until 256) {
            if (fft.magnitudesDb[i] > maxMag) {
                maxMag = fft.magnitudesDb[i]
                maxBin = i
            }
        }
        assertTrue("Max FFT bin should be around bin 10 or 11 for 1kHz sine, found $maxBin", maxBin in 9..12)
    }
}
