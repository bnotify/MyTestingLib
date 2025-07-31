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
            id = "com.example.mycustomplugin"
            implementationClass = "com.example.MainGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.example"
            artifactId = "mycustomplugin"
            version = "1.0.0"
        }
    }
    repositories {
        maven {
            url = uri("$rootDir/build/repo")
        }
    }
}
