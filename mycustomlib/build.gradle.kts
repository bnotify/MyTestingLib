plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}


android {
    namespace = "com.example.mycustomlib"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.example"
            artifactId = "mycustomlib"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            url = uri("$rootDir/build/repo")
        }
    }
}