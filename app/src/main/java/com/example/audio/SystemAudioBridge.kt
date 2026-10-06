package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

/**
 * System Audio Bridge for Android AudioSession 0 / Global Media playback.
 * Connects to background music playing from Spotify, YouTube, SoundCloud, Games, etc.
 * Handles graceful fallback without crashing if a device OEM restricts global effects.
 */
class SystemAudioBridge(private val context: Context) {

    private var systemEqualizer: Equalizer? = null
    private var systemBassBoost: BassBoost? = null
    private var systemVirtualizer: Virtualizer? = null

    var isSystemDspActive: Boolean = false
        private set

    fun enableSystemDsp(sessionId: Int = 0): Boolean {
        try {
            // Equalizer attached to global audio session (0) or app session
            systemEqualizer = Equalizer(1000, sessionId).apply {
                enabled = true
            }

            // BassBoost
            systemBassBoost = BassBoost(1000, sessionId).apply {
                enabled = true
                setStrength(500.toShort()) // 50%
            }

            // Virtualizer / Stereo widening
            systemVirtualizer = Virtualizer(1000, sessionId).apply {
                enabled = true
                setStrength(500.toShort())
            }

            isSystemDspActive = true
            broadcastAudioEffectIntent(true, sessionId)
            return true
        } catch (e: Exception) {
            Log.w("SystemAudioBridge", "Global audio effect session restricted on this device: ${e.message}")
            isSystemDspActive = false
            return false
        }
    }

    fun disableSystemDsp(sessionId: Int = 0) {
        try {
            systemEqualizer?.enabled = false
            systemEqualizer?.release()
            systemEqualizer = null

            systemBassBoost?.enabled = false
            systemBassBoost?.release()
            systemBassBoost = null

            systemVirtualizer?.enabled = false
            systemVirtualizer?.release()
            systemVirtualizer = null

            isSystemDspActive = false
            broadcastAudioEffectIntent(false, sessionId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSystemBassStrength(strengthPct: Float) {
        try {
            val strengthShort = (strengthPct.coerceIn(0f, 1f) * 1000).toInt().toShort()
            systemBassBoost?.setStrength(strengthShort)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setSystemStereoStrength(strengthPct: Float) {
        try {
            val strengthShort = (strengthPct.coerceIn(0f, 1f) * 1000).toInt().toShort()
            systemVirtualizer?.setStrength(strengthShort)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun broadcastAudioEffectIntent(open: Boolean, sessionId: Int) {
        try {
            val action = if (open) {
                android.media.audiofx.AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
            } else {
                android.media.audiofx.AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
            }
            val intent = Intent(action).apply {
                putExtra(android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                putExtra(android.media.audiofx.AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
