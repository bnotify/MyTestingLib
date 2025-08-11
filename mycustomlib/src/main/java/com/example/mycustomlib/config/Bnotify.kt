package com.example.mycustomlib.config

import android.content.Context
import android.util.Log
import com.example.mycustomlib.R
import org.xmlpull.v1.XmlPullParser

object Bnotify {

    fun Context.readBnotifyConfig(): Map<String, String> {
        val parser = resources.getXml(R.xml.bnotify_config)
        val map = mutableMapOf<String, String>()
        var eventType = parser.eventType
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> currentTag = parser.name
                XmlPullParser.TEXT -> currentTag?.let { map[it] = parser.text.trim() }
                XmlPullParser.END_TAG -> currentTag = null
            }
            eventType = parser.next()
        }
        return map
    }
}