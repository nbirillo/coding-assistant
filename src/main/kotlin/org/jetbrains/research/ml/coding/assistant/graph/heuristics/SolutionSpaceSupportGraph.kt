package org.jetbrains.research.ml.coding.assistant.graph.heuristics

import org.jetbrains.research.ml.coding.assistant.graph.HeuristicsGraph
import org.jetbrains.research.ml.coding.assistant.graph.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.graph.solutionSpace.SolutionSpaceEdgeFactory
import org.jetbrains.research.ml.coding.assistant.graph.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.graph.solutionSpace.TreeContextCache
import org.jetbrains.research.ml.coding.assistant.graph.takeClosest
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias SolutionSpaceSupportGraph = SimpleDirectedWeightedGraph<SolutionSpaceVertex, SolutionSpaceEdge>
private const val MAX_NEIGHBORS_N = 10

fun createSolutionSpaceSupportGraph(heuristicsGraph: HeuristicsGraph): SolutionSpaceSupportGraph {
    val cache: TreeContextCache = mutableMapOf()
    val graph = SolutionSpaceSupportGraph(SolutionSpaceEdgeFactory(cache))
    heuristicsGraph.vertexSet().forEach(graph::addVertex)

    for (target in heuristicsGraph.vertexSet()) {
        val neighbors = heuristicsGraph.takeClosest(MAX_NEIGHBORS_N, target).drop(1)
        for (neighbor in neighbors) {
            val edge = graph.addEdge(target, neighbor)
            graph.setEdgeWeight(edge, edge.calculatedWeight)
        }
    }

    cache.clear()
    return graph
}
