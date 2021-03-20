package org.jetbrains.research.ml.coding.assistant.system.matcher

import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

/**
 * Matches the student's partial solution to the solution space's vertex
 * Returns the difference score >= 0.0
 * 0.0 means the vertex and the partial solution is perfectly matched.
 */
interface PartialSolutionMatcher {
    fun differScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double
}

interface BooleanPartialSolutionMatcher : PartialSolutionMatcher {
    fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean

    override fun differScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double {
        return if (isMatched(vertex, partialSolution)) 0.0 else Double.POSITIVE_INFINITY
    }
}

class ExactPartialSolutionMatcher : BooleanPartialSolutionMatcher {
    override fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean {
        return vertex.fragment.root.isIsomorphicTo(partialSolution.context.root)
    }
}

class ThresholdSolutionMatcher(
    private val threshold: Double,
    private val matcher: PartialSolutionMatcher
) : BooleanPartialSolutionMatcher {
    override fun isMatched(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Boolean {
        return matcher.differScore(vertex, partialSolution) < threshold
    }
}

object EditPartialSolutionMatcher : PartialSolutionMatcher {
    override fun differScore(vertex: SolutionSpaceVertex, partialSolution: PartialSolution): Double {
        val matcher = Matcher(partialSolution.context, vertex.fragment)
        val actions = matcher.getEditActions()
        return actions.size.toDouble()
    }
}
