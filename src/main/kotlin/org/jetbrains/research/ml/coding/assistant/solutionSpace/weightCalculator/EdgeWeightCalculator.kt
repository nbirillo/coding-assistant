package org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jgrapht.Graph

typealias EdgeWeightCalculatorFactory<V, E> = (Graph<V, E>) -> EdgeWeightCalculator<V, E>

/**
 * Calculates the edge weight based on the edge for the given graph
 */
abstract class EdgeWeightCalculator<V, E>(val graph: Graph<V, E>) {
    /**
        Calculated the weight of the edge.
        Bigger score means more difference(priority) between source and target node.
    */
    abstract fun getWeight(edge: E): Double
}

/**
 * Calculates the edge weight based on the edge for the given graph.
 * Features:
 *  - prioritize the edges where the target vertex has the bigger test score
 *  - the final score depends on the sum of number of nodes in every calculated edit(`Action`)
 *  - the bigger test score difference the less edge weight is returned
 */
class CustomEdgeWeightCalculator(
    graph: Graph<SolutionSpaceVertex, SolutionSpaceEdge>
) : EdgeWeightCalculator<SolutionSpaceVertex, SolutionSpaceEdge>(graph) {
    private val cache = EdgeActionsCache(graph)
    override fun getWeight(edge: SolutionSpaceEdge): Double {
        // TODO: think about more features.
        val targetVertex = graph.getEdgeTarget(edge)
        val sourceVertex = graph.getEdgeSource(edge)
        val sourceTestScore = sourceVertex.studentInfo.metaInfo.testsResults
        val targetTestScore = targetVertex.studentInfo.metaInfo.testsResults
        if (targetTestScore < sourceTestScore) {
            return Double.POSITIVE_INFINITY
        }

        val testScoreCoefficient: Double = (targetTestScore - sourceTestScore + 1.0) * 10.0
        val actions = cache[edge]
        val editNodesSizeCoefficient: Double = actions.map { it.node.size }.sum().toDouble()
        return editNodesSizeCoefficient / testScoreCoefficient
    }
}
