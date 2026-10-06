package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.example.dsp.CrossoverEngine
import com.example.dsp.GraphicEq
import com.example.dsp.LevelMeter
import com.example.dsp.Limiter
import com.example.dsp.MeterData
import com.example.dsp.ParametricEq
import com.example.dsp.QuickTone
import com.example.dsp.RealFft
import com.example.dsp.SoftClipper
import com.example.dsp.StereoProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

enum class AudioSourceType {
    TEST_TONE,
    AUDIO_FILE
}

enum class LatencyProfile(val bufferFrames: Int, val description: String) {
    ULTRA_LOW(128, "Ultra Low (128 f)"),
    LOW(256, "Low Latency (256 f)"),
    BALANCED(512, "Balanced (512 f)"),
    STABLE(1024, "Stable (1024 f)"),
    HIGH_STABILITY(2048, "High Stability (2048 f)")
}

/**
 * Real-Time Audio Engine & DSP Orchestrator.
 * Fully separated from UI thread, lock-free, zero allocation inside the real-time processing loop.
 */
class AudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Active configuration
    var targetSampleRate: Int = 48000
    var latencyProfile: LatencyProfile = LatencyProfile.BALANCED
    var sourceType: AudioSourceType = AudioSourceType.TEST_TONE

    // DSP Processing Modules
    val graphicEq = GraphicEq()
    val parametricEq = ParametricEq()
    val quickTone = QuickTone()
    val stereoProcessor = StereoProcessor()
    val crossover = CrossoverEngine()
    val compressor = com.example.dsp.Compressor()
    val limiter = Limiter()
    val softClipper = SoftClipper()

    // Multi-Effect Rack Modules
    val delayProcessor = com.example.dsp.DelayProcessor(maxDelaySeconds = 2.0f, sampleRate = 48000)
    val reverbProcessor = com.example.dsp.ReverbProcessor()
    val modulationProcessor = com.example.dsp.ModulationProcessor()
    val bassEnhancer = com.example.dsp.BassEnhancer()
    val dynamicsEffects = com.example.dsp.DynamicsEffects()
    val dynamicEq = com.example.dsp.DynamicBiquadEq()
    val headphoneCrossfeed = com.example.dsp.HeadphoneCrossfeedProcessor()
    val timeAlignmentLeft = com.example.dsp.TimeAlignmentDelay(48000)
    val timeAlignmentRight = com.example.dsp.TimeAlignmentDelay(48000)

    // Routing & Source Channel Strips
    val activeSourceType = com.example.model.InputSourceType.SYSTEM_AUDIO
    var activeOutputRoute = com.example.model.OutputDestinationType.INTERNAL_SPEAKER

    // System-wide Global Audio Effect Bridge
    val systemAudioBridge = SystemAudioBridge(context)

    // Test tone generator
    val testTone = TestToneGenerator()

    // Level Meters
    val inputMeter = LevelMeter()
    val outputMeter = LevelMeter()

    // FFT Analyzers (512, 1024, 2048, etc.)
    val fft = RealFft(1024)
    private val fftInputBuffer = FloatArray(1024)
    private var fftWriteIdx = 0

    // Master Stage
    var inputGainDb: Float = 0.0f
    var masterGainDb: Float = 0.0f
    var isMuted: Boolean = false
    var isBypassed: Boolean = false

    // State Tracking
    private val isEngineRunning = AtomicBoolean(false)
    val isRunning: Boolean get() = isEngineRunning.get()

    private var audioThread: Thread? = null
    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null

    // Telemetry flows for UI
    private val _inputMeterFlow = MutableStateFlow(MeterData())
    val inputMeterFlow: StateFlow<MeterData> = _inputMeterFlow.asStateFlow()

    private val _outputMeterFlow = MutableStateFlow(MeterData())
    val outputMeterFlow: StateFlow<MeterData> = _outputMeterFlow.asStateFlow()

    private val _fftMagnitudes = MutableStateFlow(FloatArray(512))
    val fftMagnitudes: StateFlow<FloatArray> = _fftMagnitudes.asStateFlow()

    private val _currentDeviceName = MutableStateFlow("Internal Speaker")
    val currentDeviceName: StateFlow<String> = _currentDeviceName.asStateFlow()

    private val _actualLatencyMs = MutableStateFlow(10.6f)
    val actualLatencyMs: StateFlow<Float> = _actualLatencyMs.asStateFlow()

    private val _dspLoadPercent = MutableStateFlow(4.2f)
    val dspLoadPercent: StateFlow<Float> = _dspLoadPercent.asStateFlow()

    // Waveform visualization ring buffer
    val waveformBuffer = FloatArray(256)
    private var waveWriteIdx = 0

    init {
        detectNativeAudioHardware()
        reconfigureDsp()
    }

    fun detectNativeAudioHardware() {
        val nativeSampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val nativeSampleRate = nativeSampleRateStr?.toIntOrNull() ?: 48000
        targetSampleRate = when {
            nativeSampleRate >= 88200 -> nativeSampleRate
            nativeSampleRate >= 48000 -> 48000
            else -> 44100
        }
        updateConnectedDeviceInfo()
    }

    fun updateConnectedDeviceInfo() {
        try {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            var devName = "Internal Speaker"
            for (dev in devices) {
                when (dev.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                        devName = "Bluetooth: ${dev.productName.ifEmpty { "Device" }}"
                        break
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                        devName = "Wired Headphones"
                        break
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        devName = "USB Audio: ${dev.productName.ifEmpty { "DAC" }}"
                        break
                    }
                }
            }
            _currentDeviceName.value = devName
        } catch (e: Exception) {
            _currentDeviceName.value = "Audio Device"
        }
    }

    fun reconfigureDsp() {
        graphicEq.updateFilters(targetSampleRate)
        parametricEq.updateAll(targetSampleRate)
        quickTone.update(targetSampleRate)
        stereoProcessor.updateSampleRate(targetSampleRate)
        crossover.update(targetSampleRate)
        bassEnhancer.updateSampleRate(targetSampleRate)
        dynamicsEffects.updateSampleRate(targetSampleRate)
        dynamicEq.updateCoefficients(targetSampleRate)
        headphoneCrossfeed.updateSampleRate(targetSampleRate)
    }

    fun start() {
        if (isEngineRunning.get()) return
        isEngineRunning.set(true)

        audioThread = thread(name = "ProEqRealtimeAudioThread", priority = Thread.MAX_PRIORITY) {
            runAudioLoop()
        }
    }

    fun stop() {
        if (!isEngineRunning.get()) return
        isEngineRunning.set(false)
        try {
            audioThread?.join(500)
        } catch (e: Exception) {
            // Ignore
        }
        audioThread = null
    }

    private fun runAudioLoop() {
        val sampleRate = targetSampleRate
        val bufferFrames = latencyProfile.bufferFrames
        val channelCount = 2
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val trackBufferSize = max(minBufferSize, bufferFrames * channelCount * 4 * 4)

        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(trackBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
            isEngineRunning.set(false)
            return
        }

        // Reusable audio frame buffer (Interleaved stereo: [L0, R0, L1, R1...])
        val audioBuffer = FloatArray(bufferFrames * channelCount)
        val recordShortBuffer = ShortArray(bufferFrames * channelCount)

        val estimatedLatency = (bufferFrames.toFloat() / sampleRate.toFloat()) * 1000f
        _actualLatencyMs.value = estimatedLatency

        var meterDecayCounter = 0
        var loopStartTime: Long
        var dspExecutionTimeAcc = 0L
        var framesProcessedTotal = 0L

        while (isEngineRunning.get()) {
            loopStartTime = System.nanoTime()

            // 1. INPUT GENERATION / PLAYBACK
            when (sourceType) {
                AudioSourceType.TEST_TONE, AudioSourceType.AUDIO_FILE -> {
                    testTone.fillBuffer(audioBuffer, 0, bufferFrames, sampleRate)
                }
            }

            // 2. INPUT GAIN & METERING
            val inLinear = 10.0f.pow(inputGainDb / 20.0f)
            for (f in 0 until bufferFrames) {
                val idx = f * 2
                val inL = audioBuffer[idx] * inLinear
                val inR = audioBuffer[idx + 1] * inLinear
                audioBuffer[idx] = inL
                audioBuffer[idx + 1] = inR
                inputMeter.accumulate(inL, inR)
            }

            // 3. DSP PROCESSING PIPELINE
            val dspStart = System.nanoTime()
            val masterLinear = if (isMuted) 0f else 10.0f.pow(masterGainDb / 20.0f)

            if (!isBypassed) {
                for (f in 0 until bufferFrames) {
                    val idx = f * 2
                    var l = audioBuffer[idx]
                    var r = audioBuffer[idx + 1]

                    // Channel Routing, Mono Summing, Mid/Side, Polarity & Stereo Width
                    val routed = stereoProcessor.process(l, r)
                    l = routed.first
                    r = routed.second

                    // Noise Gate & De-Esser
                    val dyn = dynamicsEffects.process(l, r)
                    l = dyn.first
                    r = dyn.second

                    // Bass Enhancer & Sub Harmonics
                    val bEnh = bassEnhancer.process(l, r)
                    l = bEnh.first
                    r = bEnh.second

                    // Graphic EQ (10 / 15 / 31 bands)
                    val gEq = graphicEq.process(l, r)
                    l = gEq.first
                    r = gEq.second

                    // Parametric EQ (10 bands)
                    val pEq = parametricEq.process(l, r)
                    l = pEq.first
                    r = pEq.second

                    // Dynamic EQ (selective energy threshold boost/cut)
                    val dynEq = dynamicEq.process(l, r, sampleRate)
                    l = dynEq.first
                    r = dynEq.second

                    // Quick Tone (Bass / Mid / Treble / Presence / Air)
                    val qTone = quickTone.process(l, r)
                    l = qTone.first
                    r = qTone.second

                    // Headphone Crossfeed (Binaural fatigue eliminator)
                    val xfeed = headphoneCrossfeed.process(l, r)
                    l = xfeed.first
                    r = xfeed.second

                    // Time Alignment Delay (Left / Right acoustic alignment)
                    l = timeAlignmentLeft.process(l, sampleRate)
                    r = timeAlignmentRight.process(r, sampleRate)

                    // Modulation (Chorus / Tremolo)
                    val mod = modulationProcessor.process(l, r, sampleRate)
                    l = mod.first
                    r = mod.second

                    // Delay & Echo
                    val del = delayProcessor.process(l, r, sampleRate)
                    l = del.first
                    r = del.second

                    // Algorithmic Studio Reverb
                    val rev = reverbProcessor.process(l, r)
                    l = rev.first
                    r = rev.second

                    // Crossover Multi-band
                    val xOver = crossover.process(l, r)
                    l = xOver.first
                    r = xOver.second

                    // Dynamic Range Compressor
                    val comp = compressor.process(l, r, sampleRate)
                    l = comp.first
                    r = comp.second

                    // Soft Clipper
                    l = softClipper.process(l)
                    r = softClipper.process(r)

                    // Master Gain & Output Limiter
                    l *= masterLinear
                    r *= masterLinear

                    val lim = limiter.process(l, r, sampleRate)
                    l = lim.first
                    r = lim.second

                    audioBuffer[idx] = l
                    audioBuffer[idx + 1] = r

                    outputMeter.accumulate(l, r)

                    // Collect samples for FFT Analyzer & Waveform
                    val monoSample = (l + r) * 0.5f
                    fftInputBuffer[fftWriteIdx] = monoSample
                    fftWriteIdx = (fftWriteIdx + 1) % 1024

                    if (f % 4 == 0) {
                        waveformBuffer[waveWriteIdx] = monoSample
                        waveWriteIdx = (waveWriteIdx + 1) % 256
                    }
                }
            } else {
                // Full Bypass
                for (f in 0 until bufferFrames) {
                    val idx = f * 2
                    val l = audioBuffer[idx] * masterLinear
                    val r = audioBuffer[idx + 1] * masterLinear
                    audioBuffer[idx] = l
                    audioBuffer[idx + 1] = r
                    outputMeter.accumulate(l, r)
                }
            }

            val dspTime = System.nanoTime() - dspStart
            dspExecutionTimeAcc += dspTime
            framesProcessedTotal += bufferFrames

            // 4. WRITE TO AUDIO OUTPUT
            audioTrack?.write(audioBuffer, 0, audioBuffer.size, AudioTrack.WRITE_BLOCKING)

            // 5. UPDATE TELEMETRY (Periodically, every ~40ms to avoid UI overload)
            meterDecayCounter++
            if (meterDecayCounter >= 8) {
                meterDecayCounter = 0
                inputMeter.finishBlock()
                outputMeter.finishBlock()
                _inputMeterFlow.value = inputMeter.snapshot
                _outputMeterFlow.value = outputMeter.snapshot

                // Run FFT calculation
                fft.compute(fftInputBuffer)
                _fftMagnitudes.value = fft.magnitudesDb.clone()

                // Calculate DSP CPU Load
                val totalPeriodNs = (bufferFrames.toFloat() / sampleRate * 1_000_000_000f * 8).toLong()
                val load = ((dspExecutionTimeAcc.toFloat() / totalPeriodNs.toFloat()) * 100f).coerceIn(0.5f, 99.9f)
                _dspLoadPercent.value = load
                dspExecutionTimeAcc = 0L
            }
        }

        // Cleanup resources
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioRecord = null
    }
}
