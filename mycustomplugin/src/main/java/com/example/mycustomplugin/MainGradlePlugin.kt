package com.github.bnotify.mycustomplugin

import org.gradle.api.*
import org.gradle.api.file.Directory
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.register
import java.io.File
import com.google.gson.Gson
import org.gradle.kotlin.dsl.configure

open class MainGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val configFile = project.file("app/bnotify-config.json")
        val outputDir: Directory = project.layout.buildDirectory.dir("generated/source/config").get()
        val generatedFile = outputDir.file("GeneratedConfig.kt").asFile

        // Task: Generate config
        val generateConfig = project.tasks.register<GenerateConfigTask>("generateBnotifyConfig") {
            this.configFile = configFile
            this.outputFile = generatedFile
        }

        // Make compile tasks depend on generation
        project.plugins.withId("com.android.application") {
            project.extensions.configure<com.android.build.gradle.BaseExtension> {
                sourceSets.getByName("main").java.srcDir(outputDir)

                // For every build type, hook before compilation
                project.afterEvaluate {
                    project.tasks.matching { it.name.startsWith("compile") && it.name.endsWith("Kotlin") }
                        .configureEach {
                            dependsOn(generateConfig)
                        }
                }
            }
        }
    }
}

// ---------------- Task ----------------
abstract class GenerateConfigTask : DefaultTask() {

    @get:InputFile
    lateinit var configFile: File

    @get:OutputFile
    lateinit var outputFile: File

    @TaskAction
    fun generate() {
        if (!configFile.exists()) {
            logger.warn("⚠ bnotify-config.json not found at: ${configFile.absolutePath}")
            return
        }

        val jsonData = configFile.readText()
        val gson = Gson()
        val data: Map<String, Any?> = gson.fromJson(jsonData, Map::class.java) as Map<String, Any?>

        val content = buildString {
            appendLine("package com.example.mycustomlib.config")
            appendLine()
            appendLine("object GeneratedConfig {")
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
}