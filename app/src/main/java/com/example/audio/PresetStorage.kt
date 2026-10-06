package com.example.audio

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PeqBandModel
import com.example.model.StudioPreset
import com.example.presets.BuiltInPresets
import org.json.JSONArray
import org.json.JSONObject

class PresetStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pro_eq_studio_presets", Context.MODE_PRIVATE)

    fun getAllPresets(): List<StudioPreset> {
        val list = mutableListOf<StudioPreset>()
        list.addAll(BuiltInPresets.presets)

        val userJson = prefs.getString("user_presets", null)
        if (!userJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(userJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(deserialize(obj))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list
    }

    fun saveUserPreset(preset: StudioPreset) {
        val current = getUserPresets().toMutableList()
        current.removeAll { it.name.equals(preset.name, ignoreCase = true) }
        current.add(preset)
        saveUserPresets(current)
    }

    fun deleteUserPreset(presetName: String) {
        val current = getUserPresets().toMutableList()
        current.removeAll { it.name.equals(presetName, ignoreCase = true) }
        saveUserPresets(current)
    }

    fun getUserPresets(): List<StudioPreset> {
        val list = mutableListOf<StudioPreset>()
        val userJson = prefs.getString("user_presets", null) ?: return emptyList()
        try {
            val array = JSONArray(userJson)
            for (i in 0 until array.length()) {
                list.add(deserialize(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveUserPresets(presets: List<StudioPreset>) {
        val array = JSONArray()
        for (p in presets) {
            array.put(serialize(p))
        }
        prefs.edit().putString("user_presets", array.toString()).apply()
    }

    fun serialize(p: StudioPreset): JSONObject {
        val obj = JSONObject()
        obj.put("name", p.name)
        obj.put("category", p.category)
        obj.put("version", p.version)
        obj.put("preampDb", p.preampDb.toDouble())
        obj.put("inputGainDb", p.inputGainDb.toDouble())
        obj.put("masterGainDb", p.masterGainDb.toDouble())
        obj.put("graphicBandCount", p.graphicBandCount)

        val gainsArr = JSONArray()
        p.graphicGains.forEach { gainsArr.put(it.toDouble()) }
        obj.put("graphicGains", gainsArr)

        val peqArr = JSONArray()
        p.peqBands.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("enabled", b.enabled)
            bObj.put("type", b.type)
            bObj.put("freq", b.freq.toDouble())
            bObj.put("gainDb", b.gainDb.toDouble())
            bObj.put("q", b.q.toDouble())
            peqArr.put(bObj)
        }
        obj.put("peqBands", peqArr)

        obj.put("subDb", p.subDb.toDouble())
        obj.put("bassDb", p.bassDb.toDouble())
        obj.put("lowMidDb", p.lowMidDb.toDouble())
        obj.put("midDb", p.midDb.toDouble())
        obj.put("upperMidDb", p.upperMidDb.toDouble())
        obj.put("presenceDb", p.presenceDb.toDouble())
        obj.put("airDb", p.airDb.toDouble())

        obj.put("routingMode", p.routingMode)
        obj.put("stereoWidth", p.stereoWidth.toDouble())
        obj.put("balance", p.balance.toDouble())
        obj.put("midGainDb", p.midGainDb.toDouble())
        obj.put("sideGainDb", p.sideGainDb.toDouble())

        obj.put("compEnabled", p.compEnabled)
        obj.put("compThresholdDb", p.compThresholdDb.toDouble())
        obj.put("compRatio", p.compRatio.toDouble())
        obj.put("compAttackMs", p.compAttackMs.toDouble())
        obj.put("compReleaseMs", p.compReleaseMs.toDouble())
        obj.put("compMakeupDb", p.compMakeupDb.toDouble())

        obj.put("limiterEnabled", p.limiterEnabled)
        obj.put("limiterThresholdDb", p.limiterThresholdDb.toDouble())
        obj.put("limiterCeilingDb", p.limiterCeilingDb.toDouble())

        obj.put("xoverEnabled", p.xoverEnabled)
        obj.put("xoverMode", p.xoverMode)
        obj.put("xoverSlope", p.xoverSlope)
        obj.put("xoverLowFreq", p.xoverLowFreq.toDouble())
        obj.put("xoverHighFreq", p.xoverHighFreq.toDouble())
        obj.put("xoverLowGainDb", p.xoverLowGainDb.toDouble())
        obj.put("xoverMidGainDb", p.xoverMidGainDb.toDouble())
        obj.put("xoverHighGainDb", p.xoverHighGainDb.toDouble())

        return obj
    }

    fun deserialize(obj: JSONObject): StudioPreset {
        val gains = mutableListOf<Float>()
        val gainsArr = obj.optJSONArray("graphicGains")
        if (gainsArr != null) {
            for (i in 0 until gainsArr.length()) {
                gains.add(gainsArr.optDouble(i, 0.0).toFloat())
            }
        }
        while (gains.size < 31) gains.add(0f)

        val peqList = mutableListOf<PeqBandModel>()
        val peqArr = obj.optJSONArray("peqBands")
        if (peqArr != null) {
            for (i in 0 until peqArr.length()) {
                val b = peqArr.getJSONObject(i)
                peqList.add(
                    PeqBandModel(
                        id = b.optInt("id", i),
                        enabled = b.optBoolean("enabled", true),
                        type = b.optString("type", "PEAK"),
                        freq = b.optDouble("freq", 1000.0).toFloat(),
                        gainDb = b.optDouble("gainDb", 0.0).toFloat(),
                        q = b.optDouble("q", 1.0).toFloat()
                    )
                )
            }
        }

        return StudioPreset(
            name = obj.optString("name", "Custom Preset"),
            category = obj.optString("category", "User"),
            version = obj.optInt("version", 1),
            preampDb = obj.optDouble("preampDb", 0.0).toFloat(),
            inputGainDb = obj.optDouble("inputGainDb", 0.0).toFloat(),
            masterGainDb = obj.optDouble("masterGainDb", 0.0).toFloat(),
            graphicBandCount = obj.optInt("graphicBandCount", 10),
            graphicGains = gains,
            peqBands = peqList,
            subDb = obj.optDouble("subDb", 0.0).toFloat(),
            bassDb = obj.optDouble("bassDb", 0.0).toFloat(),
            lowMidDb = obj.optDouble("lowMidDb", 0.0).toFloat(),
            midDb = obj.optDouble("midDb", 0.0).toFloat(),
            upperMidDb = obj.optDouble("upperMidDb", 0.0).toFloat(),
            presenceDb = obj.optDouble("presenceDb", 0.0).toFloat(),
            airDb = obj.optDouble("airDb", 0.0).toFloat(),
            routingMode = obj.optString("routingMode", "STEREO"),
            stereoWidth = obj.optDouble("stereoWidth", 1.0).toFloat(),
            balance = obj.optDouble("balance", 0.0).toFloat(),
            midGainDb = obj.optDouble("midGainDb", 0.0).toFloat(),
            sideGainDb = obj.optDouble("sideGainDb", 0.0).toFloat(),
            compEnabled = obj.optBoolean("compEnabled", false),
            compThresholdDb = obj.optDouble("compThresholdDb", -20.0).toFloat(),
            compRatio = obj.optDouble("compRatio", 4.0).toFloat(),
            compAttackMs = obj.optDouble("compAttackMs", 20.0).toFloat(),
            compReleaseMs = obj.optDouble("compReleaseMs", 150.0).toFloat(),
            compMakeupDb = obj.optDouble("compMakeupDb", 0.0).toFloat(),
            limiterEnabled = obj.optBoolean("limiterEnabled", true),
            limiterThresholdDb = obj.optDouble("limiterThresholdDb", -0.5).toFloat(),
            limiterCeilingDb = obj.optDouble("limiterCeilingDb", -0.1).toFloat(),
            xoverEnabled = obj.optBoolean("xoverEnabled", false),
            xoverMode = obj.optString("xoverMode", "TWO_WAY"),
            xoverSlope = obj.optString("xoverSlope", "SLOPE_24DB"),
            xoverLowFreq = obj.optDouble("xoverLowFreq", 250.0).toFloat(),
            xoverHighFreq = obj.optDouble("xoverHighFreq", 3500.0).toFloat(),
            xoverLowGainDb = obj.optDouble("xoverLowGainDb", 0.0).toFloat(),
            xoverMidGainDb = obj.optDouble("xoverMidGainDb", 0.0).toFloat(),
            xoverHighGainDb = obj.optDouble("xoverHighGainDb", 0.0).toFloat()
        )
    }
}
