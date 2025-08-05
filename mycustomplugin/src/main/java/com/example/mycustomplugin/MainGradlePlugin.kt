package com.example.mycustomplugin

import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

class MainGradlePlugin : Plugin<Project> {
    companion object {
        const val CONFIG_FILE_NAME = "bnotify-config.json"
    }

    override fun apply(project: Project) {
        // Only apply the config generation to application modules
        project.plugins.withId("com.android.application") {
            configureForApplication(project)
        }
    }

    private fun configureForApplication(project: Project) {
        // Register the task first
        val generateTask = project.tasks.register("generateBnotifyConfig", GenerateBnotifyConfigTask::class.java) {
            group = "build"
            description = "Generates Bnotify configuration from JSON"

            configPath.set(project.rootProject.file("app/$CONFIG_FILE_NAME"))
            outputDir.set(project.layout.buildDirectory.dir("generated/source/bnotify"))
            packageName.set("com.example.mycustomlib.config")

            doFirst {
                if (!configPath.get().asFile.exists()) {
                    throw GradleException(
                        "$CONFIG_FILE_NAME not found in app directory!\n" +
                                "Please add the bnotify-config.json file to your app module's directory."
                    )
                }
            }
        }

        // Correct way to add the dependency
        project.afterEvaluate {
            project.tasks.named("preBuild") {
                dependsOn(generateTask)
            }
        }
    }
}

abstract class GenerateBnotifyConfigTask : DefaultTask() {
    @get:InputFile
    @get:Optional  // Mark as optional to avoid configuration-time validation
    abstract val configPath: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @TaskAction
    fun generate() {
        val jsonFile = configPath.get().asFile
        val jsonContent = jsonFile.readText()

        // Parse the JSON content into BnotifyConfig object
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
            |package ${packageName.get()}
            |
            |internal object GeneratedConfig {
            |    // Raw JSON content
            |    const val JSON = ${jsonContent.trimIndent().quoteForKotlin()}
            |
            |    // Parsed fields
            |    const val projectId: String = "${config.projectId}"
            |    const val packageName: String = "${config.packageName}"
            |    const val apiKey: String = "${config.apiKey}"
            |    ${config.authDomain?.let { "const val authDomain: String = \"$it\"" } ?: "const val authDomain: String? = null"}
            |    ${config.databaseURL?.let { "const val databaseURL: String = \"$it\"" } ?: "const val databaseURL: String? = null"}
            |    ${config.storageBucket?.let { "const val storageBucket: String = \"$it\"" } ?: "const val storageBucket: String? = null"}
            |    ${config.messagingSenderId?.let { "const val messagingSenderId: String = \"$it\"" } ?: "const val messagingSenderId: String? = null"}
            |    const val appId: String = "${config.appId}"
            |    ${config.measurementId?.let { "const val measurementId: String = \"$it\"" } ?: "const val measurementId: String? = null"}
            |}
        """.trimMargin())
        }
    }

    private fun String.quoteForKotlin(): String {
        return "\"\"\"${this.replace("\"\"\"", "\\\"\\\"\\\"")}\"\"\""
    }
}