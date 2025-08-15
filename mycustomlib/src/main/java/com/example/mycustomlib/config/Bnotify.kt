package com.example.mycustomlib

import android.content.Context
import android.util.Log
import com.example.mycustomlib.config.GeneratedConfig
import com.example.mycustomlib.model.BnotifyConfig
import org.json.JSONObject

fun Context.readBNotifyConfig(): BnotifyConfig? {
    val json = GeneratedConfig.JSON ?: return null // safe null check

    return try {
        val jsonObject = JSONObject(json)
        Log.i("Bnotify", "Extracted DATA: $json")

        BnotifyConfig(
            projectId = jsonObject.optString("projectId"),
            packageName = jsonObject.optString("packageName"),
            apiKey = jsonObject.optString("apiKey"),
            authDomain = jsonObject.optString("authDomain"),
            databaseURL = jsonObject.optString("databaseURL"),
            storageBucket = jsonObject.optString("storageBucket"),
            messagingSenderId = jsonObject.optString("messagingSenderId"),
            appId = jsonObject.optString("appId"),
            measurementId = jsonObject.optString("measurementId")
        )
    } catch (e: Exception) {
        Log.e("Bnotify", "Failed to parse config: ${e.message}")
        null
    }
}
