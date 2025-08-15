plugins {
    `kotlin-dsl` // Required for custom Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

//val kotlinVersion = "2.0.21" // Define once for consistency
val myPluginVersion = "1.2.0" // Define once for consistency

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
//    implementation("com.android.tools.build:gradle:8.4.0")// AGP version
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Compatible with Kotlin 2.0.x
//
//    // Required for proper Gradle plugin development
//    implementation(gradleApi())
//    implementation(localGroovy())
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:8.11.1")
    implementation("com.google.code.gson:gson:2.13.1")
}

gradlePlugin {
    plugins {
        create("mycustomplugin") {
            id = "com.github.bnotify.mycustomplugin"
//            id = "com.example.mycustomplugin" // Match your package
            implementationClass = "com.example.mycustomplugin.MainGradlePlugin"
            version = myPluginVersion// Add version here
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.github.bnotify"
            artifactId = "mycustomplugin"
            version = myPluginVersion

            artifact(tasks.register("pluginMarker", Jar::class) {
                archiveClassifier.set("plugin-marker")
            })
        }
    }
    repositories {
        maven {
            url = uri("$rootDir/build/repo")
        }
    }
}

// Configure Java compatibility
//java {
//    sourceCompatibility = JavaVersion.VERSION_17
//    targetCompatibility = JavaVersion.VERSION_17
//}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    compilerOptions {
//        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
//    }
//}

sourceSets["main"].java.srcDirs("src/main/kotlin")

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
