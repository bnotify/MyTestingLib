package com.example.mycustomplugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

// JSON data class
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

class MainGradlePlugin : Plugin<Project> {
    companion object {
        const val CONFIG_FILE_NAME = "bnotify-config.json"
    }

    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {
            configureForApplication(project)
        }
    }

    private fun configureForApplication(project: Project) {
        val generateTask = project.tasks.register("generateBnotifyXml", GenerateBnotifyConfigTask::class.java) {
            group = "build"
            description = "Generates bnotify_config.xml from JSON"

            val jsonFile = File("${project.rootDir}/app/${CONFIG_FILE_NAME}")
            if (!jsonFile.exists()) {
                throw GradleException("${CONFIG_FILE_NAME} not found in app directory!")
            }
            configPath.set(jsonFile)

            // Directly point to library module's res folder
            val libResDir = File(project.rootDir, "mycustomlib/src/main/res")
            outputDir.set(libResDir)

            packageName.set("com.example.mycustomlib")
        }

        project.afterEvaluate {
            // Ensure we run before resources are merged
            project.tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Resources") }
                .configureEach { dependsOn(generateTask) }
        }
    }
}

abstract class GenerateBnotifyConfigTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    abstract val configPath: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @TaskAction
    fun generate() {
        val jsonFile = configPath.get().asFile
        println("✅ Found $jsonFile, generating XML...")

        val jsonContent = jsonFile.readText()

        val config = try {
            Json.decodeFromString<BnotifyConfig>(jsonContent)
        } catch (e: Exception) {
            throw GradleException("Failed to parse $jsonFile: ${e.message}")
        }

        // Ensure res/xml exists in the library
        val xmlDir = File(outputDir.get().asFile, "xml")
        if (!xmlDir.exists()) xmlDir.mkdirs()

        val xmlFile = File(xmlDir, "bnotify_config.xml")
        xmlFile.writeText(buildXml(config))

        println("✅ bnotify_config.xml generated at: ${xmlFile.absolutePath}")
    }

    private fun buildXml(config: BnotifyConfig): String {
        return """
        <?xml version="1.0" encoding="utf-8"?>
        <bnotifyConfig>
            <projectId>${config.projectId}</projectId>
            <packageName>${config.packageName}</packageName>
            <apiKey>${config.apiKey}</apiKey>
            ${config.authDomain?.let { "<authDomain>$it</authDomain>" } ?: ""}
            ${config.databaseURL?.let { "<databaseURL>$it</databaseURL>" } ?: ""}
            ${config.storageBucket?.let { "<storageBucket>$it</storageBucket>" } ?: ""}
            ${config.messagingSenderId?.let { "<messagingSenderId>$it</messagingSenderId>" } ?: ""}
            <appId>${config.appId}</appId>
            ${config.measurementId?.let { "<measurementId>$it</measurementId>" } ?: ""}
        </bnotifyConfig>
    """.trimIndent()
    }
}
