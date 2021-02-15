package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.coding.assistant.solutionSpace.heuristics.createHeuristicsSupportGraph
import org.jetbrains.research.ml.coding.assistant.solutionSpace.heuristics.createSolutionSpaceSupportGraph
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.addVertices
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.pathsTo
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias TreeContextCache = HashMap<SolutionSpaceVertex, TreeContext>

class SolutionSpace(intermediateSolutions: List<IntermediateSolution>) {
    private val cache = TreeContextCache()
    val graph = SimpleDirectedWeightedGraph(SolutionSpaceEdgeFactory(cache))

    init {
        val completeGraph = createHeuristicsSupportGraph(intermediateSolutions.map { SolutionSpaceVertex(it) })
        val solutionSpaceSupportGraph = createSolutionSpaceSupportGraph(completeGraph, cache = cache)
        val finalSolutionVertices = solutionSpaceSupportGraph.vertexSet().filter { it.isFinal }

        val pathToFinalSolutions = solutionSpaceSupportGraph.pathsTo(finalSolutionVertices)
        graph.addVertices(solutionSpaceSupportGraph.vertexSet())

        for (vertexList in pathToFinalSolutions) {
            for ((source, target) in vertexList.windowed(2)) {
                val edge = solutionSpaceSupportGraph.getEdge(source, target)
                graph.addEdge(source, target, edge)
            }
        }
        cache.clear()
    }
}
