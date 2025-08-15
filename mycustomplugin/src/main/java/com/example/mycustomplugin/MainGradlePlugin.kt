package com.example.mycustomplugin

import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import kotlinx.serialization.Serializable

@Serializable
internal data class BnotifyConfig(
    val projectId: String,
    val packageName: String,
    val apiKey: String,
    val authDomain: String? = null,
    val databaseURL: String? = null,
    val storageBucket: String? = null,
    val messagingSenderId: String? = null,  // Optional field
    val appId: String,
    val measurementId: String? = null
)

class MainGradlePlugin: Plugin<Project>  {

    private val CONFIG_FILE_NAME: String = "bnotify-config.json"

    override fun apply(project: Project) {
        println("✅ Custom Plugin Applied!")
        // Your plugin logic here
//        checkJsonFileConfig(project)
        checkAndGenerateConfig(project)
    }

    private fun checkJsonFileConfig(project: Project){
        System.out.println("✅ FileCheckPlugin has been applied"); // ADD THIS
        project.afterEvaluate {
            var path: File = File("${rootDir}/app/")
            val requiredFile: File = project.file("${path}/${CONFIG_FILE_NAME}")
//            System.out.println("🔍 Checking file: " + requiredFile.getAbsolutePath()); // ADD THIS
            if (!requiredFile.exists()) {
                throw RuntimeException("CompileTimeException: Required \"BerryNotifierConfig\" file not found: ${path.path}/${CONFIG_FILE_NAME}")
            } else {
                System.out.println("✅ Found required file: ${CONFIG_FILE_NAME}");
            }
        }
    }

    private fun checkAndGenerateConfig(project: Project) {
        val jsonFile = File("${project.rootDir}/app/${CONFIG_FILE_NAME}")
        if (!jsonFile.exists()) {
            throw RuntimeException("${CONFIG_FILE_NAME} not found in app directory!")
        }

        val jsonContent = jsonFile.readText()
        println("✅ Found ${CONFIG_FILE_NAME}, generating source file...")

        // Parse JSON using Kotlinx Serialization
        val config = try {
            Json.decodeFromString<BnotifyConfig>(jsonContent)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse ${CONFIG_FILE_NAME}: ${e.message}")
        }

        val outputDir = File(project.buildDir, "generated/source/config")
        val packageName = "com.example.mycustomlib.config"
        val className = "GeneratedConfig"

        val configFile = File(outputDir, "$className.kt")

        configFile.writeText(
            """
        package $packageName

        object $className {
            var JSON = ${jsonContent.trim().quoteForKotlin()}
            var projectId = "${config.projectId}"
            var packageName = "${config.packageName}"
            var apiKey = "${config.apiKey}"
            var authDomain = "${config.authDomain}"
            var databaseURL = "${config.databaseURL}"
            var storageBucket = "${config.storageBucket}"
            var messagingSenderId = "${config.messagingSenderId}"
            var appId = "${config.appId}"
            var measurementId = "${config.measurementId}"
        }
        """.trimIndent()
        )

        // Tell Gradle to include this source directory in the compilation
        project.extensions.findByName("android").let { androidExt->
            val android = androidExt as com.android.build.gradle.BaseExtension
            android.sourceSets.getByName("main").java.srcDir(outputDir)
        }

    }

    private fun String.quoteForKotlin(): String {
        // Escape string for Kotlin string literal
        return "\"${this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
    }

}