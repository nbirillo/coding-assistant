package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.removeVertexList
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.replaceVertex
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicIntermediateSolution
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias DynamicIntermediateSolutionVertexChain = List<SolutionSpaceGraphVertex>
internal typealias SolutionSpaceGraph = SimpleDirectedWeightedGraph<SolutionSpaceGraphVertex, SolutionSpaceGraphEdge>

class SolutionSpaceGraphBuilder {
    internal val cache = TreeContextCache()
    internal val graph = SolutionSpaceGraph(SolutionSpaceGraphEdgeFactory(cache))

    fun addDynamicSolution(dynamic: DynamicIntermediateSolution) {
        val chain = dynamic.map { SolutionSpaceGraphVertex(it) }
        val dynamicVertices = graph.addDynamicChainOrMerge(chain)
        for ((source, target) in dynamicVertices.windowed(2)) {
            val sourceVertex = graph.fetchVertex(source)
            val targetVertex = graph.fetchVertex(target)
            if (sourceVertex == targetVertex) {
                continue
            }
            val edge = if (graph.containsEdge(sourceVertex, targetVertex)) {
                graph.getEdge(sourceVertex, targetVertex)
            } else {
                graph.addEdge(sourceVertex, targetVertex)
            }
            graph.setEdgeWeight(edge, edge.calculatedWeight)
        }
    }

    private fun clearCycles() {
        val simpleCyclesDetector = SzwarcfiterLauerSimpleCycles(graph)
        for (simpleCycle in simpleCyclesDetector.findSimpleCycles()) {
            graph.removeVertexList(simpleCycle)
        }
    }

    private fun clearPsiFiles() {
        val psiFiles = graph.vertexSet()
            .flatMap { vertex -> vertex.partialSolutions.map { it.psiFragment } }
        psiFiles
            .forEach { it.deleteFile() }
        psiFiles.takeWhile { !it.forceDeleteTmpData() }
    }

    fun build(): SolutionSpace {
        clearCycles()
        val solutionSpace = SolutionSpace(this)
        clearPsiFiles()
        return solutionSpace
    }
}

private fun SolutionSpaceGraph.fetchVertex(vertex: SolutionSpaceGraphVertex): SolutionSpaceGraphVertex {
    if (containsVertex(vertex)) {
        return vertex
    }
    return vertexSet().first { it.partialSolutions.containsAll(vertex.partialSolutions) }
}

private fun SolutionSpaceGraph.addDynamicChainOrMerge(
    vertices: DynamicIntermediateSolutionVertexChain
): DynamicIntermediateSolutionVertexChain {
    return vertices.map { vertex ->
        addVertexOrMerge(vertex)
    }
}

private fun SolutionSpaceGraph.addVertexOrMerge(vertex: SolutionSpaceGraphVertex): SolutionSpaceGraphVertex {
    val graphVertex = vertexSet().firstOrNull {
        it.containsAll(vertex.partialSolutions)
    }
    return if (graphVertex != null) {
        val newVertex = graphVertex.merged(vertex)
        replaceVertex(graphVertex, newVertex)
        newVertex
    } else {
        addVertex(vertex)
        vertex
    }
}
