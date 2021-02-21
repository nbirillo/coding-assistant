package org.jetbrains.research.ml.coding.assistant.system.finder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.BooleanPartialSolutionMatcher


class NaiveVertexFinder(override val matcher: BooleanPartialSolutionMatcher) : VertexFinder {
    override fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        return solutionSpace.graph.vertexSet().firstOrNull { matcher.isMatched(it, partialSolution) }
    }
}
