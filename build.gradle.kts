plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.foldright.auto-pipeline"
version = "0.4.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.8.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.2")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2")
    type.set("IC") // Target IDE Platform

    // Plugin Dependencies
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
    buildSearchableOptions {
        enabled = false
    }

    runIde {
        systemProperty("idea.is.internal", "true")
        autoReloadPlugins.set(true)
    }

    // see https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html#platformVersions
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("233")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
