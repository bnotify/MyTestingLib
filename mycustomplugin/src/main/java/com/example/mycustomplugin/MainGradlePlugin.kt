@file:Suppress("UNCHECKED_CAST")

package com.example.mycustomplugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.BaseExtension
import org.gradle.api.*
import org.gradle.api.file.Directory
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.register
import java.io.File
import com.google.gson.Gson
import org.gradle.kotlin.dsl.configure

open class MainGradlePlugin : Plugin<Project> {

    private val CONFIG_FILE_NAME: String = "bnotify-config.json"

    override fun apply(project: Project) {
        // Directory for generated Kotlin source
        val outputDir: Directory = project.layout
            .buildDirectory
            .dir("generated/source/config")
            .get()

        val generatedFile = outputDir.file("GeneratedConfig.kt").asFile
        val configFile = project.file(CONFIG_FILE_NAME)

        // Register custom task
        val generateConfigTask = project.tasks.register(
            "generateBnotifyConfig",
            GenerateConfigTask::class.java
        ) {
            this.configFile = configFile
            this.outputFile = generatedFile
        }

        // Hook into Android build
        project.plugins.withId("com.android.application") {
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                // Tell AGP where generated sources live
                variant.sources.java?.addStaticSourceDirectory(outputDir.toString())

                // Ensure compile tasks depend on the generator
                project.tasks.named("compile${variant.name.replaceFirstChar { it.uppercase() }}Kotlin") {
                    dependsOn(generateConfigTask)
                }
            }
        }
    }
}

// ---------------- Task ----------------
abstract class GenerateConfigTask : DefaultTask() {

    private val CONFIG_FILE_NAME: String = "bnotify-config.json"

    @get:InputFile
    lateinit var configFile: File

    @get:OutputFile
    lateinit var outputFile: File

    @TaskAction
    fun generate() {
        if (!configFile.exists()) {
            logger.warn("❌ ${CONFIG_FILE_NAME} not found at: ${configFile.absolutePath}")
            throw GradleException("❌ ${CONFIG_FILE_NAME} not found at: ${configFile.absolutePath}")
        }

        val jsonData = configFile.readText()
        val gson = Gson()
        val data: Map<String, Any?> = gson.fromJson(jsonData, Map::class.java) as Map<String, Any?>

        val content = buildString {
            appendLine("package com.example.mycustomlib.config")
            appendLine()
            appendLine("object GeneratedConfig {")
            appendLine("val JSON = ${jsonData.trim().quoteForKotlin()}")
            for ((key, value) in data) {
                val safeValue = value?.toString()?.replace("\"", "\\\"") ?: ""
                appendLine("    val ${key} = \"$safeValue\"")
            }
            appendLine("}")
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(content)
        logger.lifecycle("✅ Generated config at: ${outputFile.absolutePath}")
    }




    private fun String.quoteForKotlin(): String {
        // Escape string for Kotlin string literal
        return "\"${this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
    }
}