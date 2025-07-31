package com.example.mycustomplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class MainGradlePlugin: Plugin<Project>  {
    override fun apply(project: Project) {
        println("✅ Custom Plugin Applied!")
        // Your plugin logic here
        checkJsonFileConfig(project)
    }

    private fun checkJsonFileConfig(project: Project){
        System.out.println("✅ FileCheckPlugin has been applied"); // ADD THIS
        project.afterEvaluate {
            var path: File = File("${rootDir}/app/")
            val requiredFile: File = project.file("${path}/BerryNotifierConfig.json")
//            System.out.println("🔍 Checking file: " + requiredFile.getAbsolutePath()); // ADD THIS
            if (!requiredFile.exists()) {
                throw RuntimeException("CompileTimeException: Required \"BerryNotifierConfig\" file not found: ${path.path}/BerryNotifierConfig.json")
            } else {
                System.out.println("✅ Found required file: BerryNotifierConfig.json");
            }
        }
    }
}