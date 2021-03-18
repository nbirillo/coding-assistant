import java.net.URI

sourceControl {
    gitRepository(URI.create("https://github.com/JetBrains-Research/ast-transformations.git")) {
        producesModule("org.jetbrains.research.ml.ast.transformations:ast-transformations")
    }
}
