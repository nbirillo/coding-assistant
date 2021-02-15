package org.jetbrains.research.ml.coding.assistant.graph.solutionSpace

import com.github.gumtreediff.actions.model.Action
import org.jetbrains.research.ml.coding.assistant.graph.heuristics.WeightedEdge

class SolutionSpaceEdge(private val actions: List<Action>) : WeightedEdge() {
    val calculatedWeight: Double get() = getWeight(this)

    companion object {
        private fun getWeight(edge: SolutionSpaceEdge): Double {
            val targetVertex = edge.target as SolutionSpaceVertex
            val sourceVertex = edge.source as SolutionSpaceVertex
            val sourceTestScore = sourceVertex.representativeSolution.metaInfo.testsResults
            val targetTestScore = targetVertex.representativeSolution.metaInfo.testsResults
            // TODO: wip
            if (targetTestScore < sourceTestScore)
                return Double.POSITIVE_INFINITY

            val coefficient: Double = (targetTestScore - sourceTestScore + 1.0) * 10.0

            return edge.actions.map { it.node.size }.sum().toDouble() * coefficient
        }
    }
}
