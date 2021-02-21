package org.jetbrains.research.ml.coding.assistant.system.matcher

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

interface PartialSolutionMatcher {
    fun matchScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double
}

interface BooleanPartialSolutionMatcher : PartialSolutionMatcher {
    fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean

    override fun matchScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double {
        return if (isMatched(vertex, partialSolution)) 1.0 else 0.0
    }
}
