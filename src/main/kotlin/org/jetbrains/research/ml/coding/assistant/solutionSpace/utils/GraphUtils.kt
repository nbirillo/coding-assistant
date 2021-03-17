package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils

import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.EdgeReversedGraph


/**
 * Adds list of vertices to the graph.
 */
fun <V, E> Graph<V, E>.addVertices(vertices: Iterable<V>) {
    vertices.forEach(this::addVertex)
}

/**
 * Replaces vertex with the new one with reconnecting the edges
 */
fun <V, E> Graph<V, E>.replaceVertex(vertex: V, newVertex: V) {
    addVertex(newVertex)
    val outgoingEdges = outgoingEdgesOf(vertex).toSet()
    removeAllEdges(outgoingEdges)
    for (edge in outgoingEdges) {
        addEdge(newVertex, getEdgeTarget(edge), edge)
    }
    val incomingEdges = incomingEdgesOf(vertex).toSet()
    removeAllEdges(incomingEdges)
    for (edge in incomingEdges) {
        addEdge(getEdgeSource(edge), newVertex, edge)
    }
    removeVertex(vertex)
}


/**
 * Removes all vertices from the graph
 */
fun <V, E> Graph<V, E>.removeVertices(vertices: Collection<V>) {
    if (vertices.isEmpty()) {
        return
    }
    val cycledVertices = vertices.plus(vertices.first())
    for ((sourceVertex, targetVertex) in cycledVertices.windowed(2)) {
        removeEdge(sourceVertex, targetVertex)
    }
}
