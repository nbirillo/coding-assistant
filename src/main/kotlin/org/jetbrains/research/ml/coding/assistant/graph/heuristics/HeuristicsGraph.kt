package org.jetbrains.research.ml.coding.assistant.graph

import org.jetbrains.research.ml.coding.assistant.graph.heuristics.HeuristicsVertex
import org.jetbrains.research.ml.coding.assistant.graph.heuristics.WeightedEdge
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jgrapht.graph.DirectedWeightedMultigraph
import kotlin.math.abs

typealias HeuristicsEdge = WeightedEdge
typealias HeuristicsGraph = DirectedWeightedMultigraph<HeuristicsVertex, HeuristicsEdge>

fun createHeuristicsSupportGraph(
    intermediateSolutions: List<IntermediateSolution>
): HeuristicsGraph {
    val graph = HeuristicsGraph(HeuristicsEdge::class.java)
    val vertices = intermediateSolutions.map { HeuristicsVertex(it) }
    graph.addVertices(vertices)
    graph.completeGraph()
    return graph
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

private fun HeuristicsGraph.completeGraph() {
    for (source in vertexSet()) {
        for (target in vertexSet()) {
            if (System.identityHashCode(source) == System.identityHashCode(target)) {
                continue
            }
            val weight = abs(source.psiNodesCount - target.psiNodesCount).toDouble()
            val edge = addEdge(source, target)

            setEdgeWeight(edge, weight)
        }
    }
}
