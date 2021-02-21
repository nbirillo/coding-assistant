package org.jetbrains.research.ml.coding.assistant.solutionSpace


import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.addVertices
import org.jgrapht.Graph
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.SimpleDirectedWeightedGraph

class SolutionSpace internal constructor(graphBuilder: SolutionSpaceGraphBuilder) {
    val graph = transferGraph(graphBuilder)
}

private fun transferGraph(builder: SolutionSpaceGraphBuilder): Graph<SolutionSpaceVertex, SolutionSpaceEdge> {
    val graph = SimpleDirectedWeightedGraph(SolutionSpaceEdgeFactory)
    val oldVertices = builder.graph.vertexSet().toList()
    val newVertices = oldVertices.map { it.toSolutionSpaceVertex(builder) }
    graph.addVertices(newVertices)

    val mapping = (oldVertices zip newVertices).toMap()

    for ((oldVertex, newVertex) in oldVertices zip newVertices) {
        fun transferEdges(edges: Iterable<SolutionSpaceGraphEdge>, isOutgoing: Boolean) {
            for (outgoingEdge in edges) {
                val neighbour = if (isOutgoing)
                    builder.graph.getEdgeTarget(outgoingEdge)
                else
                    builder.graph.getEdgeSource(outgoingEdge)
                val newTarget = mapping[neighbour]!!
                val newEdge = SolutionSpaceEdge(outgoingEdge.calculatedWeight, outgoingEdge.actions)
                if (isOutgoing)
                    graph.addEdge(newVertex, newTarget, newEdge)
                else
                    graph.addEdge(newTarget, newVertex, newEdge)
                graph.setEdgeWeight(newEdge, newEdge.calculatedWeight)
            }
        }

        val outgoingEdges = builder.graph.outgoingEdgesOf(oldVertex).toSet()
        transferEdges(outgoingEdges, isOutgoing = true)

        val incomingEdges = builder.graph.incomingEdgesOf(oldVertex).toSet()
        transferEdges(incomingEdges, isOutgoing = false)
    }

    return AsUnmodifiableGraph(graph)
}

private fun SolutionSpaceGraphVertex.toSolutionSpaceVertex(builder: SolutionSpaceGraphBuilder): SolutionSpaceVertex {
    return SolutionSpaceVertex(
        builder.cache[this],
        partialSolutions.map { StudentInfo(it.id, it.metaInfo) }
    )
}
