package org.jetbrains.research.ml.coding.assistant.solutionSpace.heuristics

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdgeFactory
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.TreeContextCache
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.addVertices
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.takeClosest
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias SolutionSpaceSupportGraph = SimpleDirectedWeightedGraph<SolutionSpaceVertex, SolutionSpaceEdge>

private const val DEFAULT_MAX_NEIGHBORS_N = 10

fun createSolutionSpaceSupportGraph(
    heuristicsGraph: HeuristicsGraph,
    maxNeighborsCount: Int = DEFAULT_MAX_NEIGHBORS_N,
    cache: TreeContextCache = TreeContextCache()
): SolutionSpaceSupportGraph {
    val graph = SolutionSpaceSupportGraph(SolutionSpaceEdgeFactory(cache))
    graph.addVertices(heuristicsGraph.vertexSet())

    for (source in heuristicsGraph.vertexSet()) {
        if (source.isFinal)
            continue
        val neighbors = heuristicsGraph.takeClosest(maxNeighborsCount, source).drop(1)
        for (neighbor in neighbors) {
            val edge = graph.addEdge(source, neighbor)
            graph.setEdgeWeight(edge, edge.calculatedWeight)
        }
    }

    cache.clear()
    return graph
}
