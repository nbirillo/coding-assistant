import java.net.URI

sourceControl {
    gitRepository(URI.create("https://github.com/JetBrains-Research/ast-transformations.git")) {
        producesModule("io.github.nbirillo.ast.transformations:ast-transformations")
    }
}
