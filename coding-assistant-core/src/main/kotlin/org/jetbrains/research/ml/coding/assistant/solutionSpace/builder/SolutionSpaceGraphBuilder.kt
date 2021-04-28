package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.removeVertices
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.replaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.EdgeWeightCalculatorFactory
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicIntermediateSolution
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias DynamicIntermediateSolutionVertexChain = List<SolutionSpaceGraphVertex>
internal typealias SolutionSpaceGraph = SimpleDirectedWeightedGraph<SolutionSpaceGraphVertex, SolutionSpaceGraphEdge>

/**
 * Builder for solution space.
 * Able to add sequence of student's solutions (dynamic solution)
 */
class SolutionSpaceGraphBuilder {
    internal val graph = SolutionSpaceGraph(SolutionSpaceGraphEdge::class.java)

    fun addDynamicSolution(dynamic: DynamicIntermediateSolution) {
        val chain = dynamic.map { SolutionSpaceGraphVertex(it) }
        val dynamicVertices = graph.addDynamicChainOrMerge(chain)
        for ((source, target) in dynamicVertices.windowed(2)) {
            val sourceVertex = graph.fetchVertex(source)
            val targetVertex = graph.fetchVertex(target)
            if (sourceVertex == targetVertex) {
                continue
            }
            if (!graph.containsEdge(sourceVertex, targetVertex)) {
                graph.addEdge(sourceVertex, targetVertex)
            }
        }
    }

    private fun removeSimpleCycles() {
        val simpleCyclesDetector = SzwarcfiterLauerSimpleCycles(graph)
        for (simpleCycle in simpleCyclesDetector.findSimpleCycles()) {
            graph.removeVertices(simpleCycle)
        }
    }

    private fun removeSingletonVertices() {
        // remove non-final singleton vertices
        graph.vertexSet()
            .filter {
                graph.edgesOf(it).isEmpty() && !it.representativeSolution.metaInfo.isFinalSolution
            }
            .forEach(graph::removeVertex)
    }

    private fun removeNonFinalLeaves() {
        fun SolutionSpaceGraphVertex.isLeaf(): Boolean {
            return graph.outDegreeOf(this) == 0
        }
        fun collectVertices(vertex: SolutionSpaceGraphVertex): List<SolutionSpaceGraphVertex> {
            val prevSingleNonFinalVertices = graph
                .incomingEdgesOf(vertex)
                .map { graph.getEdgeSource(it) }
                .filter { it.isFinal && graph.outDegreeOf(it) == 1 }
            return prevSingleNonFinalVertices + prevSingleNonFinalVertices.flatMap { collectVertices(it) }
        }

        val toRemoveVertices = mutableSetOf<SolutionSpaceGraphVertex>()
        for (vertex in graph.vertexSet()) {
            if (!vertex.isFinal && vertex.isLeaf()) {
                toRemoveVertices.add(vertex)
                toRemoveVertices.addAll(collectVertices(vertex))
            }
        }

        graph.vertexSet()
            .filter { toRemoveVertices.contains(it) }
            .forEach(graph::removeVertex)

    }

    private fun deletePsiFiles() {
        val psiFiles = graph.vertexSet()
            .flatMap { vertex -> vertex.partialSolutions.map { it.psiFragment } }
        psiFiles
            .forEach { it.deleteFile() }
        psiFiles.takeWhile { !it.forceDeleteTmpData() }
    }

    fun build(
        isFileDeletionNeeded: Boolean = true,
        weightFactory: EdgeWeightCalculatorFactory<SolutionSpaceVertex, SolutionSpaceEdge>
    ): SolutionSpace {
        removeSimpleCycles()
        removeSingletonVertices()
        removeNonFinalLeaves()
        val solutionSpace = SolutionSpace(weightFactory, this)
        if (isFileDeletionNeeded) {
            deletePsiFiles()
        }
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
