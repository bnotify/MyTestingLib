plugins {
    `kotlin-dsl` // Required for custom Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

dependencies {
    implementation("com.android.tools.build:gradle:8.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

gradlePlugin {
    plugins {
        create("mycustomplugin") {
            id = "com.github.bnotify.mycustomplugin"
            implementationClass = "com.example.mycustomplugin.MainGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.github.bnotify"
            artifactId = "mycustomplugin"
            version = "1.0.7"
        }
    }
    repositories {
        maven {
            url = uri("$rootDir/build/repo")
        }
    }
}

//./gradlew :mycustomlib:publish
//./gradlew :mycustomplugin:publish
//https://github.com/hamzahafeez93/MyTestingLib

sourceSets["main"].java.srcDirs("src/main/kotlin")

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
