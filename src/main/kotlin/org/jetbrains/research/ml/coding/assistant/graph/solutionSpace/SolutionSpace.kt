package org.jetbrains.research.ml.coding.assistant.graph.solutionSpace

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.coding.assistant.graph.createHeuristicsSupportGraph
import org.jetbrains.research.ml.coding.assistant.graph.heuristics.HeuristicsVertex
import org.jetbrains.research.ml.coding.assistant.graph.heuristics.createSolutionSpaceSupportGraph
import org.jetbrains.research.ml.coding.assistant.graph.pathsTo
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias SolutionSpaceVertex = HeuristicsVertex

typealias TreeContextCache = MutableMap<SolutionSpaceVertex, TreeContext>

class SolutionSpace(
    intermediateSolutions: List<IntermediateSolution>,
) {
    private val cache: TreeContextCache = mutableMapOf()
    val graph = SimpleDirectedWeightedGraph(SolutionSpaceEdgeFactory(cache))

    init {
        val completeGraph = createHeuristicsSupportGraph(intermediateSolutions)
        val solutionSpaceSupportGraph = createSolutionSpaceSupportGraph(completeGraph)
        val finalSolutionVertices = solutionSpaceSupportGraph.vertexSet().filter { it.isFinal }

        val pathToFinalSolutions = solutionSpaceSupportGraph.pathsTo(finalSolutionVertices)
        solutionSpaceSupportGraph.vertexSet().forEach(graph::addVertex)

        for (vertexList in pathToFinalSolutions) {
            for ((source, target) in vertexList.windowed(2)) {
                val edge = solutionSpaceSupportGraph.getEdge(source, target)
                graph.addEdge(source, target, edge)
            }
        }
    }
}
