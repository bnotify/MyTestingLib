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
        targetSdk?.let {
            if (it >= 33) {
                manifestPlaceholders["exactAlarmPermission"] = "android.permission.USE_EXACT_ALARM"
            } else {
                manifestPlaceholders["exactAlarmPermission"] = ""
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("build/generated/source/bnotify")
        }
    }

    buildFeatures {
        buildConfig = true   // ensures BuildConfig is generated
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.bnotify"
            artifactId = "mycustomlib"
            version = "1.2.5"

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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.serialization.json)
    //Image processing libraray
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.okhttp3.integration)

    //http request library
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    //socket request library
    implementation("io.socket:socket.io-client:2.1.0") {
        // Excluding org.json which conflicts with Android's built-in org.json
        exclude("org.json","json");
    }
}
