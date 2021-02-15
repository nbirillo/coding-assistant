package org.jetbrains.research.ml.coding.assistant.solutionSpace.heuristics

import org.jetbrains.research.ml.coding.assistant.solutionSpace.WeightedEdge
import org.jgrapht.graph.DirectedWeightedMultigraph
import kotlin.math.abs

typealias HeuristicsVertex = org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
typealias HeuristicsEdge = WeightedEdge
typealias HeuristicsGraph = DirectedWeightedMultigraph<HeuristicsVertex, HeuristicsEdge>


fun createHeuristicsSupportGraph(
    vertices: List<HeuristicsVertex>
): HeuristicsGraph {
    val graph = HeuristicsGraph(HeuristicsEdge::class.java)
    graph.addVertices(vertices)
    graph.completeGraph { source, target -> calculateWeight(source, target) }
    return graph
}


private fun calculateWeight(source: HeuristicsVertex, target: HeuristicsVertex): Double {
    return abs(source.psiNodesCount - target.psiNodesCount).toDouble()
}

private fun HeuristicsGraph.addVertices(vertices: List<HeuristicsVertex>) {
    for (vertex in vertices) {
        val graphVertex = vertexSet().firstOrNull {
            it.containsAll(vertex.intermediateSolutions)
        }
        if (graphVertex != null) {
            val newIntermediateSolutions = graphVertex.intermediateSolutions.plus(vertex.intermediateSolutions)
            val newVertex = graphVertex.copy(intermediateSolutions = newIntermediateSolutions)
            removeVertex(graphVertex)
            addVertex(newVertex)
        } else {
            addVertex(vertex)
        }
    }
}

private fun HeuristicsGraph.completeGraph(metric: (HeuristicsVertex, HeuristicsVertex) -> Double) {
    val vertices = vertexSet()
    for (source in vertices) {
        for (target in vertices) {
            if (System.identityHashCode(source) == System.identityHashCode(target))
                continue
            val weight = metric(source, target)
            val edge = addEdge(source, target)

            setEdgeWeight(edge, weight)
        }
    }
}
