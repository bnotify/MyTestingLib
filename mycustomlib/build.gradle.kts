plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
//    id("com.example.mycustomplugin")
}

android {
    namespace = "com.example.mycustomlib"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("build/generated/source/bnotify")
        }
    }
}

//publishing {
//    publications {
//        create<MavenPublication>("release") {
//            groupId = "com.github.bnotify"
//            artifactId = "mycustomlib"
//            version = "1.0.7"
//
//            afterEvaluate {
//                from(components["release"])
//            }
//        }
//    }
//    repositories {
//        maven {
//            url = uri("$rootDir/build/repo")
//        }
//    }
//}

dependencies {
    implementation(libs.androidx.core.ktx)
}
