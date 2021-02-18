# Coding-assistant

A PyCharm plugin for generating the personalized hints

## Installation

Just clone the repo by `git clone https://github.com/nbirillo/coding-assistant.git` and run `./gradlew build shadowJar` to build a .zip distribution of the plugin. 
The .zip is located in `build/distributions/coding-assistant-1.0-SNAPSHOT.zip`. Then __install the plugin from disk__ into an IntelliJ-based IDE of your choice
(see [this guide](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk) for example). 

## Getting started

To run the plugin run `runIde` Gradle task provided by [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin).

To add `git hook` for auto-formatting the project according to the code style guide before each commit 
run `./gradlew addKtlintFormatGitPreCommitHook`. As the result, the `.git` folder will contain the necessary hook.


## Build a solution space

Run the command `./gradlew :solution-space-cli -Pinput=<Input directory with csv files> -Poutput=<Output directory>`.
