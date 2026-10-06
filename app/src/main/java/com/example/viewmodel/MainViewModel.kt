package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioCapabilityDetector
import com.example.audio.AudioEngine
import com.example.audio.AudioSourceType
import com.example.audio.LatencyProfile
import com.example.audio.PresetStorage
import com.example.audio.ToneSignalType
import com.example.dsp.CrossoverMode
import com.example.dsp.CrossoverSlope
import com.example.dsp.FilterType
import com.example.dsp.GraphicEqBandCount
import com.example.dsp.RoutingMode
import com.example.model.DeviceAudioProfile
import com.example.model.DspModuleInfo
import com.example.model.ModuleType
import com.example.model.PeqBandModel
import com.example.model.StudioPreset
import com.example.service.DspBackgroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val engine = AudioEngine(application.applicationContext)
    val storage = PresetStorage(application.applicationContext)

    // Capability Telemetry
    val deviceProfile: DeviceAudioProfile = AudioCapabilityDetector.detect(application.applicationContext)

    // System DSP State
    private val _isSystemDspActive = MutableStateFlow(false)
    val isSystemDspActive: StateFlow<Boolean> = _isSystemDspActive.asStateFlow()

    // FX Rack Modules List
    private val _rackModules = MutableStateFlow(
        listOf(
            DspModuleInfo("mod_preamp", ModuleType.PREAMP, "01 PREAMP / INPUT GAIN", isEnabled = true),
            DspModuleInfo("mod_gate", ModuleType.NOISE_GATE, "02 NOISE GATE & DE-ESSER", isEnabled = false),
            DspModuleInfo("mod_bass", ModuleType.BASS_PROCESSOR, "03 HARMONIC BASS ENHANCER", isEnabled = false),
            DspModuleInfo("mod_geq", ModuleType.GRAPHIC_EQ, "04 ISO GRAPHIC EQUALIZER", isEnabled = true),
            DspModuleInfo("mod_peq", ModuleType.PARAMETRIC_EQ, "05 10-BAND PARAMETRIC EQ", isEnabled = true),
            DspModuleInfo("mod_tone", ModuleType.MID_PROCESSOR, "06 QUICK TONE CONSOLE", isEnabled = true),
            DspModuleInfo("mod_mod", ModuleType.CHORUS, "07 CHORUS & TREMOLO MODULATION", isEnabled = false),
            DspModuleInfo("mod_delay", ModuleType.DELAY, "08 STEREO PING-PONG DELAY", isEnabled = false),
            DspModuleInfo("mod_reverb", ModuleType.REVERB, "09 ALGORITHMIC STUDIO REVERB", isEnabled = false),
            DspModuleInfo("mod_stereo", ModuleType.STEREO_WIDENER, "10 M/S STEREO & MONO SUMMING", isEnabled = true),
            DspModuleInfo("mod_comp", ModuleType.COMPRESSOR, "11 DYNAMIC RANGE COMPRESSOR", isEnabled = false),
            DspModuleInfo("mod_xover", ModuleType.CROSSOVER, "12 48dB/OCT MULTI-WAY CROSSOVER", isEnabled = false),
            DspModuleInfo("mod_limiter", ModuleType.LIMITER, "13 BRICKWALL PEAK LIMITER", isEnabled = true)
        )
    )
    val rackModules: StateFlow<List<DspModuleInfo>> = _rackModules.asStateFlow()

    // Source Channel Strips (Real Detection & Controls)
    private val _sourceStrips = MutableStateFlow(
        listOf(
            com.example.model.SourceChannelStrip(com.example.model.InputSourceType.SYSTEM_AUDIO, isEnabled = true, statusReason = "Android Global Audio Effect Session"),
            com.example.model.SourceChannelStrip(com.example.model.InputSourceType.INTERNAL_PLAYER, isEnabled = true, statusReason = "Audio File Storage / Playlist"),
            com.example.model.SourceChannelStrip(com.example.model.InputSourceType.TEST_SIGNAL_GEN, isEnabled = true, statusReason = "Sine, Sweep & Noise Synth"),
            com.example.model.SourceChannelStrip(com.example.model.InputSourceType.BLUETOOTH_IN, isEnabled = false, status = com.example.model.CapabilityStatus.LIMITED, statusReason = "A2DP Sink Requires Device Pairing"),
            com.example.model.SourceChannelStrip(com.example.model.InputSourceType.USB_AUDIO_IN, isEnabled = false, status = com.example.model.CapabilityStatus.LIMITED, statusReason = "USB Host Mode / Interface Detection"),
            com.example.model.SourceChannelStrip(com.example.model.InputSourceType.AUX_LINE_IN, isEnabled = false, status = com.example.model.CapabilityStatus.UNAVAILABLE, statusReason = "Line-in hardware jack not present on mobile")
        )
    )
    val sourceStrips: StateFlow<List<com.example.model.SourceChannelStrip>> = _sourceStrips.asStateFlow()

    // Active Selected Source
    private val _selectedSource = MutableStateFlow(com.example.model.InputSourceType.SYSTEM_AUDIO)
    val selectedSource: StateFlow<com.example.model.InputSourceType> = _selectedSource.asStateFlow()

    // Output Route Destination States
    private val _outputRoutes = MutableStateFlow(
        listOf(
            com.example.model.OutputRouteState(com.example.model.OutputDestinationType.INTERNAL_SPEAKER, isConnected = true, profilePreset = "Phone Speaker Tuned"),
            com.example.model.OutputRouteState(com.example.model.OutputDestinationType.WIRED_HEADSET, isConnected = false, profilePreset = "Harman In-Ear Reference"),
            com.example.model.OutputRouteState(com.example.model.OutputDestinationType.BLUETOOTH_A2DP, isConnected = false, profilePreset = "Bluetooth Balanced"),
            com.example.model.OutputRouteState(com.example.model.OutputDestinationType.USB_DAC_INTERFACE, isConnected = false, profilePreset = "Bit-Perfect Studio Master"),
            com.example.model.OutputRouteState(com.example.model.OutputDestinationType.CAR_AUDIO_PROFILE, isConnected = false, profilePreset = "Car Sub & Time Alignment")
        )
    )
    val outputRoutes: StateFlow<List<com.example.model.OutputRouteState>> = _outputRoutes.asStateFlow()

    private val _selectedOutput = MutableStateFlow(com.example.model.OutputDestinationType.INTERNAL_SPEAKER)
    val selectedOutput: StateFlow<com.example.model.OutputDestinationType> = _selectedOutput.asStateFlow()

    // Car Audio Alignment State
    private val _carAlignment = MutableStateFlow(com.example.model.CarAudioTimeAlignment())
    val carAlignment: StateFlow<com.example.model.CarAudioTimeAlignment> = _carAlignment.asStateFlow()

    // Headphone Profile State
    private val _headphoneProfile = MutableStateFlow(com.example.model.HeadphoneDspProfile())
    val headphoneProfile: StateFlow<com.example.model.HeadphoneDspProfile> = _headphoneProfile.asStateFlow()

    // Current Active Preset
    private val _currentPreset = MutableStateFlow(storage.getAllPresets().first())
    val currentPreset: StateFlow<StudioPreset> = _currentPreset.asStateFlow()

    private val _allPresets = MutableStateFlow(storage.getAllPresets())
    val allPresets: StateFlow<List<StudioPreset>> = _allPresets.asStateFlow()

    // A/B Comparison Slots
    private var slotA: StudioPreset = storage.getAllPresets().first()
    private var slotB: StudioPreset = storage.getAllPresets().getOrNull(2) ?: storage.getAllPresets().first()
    private val _currentABSlot = MutableStateFlow("A")
    val currentABSlot: StateFlow<String> = _currentABSlot.asStateFlow()

    // Selected Navigation Tab
    private val _activeTab = MutableStateFlow("HOME") // HOME, EQ, PARAMETRIC, MONO_STEREO, TONE, DYNAMICS, CROSSOVER, ANALYZER, PRESETS, SETTINGS
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // UI state flags
    private val _isPowerOn = MutableStateFlow(false)
    val isPowerOn: StateFlow<Boolean> = _isPowerOn.asStateFlow()

    private val _isBypassed = MutableStateFlow(false)
    val isBypassed: StateFlow<Boolean> = _isBypassed.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Graphic EQ Band selection: 10, 15, 31
    private val _eqBandCount = MutableStateFlow(GraphicEqBandCount.BANDS_10)
    val eqBandCount: StateFlow<GraphicEqBandCount> = _eqBandCount.asStateFlow()

    // Selected PEQ Band index for inspector
    private val _selectedPeqBand = MutableStateFlow(0)
    val selectedPeqBand: StateFlow<Int> = _selectedPeqBand.asStateFlow()

    init {
        // Load initial flat preset
        loadPreset(storage.getAllPresets().first())
        slotA = _currentPreset.value
    }

    fun setTab(tab: String) {
        _activeTab.value = tab
    }

    fun togglePower() {
        if (_isPowerOn.value) {
            engine.stop()
            _isPowerOn.value = false
        } else {
            engine.testTone.isRunning = true
            engine.start()
            _isPowerOn.value = true
        }
    }

    fun toggleSystemDsp() {
        if (_isSystemDspActive.value) {
            engine.systemAudioBridge.disableSystemDsp()
            DspBackgroundService.stopService(getApplication())
            _isSystemDspActive.value = false
        } else {
            val ok = engine.systemAudioBridge.enableSystemDsp()
            if (ok) {
                DspBackgroundService.startService(getApplication())
                _isSystemDspActive.value = true
            }
        }
    }

    fun toggleRackModule(id: String) {
        val currentList = _rackModules.value.map {
            if (it.id == id) {
                val newState = !it.isEnabled
                when (it.type) {
                    ModuleType.DELAY -> engine.delayProcessor.isEnabled = newState
                    ModuleType.REVERB -> engine.reverbProcessor.isEnabled = newState
                    ModuleType.CHORUS -> engine.modulationProcessor.isEnabled = newState
                    ModuleType.BASS_PROCESSOR -> engine.bassEnhancer.isEnabled = newState
                    ModuleType.NOISE_GATE -> engine.dynamicsEffects.gateEnabled = newState
                    ModuleType.COMPRESSOR -> engine.compressor.isEnabled = newState
                    ModuleType.LIMITER -> engine.limiter.isEnabled = newState
                    ModuleType.GRAPHIC_EQ -> engine.graphicEq.isBypassed = !newState
                    ModuleType.PARAMETRIC_EQ -> engine.parametricEq.isBypassed = !newState
                    else -> {}
                }
                it.copy(isEnabled = newState)
            } else it
        }
        _rackModules.value = currentList
    }

    fun selectSource(sourceType: com.example.model.InputSourceType) {
        _selectedSource.value = sourceType
        when (sourceType) {
            com.example.model.InputSourceType.TEST_SIGNAL_GEN -> setAudioSource(AudioSourceType.TEST_TONE)
            com.example.model.InputSourceType.INTERNAL_PLAYER -> setAudioSource(AudioSourceType.AUDIO_FILE)
            else -> setAudioSource(AudioSourceType.TEST_TONE)
        }
    }

    fun selectOutput(dest: com.example.model.OutputDestinationType) {
        _selectedOutput.value = dest
        val updated = _outputRoutes.value.map {
            it.copy(isConnected = it.destinationType == dest)
        }
        _outputRoutes.value = updated
    }

    // Universal Sound Profile active state
    private val _activeUniversalProfile = MutableStateFlow(com.example.presets.UniversalSoundProfiles.profiles[4]) // default Sound Sistem Mewah
    val activeUniversalProfile: StateFlow<com.example.model.AudioProfileDetail> = _activeUniversalProfile.asStateFlow()

    fun applyUniversalSoundProfile(profile: com.example.model.AudioProfileDetail) {
        _activeUniversalProfile.value = profile

        // 1. Bass Enhancer & Sub Harmonics
        engine.bassEnhancer.isEnabled = profile.bassBoostDb > 0f || profile.subHarmonics > 0f
        engine.bassEnhancer.punchDb = profile.bassBoostDb
        engine.bassEnhancer.subHarmonicsAmount = profile.subHarmonics
        engine.bassEnhancer.updateSampleRate(engine.targetSampleRate)

        // 2. Quick Tone (Mid clarity and Treble/Air)
        engine.quickTone.midDb = profile.midClarityDb
        engine.quickTone.airDb = profile.airPresenceDb
        engine.quickTone.bassDb = profile.bassBoostDb * 0.7f
        engine.quickTone.update(engine.targetSampleRate)

        // 3. Binaural Crossfeed (for Headset / IEMs)
        engine.headphoneCrossfeed.isEnabled = profile.crossfeedEnabled
        engine.headphoneCrossfeed.amount = profile.crossfeedAmount
        _headphoneProfile.value = _headphoneProfile.value.copy(
            isCrossfeedEnabled = profile.crossfeedEnabled,
            crossfeedAmount = profile.crossfeedAmount
        )

        // 4. Stereo Width Spatial Immersion
        engine.stereoProcessor.stereoWidth = profile.stereoWidthPct / 100.0f

        // 5. Crossover Highpass Protection
        engine.crossover.lowFreq = profile.crossoverHpfHz
        engine.crossover.update(engine.targetSampleRate)

        // 6. Brickwall Peak Limiter Ceiling
        engine.limiter.ceilingDb = profile.limiterCeilingDb
    }

    fun updateDynamicEq(enabled: Boolean, freq: Float, q: Float, threshDb: Float, ratio: Float, isCut: Boolean) {
        engine.dynamicEq.isEnabled = enabled
        engine.dynamicEq.frequency = freq
        engine.dynamicEq.q = q
        engine.dynamicEq.thresholdDb = threshDb
        engine.dynamicEq.ratio = ratio
        engine.dynamicEq.isDynamicCut = isCut
        engine.dynamicEq.updateCoefficients(engine.targetSampleRate)
    }

    fun updateCrossfeed(enabled: Boolean, amount: Float) {
        engine.headphoneCrossfeed.isEnabled = enabled
        engine.headphoneCrossfeed.amount = amount
        _headphoneProfile.value = _headphoneProfile.value.copy(isCrossfeedEnabled = enabled, crossfeedAmount = amount)
    }

    fun updateTimeAlignment(leftMs: Float, rightMs: Float) {
        engine.timeAlignmentLeft.delayMs = leftMs
        engine.timeAlignmentRight.delayMs = rightMs
        _carAlignment.value = _carAlignment.value.copy(frontLeftDelayMs = leftMs, frontRightDelayMs = rightMs)
    }

    fun toggleBypass() {
        val newState = !_isBypassed.value
        _isBypassed.value = newState
        engine.isBypassed = newState
    }

    fun toggleMute() {
        val newState = !_isMuted.value
        _isMuted.value = newState
        engine.isMuted = newState
    }

    fun setInputGain(gainDb: Float) {
        engine.inputGainDb = gainDb
    }

    fun setMasterGain(gainDb: Float) {
        engine.masterGainDb = gainDb
    }

    fun setGraphicEqBandCount(count: GraphicEqBandCount) {
        _eqBandCount.value = count
        engine.graphicEq.bandMode = count
        engine.graphicEq.updateFilters(engine.targetSampleRate)
    }

    fun setGraphicBandGain(index: Int, gainDb: Float) {
        engine.graphicEq.setBandGain(index, gainDb, engine.targetSampleRate)
    }

    fun setGraphicPreamp(gainDb: Float) {
        engine.graphicEq.preampDb = gainDb
    }

    fun resetGraphicEq() {
        engine.graphicEq.resetAll(engine.targetSampleRate)
    }

    // PEQ Controls
    fun selectPeqBand(index: Int) {
        _selectedPeqBand.value = index
    }

    fun updatePeqBand(index: Int, freq: Float, gainDb: Float, q: Float, type: FilterType, enabled: Boolean) {
        if (index in 0 until 10) {
            val b = engine.parametricEq.bands[index]
            b.frequency = freq
            b.gainDb = gainDb
            b.q = q
            b.type = type
            b.isEnabled = enabled
            engine.parametricEq.updateBand(index, engine.targetSampleRate)
        }
    }

    // Quick Tone Controls
    fun setQuickTone(sub: Float, bass: Float, lowMid: Float, mid: Float, upperMid: Float, presence: Float, air: Float) {
        engine.quickTone.subDb = sub
        engine.quickTone.bassDb = bass
        engine.quickTone.lowMidDb = lowMid
        engine.quickTone.midDb = mid
        engine.quickTone.upperMidDb = upperMid
        engine.quickTone.presenceDb = presence
        engine.quickTone.airDb = air
        engine.quickTone.update(engine.targetSampleRate)
    }

    fun applyQuickTonePreset(presetName: String) {
        engine.quickTone.applyPreset(presetName, engine.targetSampleRate)
    }

    // Routing & Stereo Controls
    fun setRoutingMode(mode: RoutingMode) {
        engine.stereoProcessor.mode = mode
    }

    fun setStereoWidth(width: Float) {
        engine.stereoProcessor.stereoWidth = width
    }

    fun setBalance(bal: Float) {
        engine.stereoProcessor.balance = bal
    }

    fun setMidSideGain(midDb: Float, sideDb: Float) {
        engine.stereoProcessor.midGainDb = midDb
        engine.stereoProcessor.sideGainDb = sideDb
    }

    fun setPolarity(invertL: Boolean, invertR: Boolean) {
        engine.stereoProcessor.invertLeft = invertL
        engine.stereoProcessor.invertRight = invertR
    }

    fun setPhase(degrees: Float) {
        engine.stereoProcessor.phaseDegrees = degrees
    }

    // Dynamics
    fun updateCompressor(enabled: Boolean, threshDb: Float, ratio: Float, attackMs: Float, releaseMs: Float, makeupDb: Float) {
        engine.compressor.isEnabled = enabled
        engine.compressor.thresholdDb = threshDb
        engine.compressor.ratio = ratio
        engine.compressor.attackMs = attackMs
        engine.compressor.releaseMs = releaseMs
        engine.compressor.makeupGainDb = makeupDb
    }

    fun updateLimiter(enabled: Boolean, threshDb: Float, ceilDb: Float) {
        engine.limiter.isEnabled = enabled
        engine.limiter.thresholdDb = threshDb
        engine.limiter.ceilingDb = ceilDb
    }

    fun updateClipper(enabled: Boolean, driveDb: Float, outDb: Float) {
        engine.softClipper.isEnabled = enabled
        engine.softClipper.driveDb = driveDb
        engine.softClipper.outputDb = outDb
    }

    // Crossover
    fun updateCrossover(enabled: Boolean, mode: CrossoverMode, slope: CrossoverSlope, lowF: Float, highF: Float, lowG: Float, midG: Float, highG: Float) {
        engine.crossover.isEnabled = enabled
        engine.crossover.mode = mode
        engine.crossover.slope = slope
        engine.crossover.lowFreq = lowF
        engine.crossover.highFreq = highF
        engine.crossover.lowGainDb = lowG
        engine.crossover.midGainDb = midG
        engine.crossover.highGainDb = highG
        engine.crossover.update(engine.targetSampleRate)
    }

    // Test tone generator
    fun setTestSignal(type: ToneSignalType, freq: Float, levelDb: Float) {
        engine.testTone.signalType = type
        engine.testTone.frequency = freq
        engine.testTone.levelDb = levelDb
    }

    fun setAudioSource(source: AudioSourceType) {
        val wasRunning = engine.isRunning
        if (wasRunning) engine.stop()
        engine.sourceType = source
        if (wasRunning) engine.start()
    }

    // Preset & A/B System
    fun loadPreset(preset: StudioPreset) {
        _currentPreset.value = preset

        // Apply to DSP engines smoothly
        engine.graphicEq.preampDb = preset.preampDb
        val count = when (preset.graphicBandCount) {
            15 -> GraphicEqBandCount.BANDS_15
            31 -> GraphicEqBandCount.BANDS_31
            else -> GraphicEqBandCount.BANDS_10
        }
        _eqBandCount.value = count
        engine.graphicEq.bandMode = count

        for (i in 0 until 31) {
            val gain = preset.graphicGains.getOrElse(i) { 0f }
            engine.graphicEq.bandGains31[i] = gain
        }
        engine.graphicEq.updateFilters(engine.targetSampleRate)

        // PEQ
        if (preset.peqBands.isNotEmpty()) {
            preset.peqBands.forEachIndexed { idx, p ->
                if (idx < 10) {
                    val b = engine.parametricEq.bands[idx]
                    b.isEnabled = p.enabled
                    b.frequency = p.freq
                    b.gainDb = p.gainDb
                    b.q = p.q
                    b.type = try { FilterType.valueOf(p.type) } catch (e: Exception) { FilterType.PEAK }
                }
            }
        }
        engine.parametricEq.updateAll(engine.targetSampleRate)

        // Quick tone
        engine.quickTone.subDb = preset.subDb
        engine.quickTone.bassDb = preset.bassDb
        engine.quickTone.lowMidDb = preset.lowMidDb
        engine.quickTone.midDb = preset.midDb
        engine.quickTone.upperMidDb = preset.upperMidDb
        engine.quickTone.presenceDb = preset.presenceDb
        engine.quickTone.airDb = preset.airDb
        engine.quickTone.update(engine.targetSampleRate)

        // Stereo & Routing
        engine.stereoProcessor.mode = try { RoutingMode.valueOf(preset.routingMode) } catch (e: Exception) { RoutingMode.STEREO }
        engine.stereoProcessor.stereoWidth = preset.stereoWidth
        engine.stereoProcessor.balance = preset.balance
        engine.stereoProcessor.midGainDb = preset.midGainDb
        engine.stereoProcessor.sideGainDb = preset.sideGainDb

        // Dynamics
        engine.compressor.isEnabled = preset.compEnabled
        engine.compressor.thresholdDb = preset.compThresholdDb
        engine.compressor.ratio = preset.compRatio
        engine.compressor.attackMs = preset.compAttackMs
        engine.compressor.releaseMs = preset.compReleaseMs
        engine.compressor.makeupGainDb = preset.compMakeupDb

        engine.limiter.isEnabled = preset.limiterEnabled
        engine.limiter.thresholdDb = preset.limiterThresholdDb
        engine.limiter.ceilingDb = preset.limiterCeilingDb

        // Crossover
        engine.crossover.isEnabled = preset.xoverEnabled
        engine.crossover.mode = try { CrossoverMode.valueOf(preset.xoverMode) } catch (e: Exception) { CrossoverMode.TWO_WAY }
        engine.crossover.slope = try { CrossoverSlope.valueOf(preset.xoverSlope) } catch (e: Exception) { CrossoverSlope.SLOPE_24DB }
        engine.crossover.lowFreq = preset.xoverLowFreq
        engine.crossover.highFreq = preset.xoverHighFreq
        engine.crossover.lowGainDb = preset.xoverLowGainDb
        engine.crossover.midGainDb = preset.xoverMidGainDb
        engine.crossover.highGainDb = preset.xoverHighGainDb
        engine.crossover.update(engine.targetSampleRate)
    }

    fun savePreset(name: String, category: String = "User") {
        val current = _currentPreset.value
        val peqList = engine.parametricEq.bands.map {
            PeqBandModel(
                id = it.id,
                enabled = it.isEnabled,
                type = it.type.name,
                freq = it.frequency,
                gainDb = it.gainDb,
                q = it.q
            )
        }
        val p = current.copy(
            name = name,
            category = category,
            preampDb = engine.graphicEq.preampDb,
            graphicBandCount = engine.graphicEq.bandMode.count,
            graphicGains = engine.graphicEq.bandGains31.toList(),
            peqBands = peqList,
            subDb = engine.quickTone.subDb,
            bassDb = engine.quickTone.bassDb,
            lowMidDb = engine.quickTone.lowMidDb,
            midDb = engine.quickTone.midDb,
            upperMidDb = engine.quickTone.upperMidDb,
            presenceDb = engine.quickTone.presenceDb,
            airDb = engine.quickTone.airDb,
            routingMode = engine.stereoProcessor.mode.name,
            stereoWidth = engine.stereoProcessor.stereoWidth,
            balance = engine.stereoProcessor.balance,
            midGainDb = engine.stereoProcessor.midGainDb,
            sideGainDb = engine.stereoProcessor.sideGainDb,
            compEnabled = engine.compressor.isEnabled,
            compThresholdDb = engine.compressor.thresholdDb,
            compRatio = engine.compressor.ratio,
            compAttackMs = engine.compressor.attackMs,
            compReleaseMs = engine.compressor.releaseMs,
            compMakeupDb = engine.compressor.makeupGainDb,
            limiterEnabled = engine.limiter.isEnabled,
            limiterThresholdDb = engine.limiter.thresholdDb,
            limiterCeilingDb = engine.limiter.ceilingDb,
            xoverEnabled = engine.crossover.isEnabled,
            xoverMode = engine.crossover.mode.name,
            xoverSlope = engine.crossover.slope.name,
            xoverLowFreq = engine.crossover.lowFreq,
            xoverHighFreq = engine.crossover.highFreq,
            xoverLowGainDb = engine.crossover.lowGainDb,
            xoverMidGainDb = engine.crossover.midGainDb,
            xoverHighGainDb = engine.crossover.highGainDb
        )
        storage.saveUserPreset(p)
        _allPresets.value = storage.getAllPresets()
        _currentPreset.value = p
    }

    fun deletePreset(name: String) {
        storage.deleteUserPreset(name)
        _allPresets.value = storage.getAllPresets()
    }

    fun toggleABCompare() {
        if (_currentABSlot.value == "A") {
            slotA = _currentPreset.value
            _currentABSlot.value = "B"
            loadPreset(slotB)
        } else {
            slotB = _currentPreset.value
            _currentABSlot.value = "A"
            loadPreset(slotA)
        }
    }

    fun setSampleRate(sampleRate: Int) {
        val wasRunning = engine.isRunning
        if (wasRunning) engine.stop()
        engine.targetSampleRate = sampleRate
        engine.reconfigureDsp()
        if (wasRunning) engine.start()
    }

    fun setLatencyProfile(profile: LatencyProfile) {
        val wasRunning = engine.isRunning
        if (wasRunning) engine.stop()
        engine.latencyProfile = profile
        if (wasRunning) engine.start()
    }

    override fun onCleared() {
        super.onCleared()
        engine.stop()
    }
}
