import java.net.URI

rootProject.name = "coding-assistant"

include(
    "coding-assistant-core",
    "coding-assistant-plugin"
)

sourceControl {
    gitRepository(URI.create("https://github.com/JetBrains-Research/ast-transformations.git")) {
        producesModule("org.jetbrains.research.ml.ast.transformations:ast-transformations")
    }
}
