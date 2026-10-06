package com.example.audio

enum class ToneSignalType {
    SINE,
    PINK_NOISE,
    WHITE_NOISE,
    SWEEP,
    IMPULSE
}

/**
 * High-precision Real-time Audio Signal Generator for calibration and tuning.
 * Generates Pure Sine, Pink Noise (Paul Kellet's filter), White Noise, Logarithmic Sine Sweep, and Dirac Impulse.
 */
class TestToneGenerator {
    var isRunning: Boolean = false
    var signalType: ToneSignalType = ToneSignalType.SINE
    var frequency: Float = 1000f // Hz (20Hz to 20kHz)
    var levelDb: Float = -12f    // Safe calibration default level (-12 dBFS)

    // Generator state
    private var phase: Double = 0.0
    private var sweepPhase: Double = 0.0
    private var sweepCurrentFreq: Double = 20.0
    private var impulseCounter: Int = 0

    // Pink noise state (Paul Kellet's algorithm)
    private var b0 = 0.0f
    private var b1 = 0.0f
    private var b2 = 0.0f
    private var b3 = 0.0f
    private var b4 = 0.0f
    private var b5 = 0.0f
    private var b6 = 0.0f

    // Fast Pseudo-Random XorShift RNG
    private var rngState: Long = 88172645463325252L

    private fun nextFloat(): Float {
        rngState = rngState xor (rngState shl 13)
        rngState = rngState xor (rngState ushr 7)
        rngState = rngState xor (rngState shl 17)
        return ((rngState and 0x7FFFFFFFL).toFloat() / 0x7FFFFFFF.toFloat()) * 2.0f - 1.0f
    }

    fun fillBuffer(buffer: FloatArray, offset: Int, numFrames: Int, sampleRate: Int) {
        if (!isRunning) {
            val end = offset + numFrames * 2
            for (i in offset until end) {
                buffer[i] = 0f
            }
            return
        }

        val amp = Math.pow(10.0, (levelDb / 20.0).toDouble()).toFloat()
        val twoPi = 2.0 * Math.PI
        val phaseInc = twoPi * frequency / sampleRate

        var outIdx = offset
        for (f in 0 until numFrames) {
            val s: Float = when (signalType) {
                ToneSignalType.SINE -> {
                    val sample = kotlin.math.sin(phase).toFloat()
                    phase += phaseInc
                    if (phase >= twoPi) phase -= twoPi
                    sample
                }
                ToneSignalType.WHITE_NOISE -> {
                    nextFloat()
                }
                ToneSignalType.PINK_NOISE -> {
                    val white = nextFloat()
                    b0 = 0.99886f * b0 + white * 0.0555179f
                    b1 = 0.99332f * b1 + white * 0.0750759f
                    b2 = 0.96900f * b2 + white * 0.1538520f
                    b3 = 0.86650f * b3 + white * 0.3104856f
                    b4 = 0.55000f * b4 + white * 0.5329522f
                    b5 = -0.7616f * b5 - white * 0.0168980f
                    val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f
                    b6 = white * 0.115926f
                    (pink * 0.11f) // scaled to approx 0 dB
                }
                ToneSignalType.SWEEP -> {
                    // Logarithmic sweep 20 Hz to 20,000 Hz over 4 seconds
                    val sweepDurationSec = 4.0
                    val sweepRate = Math.pow(20000.0 / 20.0, 1.0 / (sampleRate * sweepDurationSec))
                    sweepCurrentFreq *= sweepRate
                    if (sweepCurrentFreq > 20000.0) {
                        sweepCurrentFreq = 20.0
                        sweepPhase = 0.0
                    }
                    val sample = kotlin.math.sin(sweepPhase).toFloat()
                    sweepPhase += twoPi * sweepCurrentFreq / sampleRate
                    if (sweepPhase >= twoPi) sweepPhase -= twoPi
                    sample
                }
                ToneSignalType.IMPULSE -> {
                    // 1 sample impulse every 1 second (sampleRate frames)
                    val s = if (impulseCounter == 0) 1.0f else 0.0f
                    impulseCounter = (impulseCounter + 1) % sampleRate
                    s
                }
            }

            val frameSample = s * amp
            // Stereo interleaved
            buffer[outIdx++] = frameSample
            buffer[outIdx++] = frameSample
        }
    }
}
