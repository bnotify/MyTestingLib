package com.example.mycustomplugin

import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import kotlinx.serialization.Serializable
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

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
//        checkAndGenerateConfig(project)
        // Only apply the config generation to application modules
        project.plugins.withId("com.android.application") {
            configureForApplication(project)
        }
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

    private fun configureForApplication(project: Project) {
        // Register the task first
        val generateTask = project.tasks.register("generateBnotifyConfig", GenerateBnotifyConfigTask::class.java) {
            group = "build"
            description = "Generates Bnotify configuration from JSON"

            val jsonFile = File("${project.rootDir}/app/${CONFIG_FILE_NAME}")
            if (!jsonFile.exists()) {
                throw RuntimeException("${CONFIG_FILE_NAME} not found in app directory!")
            }
            configPath.set(jsonFile)
            outputDir.set(project.layout.buildDirectory.dir("generated/source/bnotify"))
            packageName.set("com.example.mycustomlib.config")

            doFirst {
                if (!configPath.get().asFile.exists()) {
                    throw GradleException(
                        "$CONFIG_FILE_NAME not found in app directory!\n" +
                                "Please add the ${CONFIG_FILE_NAME} file to your app module's directory."
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

    private fun String.quoteForKotlin(): String {
        // Escape string for Kotlin string literal
        return "\"${this.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""
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
            package $packageName

            object GeneratedConfig {
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
        """.trimMargin())
        }
    }

    private fun String.quoteForKotlin(): String {
        return "\"\"\"${this.replace("\"\"\"", "\\\"\\\"\\\"")}\"\"\""
    }
}