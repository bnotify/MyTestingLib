package com.example.mycustomplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class MainGradlePlugin : Plugin<Project> {
    companion object {
        const val CONFIG_FILE_NAME = "bnotify-config.json"
        const val EXTENSION_NAME = "bnotify"
    }

    override fun apply(project: Project) {
        // Create extension for configuration
        val extension = project.extensions.create(EXTENSION_NAME, BnotifyExtension::class.java)

        // Register the task
        val generateTask = project.tasks.register("generateBnotifyConfig", GenerateBnotifyConfigTask::class.java) {
            group = "build"
            description = "Generates Bnotify configuration from JSON"

            // Configure defaults but allow override via extension
            configPath.convention(project.layout.projectDirectory.file("app/$CONFIG_FILE_NAME"))
            outputDir.convention(project.layout.buildDirectory.dir("generated/source/bnotify"))
            packageName.convention("com.example.mycustomlib.config")
        }

        // Hook into Android plugin if present
        project.plugins.withId("com.android.application") {
            project.afterEvaluate {
                // Validate config file exists before build
                project.tasks.named("preBuild") {
                    dependsOn(generateTask)
                }
            }
        }

        project.plugins.withId("com.android.library") {
            project.afterEvaluate {
                // Validate config file exists before build
                project.tasks.named("preBuild") {
                    dependsOn(generateTask)
                }
            }
        }
    }
}

open class GenerateBnotifyConfigTask : DefaultTask() {
    @get:InputFile
    val configPath: RegularFileProperty = project.objects.fileProperty()

    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @get:Input
    val packageName: Property<String> = project.objects.property(String::class.java)

    @Suppress("NewApi")
    @TaskAction
    fun generate() {
        val configFile = configPath.get().asFile
        if (!configFile.exists()) {
            throw GradleException("bnotify-config.json not found in ${configFile.parent}. Please add the file to your app module.")
        }

        val jsonContent = configFile.readText()
        val config = try {
            Json.decodeFromString<BnotifyConfig>(jsonContent)
        } catch (e: Exception) {
            throw GradleException("Failed to parse bnotify-config.json: ${e.message}")
        }

        val outputDirFile = outputDir.get().asFile
        val packageDir = packageName.get().replace(".", "/")

        File(outputDirFile, "$packageDir/GeneratedConfig.kt").apply {
            parentFile.mkdirs()
            writeText("""
                package ${packageName}
                
                internal object GeneratedConfig {
                    const val projectId = "${config.projectId}"
                    const val packageName = "${config.packageName}"
                    const val apiKey = "${config.apiKey}"
                    ${config.authDomain?.let { "const val authDomain = \"$it\"" } ?: "const val authDomain: String? = null"}
                    ${config.databaseURL?.let { "const val databaseURL = \"$it\"" } ?: "const val databaseURL: String? = null"}
                    ${config.storageBucket?.let { "const val storageBucket = \"$it\"" } ?: "const val storageBucket: String? = null"}
                    ${config.messagingSenderId?.let { "const val messagingSenderId = \"$it\"" } ?: "const val messagingSenderId: String? = null"}
                    const val appId = "${config.appId}"
                    ${config.measurementId?.let { "const val measurementId = \"$it\"" } ?: "const val measurementId: String? = null"}
                    
                    const val JSON = \"\"\"$jsonContent\"\"\"
                }
            """.trimIndent())
        }
    }
}

open class BnotifyExtension {
    var configPath: String? = null
    var packageName: String? = null
}