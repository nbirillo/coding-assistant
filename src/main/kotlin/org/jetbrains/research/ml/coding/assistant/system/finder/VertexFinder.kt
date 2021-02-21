package org.jetbrains.research.ml.coding.assistant.system.finder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.PartialSolutionMatcher

interface VertexFinder {
    val matcher: PartialSolutionMatcher

    fun findCorrespondingVertex(solutionSpace: SolutionSpace, partialSolution: PartialSolution): SolutionSpaceVertex?
}
