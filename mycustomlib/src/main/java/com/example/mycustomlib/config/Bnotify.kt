package com.example.mycustomlib.config

import android.util.Log

class Bnotify {

    fun init() {
        println("BNotify_ClassLoader: ${GeneratedConfig::class.java.classLoader}")
        println("BNotify_All values: ${GeneratedConfig.JSON}") // Check raw JSON
    }
}