group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization") version "1.4.30" apply true
}

dependencies {
    api("org.jetbrains.research.ml.ast.transformations:ast-transformations-core") {
        version {
            branch = "develop-8"
        }
    }
    api("org.jgrapht:jgrapht-core:1.4.0")
    api("org.jgrapht:jgrapht-ext:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    implementation(group = "de.siegmar", name = "fastcsv", version = "2.0.0")
    api("com.github.gumtreediff", "core", "2.1.2")
}

tasks {
    jar {
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }
}
