package com.example.mycustomplugin

import kotlinx.serialization.Serializable

@Serializable
data class BnotifyConfig(
    val projectId: String,
    val packageName: String,
    val apiKey: String,
    val authDomain: String? = null,
    val databaseURL: String? = null,
    val storageBucket: String? = null,
    val messagingSenderId: String? = null,
    val appId: String,
    val measurementId: String? = null
)