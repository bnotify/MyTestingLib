plugins {
    `kotlin-dsl` // Required for custom Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.example"
version = "1.0.2"

dependencies {
//    implementation(project(":mycustomlib"))
//    implementation("com.example:mycustomlib:1.0.0")
}

gradlePlugin {
    plugins {
        create("mycustomplugin") {
            id = "com.example.mycustomplugin"
            implementationClass = "com.example.mycustomplugin.MainGradlePlugin"
        }
    }
}

//publishing {
//    publications {
//        create<MavenPublication>("pluginMaven") {
//            groupId = "com.github.hamzahafeez93"
//            artifactId = "MyTestingLib-mycustomplugin"
//            version = "1.0.0"
//        }
//    }
//    repositories {
//        maven {
//            url = uri("$rootDir/build/repo")
//        }
//    }
//}



//./gradlew :mycustomlib:publish
//./gradlew :mycustomplugin:publish
//https://github.com/hamzahafeez93/MyTestingLib

sourceSets["main"].java.srcDirs("src/main/kotlin")

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
