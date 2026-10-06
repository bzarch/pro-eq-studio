package com.example.presets

import com.example.model.AudioProfileDetail
import com.example.model.AudioSystemCategory

object UniversalSoundProfiles {

    val profiles: List<AudioProfileDetail> = listOf(
        AudioProfileDetail(
            category = AudioSystemCategory.IEM_AUDIOPHILE,
            presetName = "IEM Harman Reference Target",
            bassBoostDb = 3.5f,
            subHarmonics = 0.2f,
            midClarityDb = 2.0f,
            airPresenceDb = 3.5f,
            crossfeedEnabled = true,
            crossfeedAmount = 0.25f,
            limiterCeilingDb = -0.5f,
            crossoverHpfHz = 10f,
            stereoWidthPct = 105f,
            description = "Tuned for In-Ear Monitors (KZ, Moondrop, Tangzu, Sennheiser IE). Enhanced pinna sub-bass rumble, natural midrange, and crystalline treble."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.TWS_EARBUDS,
            presetName = "TWS Deep Bass & Clear Vocal",
            bassBoostDb = 5.0f,
            subHarmonics = 0.4f,
            midClarityDb = 2.5f,
            airPresenceDb = 2.0f,
            crossfeedEnabled = false,
            crossfeedAmount = 0.0f,
            limiterCeilingDb = -0.2f,
            crossoverHpfHz = 25f,
            stereoWidthPct = 110f,
            description = "Optimized for wireless Bluetooth TWS (AirPods, Galaxy Buds, Sony WF, Soundcore). Compensates compression, adds warm sub punch and speech clarity."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.OVER_EAR_HEADSET,
            presetName = "Headset Studio Mastering Flat",
            bassBoostDb = 1.0f,
            subHarmonics = 0.0f,
            midClarityDb = 0.5f,
            airPresenceDb = 1.5f,
            crossfeedEnabled = true,
            crossfeedAmount = 0.40f,
            limiterCeilingDb = -0.1f,
            crossoverHpfHz = 15f,
            stereoWidthPct = 100f,
            description = "Perfect for studio monitor headphones (Audio Technica M50x, Sony MDR-7506, Sennheiser HD600/650). Binaural crossfeed reduces headphone fatigue."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.ROOM_BEDROOM,
            presetName = "Kamar Akustik / Nearfield Desk",
            bassBoostDb = 1.5f,
            subHarmonics = 0.1f,
            midClarityDb = 1.5f,
            airPresenceDb = 2.0f,
            crossfeedEnabled = false,
            crossfeedAmount = 0.0f,
            limiterCeilingDb = -0.5f,
            crossoverHpfHz = 40f,
            stereoWidthPct = 115f,
            description = "Didesain untuk speaker kamar / meja belajar / mini studio. Mengurangi dengung pantulan dinding (room boom) dan memperjelas vokal di jarak dekat."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.HIGH_END_HIFI,
            presetName = "Sound Sistem Mewah / Audiophile Hi-Fi",
            bassBoostDb = 2.0f,
            subHarmonics = 0.35f,
            midClarityDb = 1.0f,
            airPresenceDb = 3.0f,
            crossfeedEnabled = false,
            crossfeedAmount = 0.0f,
            limiterCeilingDb = -0.05f,
            crossoverHpfHz = 20f,
            stereoWidthPct = 125f,
            description = "Profil untuk perangkat High-End Audio, Ampli Tabung / Class A/AB, Speaker Tower & DAC Mewah. Separasi instrumen holografik, ultra-low distortion, dan panggung suara megah."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.PA_STAGE_OUTDOOR,
            presetName = "PA Lapangan & Sound System Hajatan",
            bassBoostDb = 4.0f,
            subHarmonics = 0.6f,
            midClarityDb = 4.5f,
            airPresenceDb = 3.0f,
            crossfeedEnabled = false,
            crossfeedAmount = 0.0f,
            limiterCeilingDb = -1.5f,
            crossoverHpfHz = 35f,
            stereoWidthPct = 100f,
            description = "Setup untuk audio panggung, sound hajatan/event outdoor. Mid vokal gacor tembus jarak jauh, bass padat bertenaga, dan proteksi limiter brickwall anti-jebol."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.CAR_AUDIO_DSP,
            presetName = "Car Audio SQL / Time Aligned",
            bassBoostDb = 6.0f,
            subHarmonics = 0.5f,
            midClarityDb = 2.0f,
            airPresenceDb = 2.5f,
            crossfeedEnabled = false,
            crossfeedAmount = 0.0f,
            limiterCeilingDb = -0.3f,
            crossoverHpfHz = 30f,
            stereoWidthPct = 120f,
            description = "Optimasi kabin mobil (Sound Quality Loud). Penyelarasan delay speaker depan/belakang dan bass kabin bertenaga."
        ),
        AudioProfileDetail(
            category = AudioSystemCategory.PARTY_SPEAKER,
            presetName = "Party Box / Bass Boster Outdoor",
            bassBoostDb = 7.5f,
            subHarmonics = 0.7f,
            midClarityDb = 1.0f,
            airPresenceDb = 4.0f,
            crossfeedEnabled = false,
            crossfeedAmount = 0.0f,
            limiterCeilingDb = -0.5f,
            crossoverHpfHz = 45f,
            stereoWidthPct = 130f,
            description = "Untuk speaker portable Bluetooth pesta (JBL PartyBox, Sony XP, Harman Kardon). Dentuman bass maksimal dengan proteksi excursion limiter."
        )
    )
}
