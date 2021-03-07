package org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jgrapht.Graph

typealias EdgeWeightCalculatorFactory<V, E> = (Graph<V, E>) -> EdgeWeightCalculator<V, E>

abstract class EdgeWeightCalculator<V, E>(val graph: Graph<V, E>) {
    abstract fun getWeight(edge: E): Double
}

class CustomEdgeWeightCalculator(
    graph: Graph<SolutionSpaceVertex, SolutionSpaceEdge>
) :
    EdgeWeightCalculator<SolutionSpaceVertex, SolutionSpaceEdge>(graph) {
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
        val editNodesSizeCoefficient: Double = edge.actions.map { it.node.size }.sum().toDouble()
        return editNodesSizeCoefficient / testScoreCoefficient
    }
}
