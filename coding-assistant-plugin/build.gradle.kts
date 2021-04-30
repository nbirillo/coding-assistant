group = rootProject.group
version = rootProject.version

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0" apply true
}

dependencies {
    implementation(project(":coding-assistant-core"))
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

/**
 * Gradle task to build and serialize solution space into `output` directory.
 * input is a directory with a name of the task containing dataset .csv files.
 */
open class SolutionSpaceCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Input directory with csv files
    @get:Input
    val input: String? by project

    // Output directory
    @get:Input
    val output: String? by project

    init {
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

/**
 * Generates hint report using implemented algorithm.
 */
open class HintGenerationCliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    // Path to the serialized solution space file.
    @get:Input
    val solutionSpacePath: String? by project

    // Path to support information about dataset original code fragments.
    // Needed for only report.
    @get:Input
    val codeRepositoryPath: String? by project

    // Name of the task.
    @get:Input
    val taskName: String? by project

    // Directory to store the report.
    @get:Input
    val outputDir: String? by project

    @get:Input
    val fragmentPath: String? by project

    init {
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<SolutionSpaceCliTask>("solutionSpaceCli") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "solution-space",
            input?.let { "--input_path=$it" },
            output?.let { "--output_path=$it" }
        )
    }

    register<HintGenerationCliTask>("hintGenerationCli") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "hint-generation",
            solutionSpacePath?.let { "--space_path=$it" },
            codeRepositoryPath?.let { "--code_repository_path=$it" },
            outputDir?.let { "--output_path=$it" },
            fragmentPath?.let { "--fragment_path=$it" },
            taskName?.let { "--task_name=$it" }
        )
    }
}
