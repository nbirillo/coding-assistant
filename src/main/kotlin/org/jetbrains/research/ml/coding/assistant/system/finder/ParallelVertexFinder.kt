package org.jetbrains.research.ml.coding.assistant.system.finder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.PartialSolutionMatcher

class ParallelVertexFinder(override val matcher: PartialSolutionMatcher) : VertexFinder {
    override fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        return solutionSpace.graph.vertexSet()
            .parallelStream()
            .min(compareBy { matcher.differScore(it, partialSolution) })
            .orElse(null)
    }
}
