package org.jetbrains.research.ml.coding.assistant.system.matcher

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

class ExactPartialSolutionMatcher : BooleanPartialSolutionMatcher {
    override fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean {
        return vertex.fragment.root.isIsomorphicTo(partialSolution.context.root)
    }
}
