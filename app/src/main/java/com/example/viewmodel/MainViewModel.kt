package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.model.PeqBandModel
import com.example.model.StudioPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val engine = AudioEngine(application.applicationContext)
    val storage = PresetStorage(application.applicationContext)

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
