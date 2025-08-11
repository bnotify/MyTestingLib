package com.example.mycustomlib.config

import android.util.Log

object Bnotify {

    fun init() {
        println("BNotify_ClassLoader: ${GeneratedConfig::class.java.classLoader}")
        println("BNotify_All values: ${GeneratedConfig.JSON}") // Check raw JSON
        Log.i("BNotify","projectId: ${GeneratedConfig.projectId} packageName: ${GeneratedConfig.packageName} apiKey: ${GeneratedConfig.apiKey} authDomain: ${GeneratedConfig.authDomain} databaseURL: ${GeneratedConfig.databaseURL} storageBucket: ${GeneratedConfig.storageBucket} messagingSenderId: ${GeneratedConfig.messagingSenderId} appId: ${GeneratedConfig.appId} measurementId: ${GeneratedConfig.measurementId}")
    }

    fun getConfig(): Map<String, String> {
        return mapOf(
            "projectId" to GeneratedConfig.projectId,
            "packageName" to GeneratedConfig.packageName,
            "apiKey" to GeneratedConfig.apiKey,
            "authDomain" to GeneratedConfig.authDomain,
            "databaseURL" to GeneratedConfig.databaseURL,
            "storageBucket" to GeneratedConfig.storageBucket,
            "messagingSenderId" to GeneratedConfig.messagingSenderId,
            "appId" to GeneratedConfig.appId,
            "measurementId" to GeneratedConfig.measurementId,
        )
    }
}