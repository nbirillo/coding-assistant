package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import com.github.gumtreediff.actions.model.Action

class SolutionSpaceGraphEdge(internal val actions: List<Action>) : WeightedEdge() {
    val calculatedWeight: Double get() = getWeight(this)

    companion object {
        private fun getWeight(edge: SolutionSpaceGraphEdge): Double {
            val targetVertex = edge.target as SolutionSpaceGraphVertex
            val sourceVertex = edge.source as SolutionSpaceGraphVertex
            val sourceTestScore = sourceVertex.representativeSolution.metaInfo.testsResults
            val targetTestScore = targetVertex.representativeSolution.metaInfo.testsResults
            // TODO: wip
            if (targetTestScore < sourceTestScore) {
                return Double.POSITIVE_INFINITY
            }

            val testScoreCoefficient: Double = (targetTestScore - sourceTestScore + 1.0) * 10.0
            val editNodesSizeCoefficient: Double = edge.actions.map { it.node.size }.sum().toDouble()
            return editNodesSizeCoefficient / testScoreCoefficient
        }
    }
}
