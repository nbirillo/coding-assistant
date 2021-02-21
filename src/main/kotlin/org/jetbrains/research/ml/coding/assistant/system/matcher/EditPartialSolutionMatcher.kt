package org.jetbrains.research.ml.coding.assistant.system.matcher

import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

object EditPartialSolutionMatcher : PartialSolutionMatcher {
    override fun differScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double {
        val matcher = Matcher(partialSolution.context, vertex.fragment)
        val actions = matcher.getEditActions()
        return actions.size.toDouble()
    }
}
