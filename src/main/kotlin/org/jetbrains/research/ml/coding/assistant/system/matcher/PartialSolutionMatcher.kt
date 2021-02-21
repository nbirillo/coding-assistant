package org.jetbrains.research.ml.coding.assistant.system.matcher

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

interface PartialSolutionMatcher {
    fun differScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double
}

interface BooleanPartialSolutionMatcher : PartialSolutionMatcher {
    fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean

    override fun differScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double {
        return if (isMatched(vertex, partialSolution)) 0.0 else Double.POSITIVE_INFINITY
    }
}
