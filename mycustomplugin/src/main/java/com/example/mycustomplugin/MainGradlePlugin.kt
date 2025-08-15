package com.example.mycustomplugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import java.io.File


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

    /*private fun checkAndGenerateConfig(project: Project) {
        val packagePath = "com/example/mycustomlib/config"
        val outputFile = File(project.projectDir, "src/main/java/$packagePath/GeneratedConfig.kt")

        val generateTask = project.tasks.register("generateConfig") {
            doLast {
                val jsonFile = File("${project.rootDir}/app/$CONFIG_FILE_NAME")
                if (!jsonFile.exists()) {
                    println("❌ ${CONFIG_FILE_NAME} not found in '/app' directory. Please add the file to your app module.")
                    throw GradleException("${CONFIG_FILE_NAME} not found in '/app' directory. Please add the file to your app module.")
                    return@doLast
                }

                println("✅ Found required file: ${CONFIG_FILE_NAME}");

                val jsonContent = jsonFile.readText()
                val config = Json.decodeFromString<BnotifyConfig>(jsonContent)

                println("✅ Reading: ${CONFIG_FILE_NAME} file");

                println("✅ Generating: GeneratedConfig.kt Class");

                outputFile.parentFile.mkdirs()
                outputFile.writeText(
                    """
                package com.example.mycustomlib.config

                object GeneratedConfig {
                    var JSON: String? = ${jsonContent.trim().quoteForKotlin()}
                    var projectId: String? = "${config.projectId}"
                    var packageName: String? = "${config.packageName}"
                    var apiKey: String? = "${config.apiKey}"
                    var authDomain: String? = "${config.authDomain}"
                    var databaseURL: String? = "${config.databaseURL}"
                    var storageBucket: String? = "${config.storageBucket}"
                    var messagingSenderId: String? = "${config.messagingSenderId}"
                    var appId: String? = "${config.appId}"
                    var measurementId: String? = "${config.measurementId}"
                }
                """.trimIndent()
                )

                println("✅ GeneratedConfig.kt is generated successfully")
            }
        }

        // Ensure generation runs before compileKotlin
        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            dependsOn(generateTask)
        }
    }*/

    private fun checkAndGenerateConfig(project: Project) {
        project.afterEvaluate {
            val generateTask = tasks.register("generateConfig") {
                inputs.file(File("${project.rootDir}/app/$CONFIG_FILE_NAME"))
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                    .withPropertyName(CONFIG_FILE_NAME)

                outputs.dir(File(project.layout.buildDirectory.get().asFile, "generated/source/config"))

                doLast {
                    val jsonFile = File("${project.rootDir}/app/$CONFIG_FILE_NAME")
                    if (!jsonFile.exists()) {
                        throw GradleException("$CONFIG_FILE_NAME not found in app directory!")
                    }

                    // Keep checksum to detect mid-build changes
                    val initialChecksum = jsonFile.readBytes().contentHashCode()

                    val jsonContent = jsonFile.readText()
                    val config = try {
                        Json.decodeFromString<BnotifyConfig>(jsonContent)
                    } catch (e: Exception) {
                        throw GradleException("Failed to parse $CONFIG_FILE_NAME: ${e.message}")
                    }

                    // Release-mode strict validation
                    if (project.gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }) {
                        validateReleaseConfig(config)
                    }

                    // Re-check checksum after reading (to avoid mid-build changes)
                    val finalChecksum = jsonFile.readBytes().contentHashCode()
                    if (initialChecksum != finalChecksum) {
                        throw GradleException("$CONFIG_FILE_NAME changed during build — please rebuild.")
                    }

                    // Generate file
                    val outputDir = File(project.layout.buildDirectory.get().asFile, "generated/source/config")
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

                    // Add generated source dir
                    project.extensions.findByName("android")?.let { androidExt ->
                        val android = androidExt as com.android.build.gradle.BaseExtension
                        android.sourceSets.getByName("main").java.srcDir(outputDir)
                    }
                }
            }

            tasks.named("preBuild") { dependsOn(generateTask) }
        }
    }

    private fun validateReleaseConfig(config: BnotifyConfig) {
        val requiredFields = mapOf(
            "projectId" to config.projectId,
            "packageName" to config.packageName,
            "apiKey" to config.apiKey,
            "appId" to config.appId
        )

        val missing = requiredFields.filter { it.value.isNullOrBlank() }.keys
        if (missing.isNotEmpty()) {
            throw GradleException("Release build failed: Missing required config fields: ${missing.joinToString(", ")}")
        }
    }


    private fun String.quoteForKotlin(): String {
        // Escape string for Kotlin string literal
        return "\"${this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
    }

}