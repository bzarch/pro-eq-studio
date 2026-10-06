package com.example.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance Radix-2 Cooley-Tukey in-place FFT algorithm.
 * Optimized with precomputed twiddle factors and bit-reversal tables.
 * Lock-free and allocation-free in the processing loop.
 */
class RealFft(val size: Int) {
    init {
        require(size > 0 && (size and (size - 1)) == 0) { "FFT size must be a power of 2: $size" }
    }

    private val bitRev = IntArray(size)
    private val cosTable = FloatArray(size / 2)
    private val sinTable = FloatArray(size / 2)
    private val hanningWindow = FloatArray(size)

    // Reusable internal buffers
    val real = FloatArray(size)
    val imag = FloatArray(size)
    val magnitudesDb = FloatArray(size / 2)

    init {
        // Precompute bit-reversal indices
        var j = 0
        for (i in 0 until size - 1) {
            bitRev[i] = j
            var k = size shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }
        bitRev[size - 1] = size - 1

        // Precompute twiddle factors and Hanning window
        val half = size / 2
        for (i in 0 until half) {
            val angle = -2.0 * PI * i / size
            cosTable[i] = cos(angle).toFloat()
            sinTable[i] = sin(angle).toFloat()
        }
        for (i in 0 until size) {
            hanningWindow[i] = (0.5 * (1.0 - cos(2.0 * PI * i / (size - 1)))).toFloat()
        }
    }

    /**
     * Compute FFT on an input array of samples with windowing applied.
     */
    fun compute(input: FloatArray, offset: Int = 0) {
        val n = size
        for (i in 0 until n) {
            val rev = bitRev[i]
            val s = if (offset + rev < input.size) input[offset + rev] else 0f
            real[i] = s * hanningWindow[rev]
            imag[i] = 0.0f
        }

        // Cooley-Tukey Radix-2 FFT
        var step = 1
        while (step < n) {
            val jump = step shl 1
            val delta = n / jump

            for (group in 0 until step) {
                val tableIdx = group * delta
                val wr = cosTable[tableIdx]
                val wi = sinTable[tableIdx]

                var i = group
                while (i < n) {
                    val match = i + step
                    val tr = wr * real[match] - wi * imag[match]
                    val ti = wr * imag[match] + wi * real[match]

                    real[match] = real[i] - tr
                    imag[match] = imag[i] - ti
                    real[i] += tr
                    imag[i] += ti

                    i += jump
                }
            }
            step = jump
        }

        // Compute magnitude in dB normalized to 0 dBFS
        val half = n / 2
        val norm = 2.0f / n
        for (i in 0 until half) {
            val r = real[i] * norm
            val im = imag[i] * norm
            val mag = kotlin.math.sqrt(r * r + im * im)
            magnitudesDb[i] = (20.0f * kotlin.math.log10(mag.coerceAtLeast(1e-5f))).coerceIn(-90f, 12f)
        }
    }
}
