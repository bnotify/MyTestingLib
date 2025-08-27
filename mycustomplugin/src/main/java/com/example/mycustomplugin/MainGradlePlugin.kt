
package com.example.mycustomplugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File


@Serializable
internal data class BnotifyConfig(
    val projectId: String,
    val packageName: String,
    val apiKey: String,
    val authDomain: String? = null,
    val databaseURL: String? = null,
    val storageBucket: String? = null,
    val messagingSenderId: String? = null,
    val appId: String,
    val measurementId: String? = null,
    val fcmAppId: String,
    val fcmProjectId: String,
    val fcmApiKey: String,
    val fcmSenderId: String,
)

class MainGradlePlugin: Plugin<Project>  {

    private val CONFIG_FILE_NAME: String = "bnotify-config.json"

    override fun apply(project: Project) {
        println("✅ Custom Plugin Applied!")
        // Your plugin logic here
//        checkJsonFileConfig(project)
        checkAndGenerateConfig(project)
    }

    /*private fun checkAndGenerateConfig(project: Project) {
        val outputDir = File(project.layout.buildDirectory.get().asFile, "generated/source/config")
        val packageName = "com.example.mycustomlib.config"
        val className = "GeneratedConfig"

        // Register the task
        val generateTask = project.tasks.register("generateConfig") {
            outputs.dir(outputDir) // tell Gradle this task produces files here

            doLast {
                val jsonFile = File("${project.rootDir}/app/$CONFIG_FILE_NAME")
                if (!jsonFile.exists()) {
                    throw RuntimeException("$CONFIG_FILE_NAME not found in app directory!")
                }

                val jsonContent = jsonFile.readText()
                val config = Json.decodeFromString<BnotifyConfig>(jsonContent)

                val configFile = File(outputDir, "$className.kt")
                configFile.parentFile.mkdirs()
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
            }
        }

        // ✅ Add generated dir to sources DURING CONFIGURATION
        project.extensions.findByName("android")?.let { androidExt ->
            val android = androidExt as com.android.build.gradle.BaseExtension
            android.sourceSets.getByName("main").java.srcDir(outputDir)
        }

        // Ensure generation happens before compile
        project.tasks.named("preBuild") { dependsOn(generateTask) }
    }*/

    /*private fun checkAndGenerateConfig(project: Project) {
        val packagePath = "com/example/mycustomlib/config"
        val outputFile = File(project.projectDir, "src/main/java/$packagePath/GeneratedConfig.kt")

        val generateTask = project.tasks.register("generateConfig") {
            doLast {
                val jsonFile = File("${project.rootDir}/app/$CONFIG_FILE_NAME")
                if (!jsonFile.exists()) {
                    println("❌ No $CONFIG_FILE_NAME found, keeping default GeneratedConfig.")
                    throw GradleException("❌ No $CONFIG_FILE_NAME found, keeping default GeneratedConfig.")
//                    return@doLast
                }

                val jsonContent = jsonFile.readText()
                val config = Json.decodeFromString<BnotifyConfig>(jsonContent)

                outputFile.parentFile.mkdirs()
                outputFile.writeText(
                    """
                package com.example.mycustomlib.config

                object GeneratedConfig {
                    val JSON: String? = ${jsonContent.trim().quoteForKotlin()}
                    val projectId: String? = "${config.projectId}"
                    val packageName: String? = "${config.packageName}"
                    val apiKey: String? = "${config.apiKey}"
                    val authDomain: String? = "${config.authDomain}"
                    val databaseURL: String? = "${config.databaseURL}"
                    val storageBucket: String? = "${config.storageBucket}"
                    val messagingSenderId: String? = "${config.messagingSenderId}"
                    val appId: String? = "${config.appId}"
                    val measurementId: String? = "${config.measurementId}"
                    val fcmAppId: String? = "${config.fcmAppId}"
                    val fcmProjectId: String? = "${config.fcmProjectId}"
                    val fcmApiKey: String? = "${config.fcmApiKey}"
                    val fcmSenderId: String? = "${config.fcmSenderId}"
                }
                """.trimIndent()
                )
            }
        }

        // Ensure generation runs before compileKotlin
        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            dependsOn(generateTask)
        }
    }*/

    private fun checkAndGenerateConfig(project: Project) {
        val generateTask = project.tasks.register("generateConfig") {
            doLast {
                val jsonFile = File("${project.rootDir}/app/$CONFIG_FILE_NAME")
                if (!jsonFile.exists()) {
                    throw GradleException("❌ No $CONFIG_FILE_NAME found in app directory!")
                }

                // Read app's applicationId from Android config
                val android = project.extensions.findByName("android") as? com.android.build.gradle.AppExtension
                    ?: throw GradleException("Android extension not found!")

                val appPackageName = android.defaultConfig.applicationId
                val packagePath = appPackageName?.replace('.', '/') + "/config"
                val packageName = "$appPackageName.config"

                val outputFile = File(project.projectDir, "src/main/java/$packagePath/GeneratedConfig.kt")

                val jsonContent = jsonFile.readText()
                val config = Json.decodeFromString<BnotifyConfig>(jsonContent)

                outputFile.parentFile.mkdirs()
                outputFile.writeText(
                    """
                package $packageName

                object GeneratedConfig {
                    val JSON: String? = ${jsonContent.trim().quoteForKotlin()}
                    val projectId: String? = "${config.projectId}"
                    val packageName: String? = "${config.packageName}"
                    val apiKey: String? = "${config.apiKey}"
                    val authDomain: String? = "${config.authDomain}"
                    val databaseURL: String? = "${config.databaseURL}"
                    val storageBucket: String? = "${config.storageBucket}"
                    val messagingSenderId: String? = "${config.messagingSenderId}"
                    val appId: String? = "${config.appId}"
                    val measurementId: String? = "${config.measurementId}"
                    val fcmAppId: String? = "${config.fcmAppId}"
                    val fcmProjectId: String? = "${config.fcmProjectId}"
                    val fcmApiKey: String? = "${config.fcmApiKey}"
                    val fcmSenderId: String? = "${config.fcmSenderId}"
                }
                """.trimIndent()
                )
            }
        }

        // Ensure generation runs before compile
        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            dependsOn(generateTask)
        }
    }



    private fun String.quoteForKotlin(): String {
        // Escape string for Kotlin string literal
        return "\"${this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
    }

}
