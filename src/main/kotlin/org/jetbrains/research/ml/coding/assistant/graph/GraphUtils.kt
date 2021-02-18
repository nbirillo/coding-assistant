package org.jetbrains.research.ml.coding.assistant.graph

import org.jgrapht.DirectedGraph
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.traverse.ClosestFirstIterator

fun <V, E> Graph<V, E>.takeClosest(
    maxN: Int,
    startVertex: V? = null,
    radius: Double = Double.POSITIVE_INFINITY
): List<V> {
    val iterator = ClosestFirstIterator(this, startVertex, radius)
    val closest = arrayListOf<V>().apply { ensureCapacity(maxN) }
    while (iterator.hasNext() && closest.size < maxN) {
        val element = iterator.next()
        closest.add(element)
    }
    return closest
}

fun <K, V> MutableMap<K, V>.getOrCreate(key: K, supplier: () -> V): V {
    var value = this[key]
    if (value != null) {
        return value
    }
    value = supplier()
    this[key] = value
    return value
}

fun <V, E> DirectedGraph<V, E>.pathsTo(vertices: Collection<V>): List<List<V>> {
    val inverted = EdgeReversedGraph(this)
    val dijkstraShortestPath = DijkstraShortestPath(inverted)
    val otherGraphVertices = vertexSet().minus(vertices)
    return vertices
        .map { dijkstraShortestPath.getPaths(it) }
        .flatMap { singleSourcePaths ->
            otherGraphVertices.map { singleSourcePaths.getPath(it).vertexList.reversed() }
        }
}
