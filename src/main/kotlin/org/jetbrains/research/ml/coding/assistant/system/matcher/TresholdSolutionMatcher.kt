package org.jetbrains.research.ml.coding.assistant.system.matcher

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

class TresholdSolutionMatcher(
    private val treshold: Double,
    private val matcher: PartialSolutionMatcher
) : BooleanPartialSolutionMatcher {
    override fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean {
        return matcher.differScore(vertex, partialSolution) < treshold
    }
}
