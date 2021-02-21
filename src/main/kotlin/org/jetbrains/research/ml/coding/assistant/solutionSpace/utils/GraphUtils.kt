package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils

import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.EdgeReversedGraph

fun <V, E> Graph<V, E>.addVertices(vertices: Iterable<V>) {
    vertices.forEach(this::addVertex)
}

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

fun <V, E> Graph<V, E>.pathsTo(vertices: Collection<V>): List<List<V>> {
    val inverted = EdgeReversedGraph(this)
    val dijkstraShortestPath = DijkstraShortestPath(inverted)
    val otherGraphVertices = vertexSet().minus(vertices)
    return vertices
        .map { dijkstraShortestPath.getPaths(it) }
        .flatMap { singleSourcePaths ->
            otherGraphVertices.map { singleSourcePaths.getPath(it).vertexList.reversed() }
        }
}

fun <V, E> Graph<V, E>.removeVertexList(vertices: Collection<V>) {
    if (vertices.isEmpty()) {
        return
    }
    val cycledVertices = vertices.plus(vertices.first())
    for ((sourceVertex, targetVertex) in cycledVertices.windowed(2)) {
        removeEdge(sourceVertex, targetVertex)
    }
}
