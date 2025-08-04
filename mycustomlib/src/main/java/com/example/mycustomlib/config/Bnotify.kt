package com.example.mycustomlib.config

import android.util.Log

class Bnotify {

    fun init(){
        Log.i("BNotify","projectId: ${GeneratedConfig.projectId} packageName: ${GeneratedConfig.packageName} apiKey: ${GeneratedConfig.apiKey} authDomain: ${GeneratedConfig.authDomain} databaseURL: ${GeneratedConfig.databaseURL} storageBucket: ${GeneratedConfig.storageBucket} messagingSenderId: ${GeneratedConfig.messagingSenderId} appId: ${GeneratedConfig.appId} measurementId: ${GeneratedConfig.measurementId}")
    }
}