package com.example.mycustomlib

import android.content.Context
import com.example.mycustomlib.config.GeneratedConfig
import com.example.mycustomlib.model.BnotifyConfig
import org.json.JSONObject

fun Context.readBNotifyConfig(): BnotifyConfig? {
    val jsonObject = JSONObject(GeneratedConfig.JSON)
    val projectId = jsonObject.getString("projectId")
    val packageName = jsonObject.getString("packageName")
    val apiKey = jsonObject.getString("apiKey")
    val authDomain = jsonObject.getString("authDomain")
    val databaseURL = jsonObject.getString("databaseURL")
    val storageBucket = jsonObject.getString("storageBucket")
    val messagingSenderId = jsonObject.getString("messagingSenderId")
    val appId = jsonObject.getString("appId")
    val measurementId = jsonObject.getString("measurementId")
    val model = BnotifyConfig(projectId, packageName, apiKey, authDomain, databaseURL, storageBucket, messagingSenderId, appId, measurementId)
    return model
}
