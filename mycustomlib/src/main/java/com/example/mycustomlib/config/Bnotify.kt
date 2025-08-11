package com.example.mycustomlib

import android.content.Context
import android.util.Xml
import com.example.mycustomlib.model.BnotifyConfig
import org.xmlpull.v1.XmlPullParser

fun Context.readBnotifyConfig(): BnotifyConfig? {
    val parser: XmlPullParser = resources.getXml(R.xml.bnotify_config)
    var projectId: String? = null
    var packageName: String? = null
    var apiKey: String? = null
    var authDomain: String? = null
    var databaseURL: String? = null
    var storageBucket: String? = null
    var messagingSenderId: String? = null
    var appId: String? = null
    var measurementId: String? = null

    var eventType = parser.eventType
    var currentTag: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> currentTag = parser.name
            XmlPullParser.TEXT -> {
                val value = parser.text.trim()
                when (currentTag) {
                    "projectId" -> projectId = value
                    "packageName" -> packageName = value
                    "apiKey" -> apiKey = value
                    "authDomain" -> authDomain = value
                    "databaseURL" -> databaseURL = value
                    "storageBucket" -> storageBucket = value
                    "messagingSenderId" -> messagingSenderId = value
                    "appId" -> appId = value
                    "measurementId" -> measurementId = value
                }
            }
            XmlPullParser.END_TAG -> currentTag = null
        }
        eventType = parser.next()
    }

    return if (projectId != null && apiKey != null && appId != null) {
        BnotifyConfig(
            projectId = projectId!!,
            packageName = packageName ?: "",
            apiKey = apiKey!!,
            authDomain = authDomain,
            databaseURL = databaseURL,
            storageBucket = storageBucket,
            messagingSenderId = messagingSenderId,
            appId = appId!!,
            measurementId = measurementId
        )
    } else {
        null // Required fields missing
    }
}
