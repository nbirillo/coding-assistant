package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.TreeContextCache
import org.jgrapht.DirectedGraph
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.traverse.ClosestFirstIterator


// First element of the list is `startVertex`
fun <V, E> Graph<V, E>.takeClosest(
    maxN: Int,
    startVertex: V,
    radius: Double = Double.POSITIVE_INFINITY
): List<V> {
    val iterator = ClosestFirstIterator(this, startVertex, radius)
    val maxPathSize = maxN  + 1
    val closest = arrayListOf<V>().apply { ensureCapacity(maxPathSize) }
    while (iterator.hasNext() && closest.size < maxPathSize) {
        val element = iterator.next()
        closest.add(element)
    }
    return closest
}

fun <V, E> Graph<V, E>.addVertices(
    vertices: Collection<V>
) {
    for (vertex in vertices) {
        addVertex(vertex)
    }
}

fun TreeContextCache.getOrCreate(
    key: SolutionSpaceVertex,
    supplier: () -> TreeContext
): TreeContext {
    var value = this[key]
    if (value != null)
        return value
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
