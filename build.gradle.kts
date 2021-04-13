import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    val kotlinVersion = "1.4.30"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.intellij") version "0.7.2"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.15.0"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
    }

    ktlint {
        enableExperimentalRules.set(true)
    }

    detekt {
        config = files("../detekt-config.yml")
        buildUponDefaultConfig = true

        reports {
            html.enabled = false
            xml.enabled = false
            txt.enabled = false
        }
    }
}

subprojects {
    group = "io.github.nbirillo.coding.assistant"
    version = "1.0-SNAPSHOT"
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("org.jetbrains.intellij")
        plugin("com.github.johnrengelman.shadow")
        plugin("org.jetbrains.dokka")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    intellij {
        type = "PC"
        version = "2020.3.3"
        downloadSources = false
        setPlugins("PythonCore")
        updateSinceUntilBuild = true
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "11"
            targetCompatibility = "11"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }
        // According to this topic:
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010164960-Build-Intellij-plugin-in-IDEA-2019-1-2020-3?page=1#community_comment_360002517940
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }

        jar {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        implementation("org.jetbrains.research.ml.ast.transformations:ast-transformations-core") {
            version {
                branch = "master"
            }
        }
        implementation("org.jgrapht:jgrapht-core:1.1.0")
        implementation("org.jgrapht:jgrapht-ext:1.1.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.1.0")

        implementation(group = "de.siegmar", name = "fastcsv", version = "2.0.0")
        implementation("com.github.gumtreediff", "core", "2.1.2")

        implementation("com.xenomachina:kotlin-argparser:2.0.7")
    }
}
