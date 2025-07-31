plugins {
    `kotlin-dsl` // Required for custom Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
//    implementation(project(":mycustomlib"))
//    implementation("com.example:mycustomlib:1.0.0")
}

gradlePlugin {
    plugins {
        create("mycustomplugin") {
            id = "com.github.hamzahafeez93.mycustomplugin"
            implementationClass = "com.example.MainGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.github.hamzahafeez93"
            artifactId = "MyTestingLib-mycustomplugin"
            version = "1.0.0"
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
