package org.jetbrains.research.ml.coding.assistant.system.finder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.BooleanPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.system.matcher.PartialSolutionMatcher

/**
 * Finds the closest vertex to a given student's partial solution
 */
abstract class VertexFinder {
    abstract val matcher: PartialSolutionMatcher

    abstract fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex?

    override fun toString(): String {
        return "${this::class.simpleName}(matcher=$matcher)"
    }
}

/**
 * Naive vertex finder returns the first vertex that matcher predicate `BooleanPartialSolutionMatcher.isMatched`
 */
class NaiveVertexFinder(override val matcher: BooleanPartialSolutionMatcher) : VertexFinder() {
    override fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        return solutionSpace.graph.vertexSet().firstOrNull { matcher.isMatched(it, partialSolution) }
    }
}

/**
 * Parallel vertex finder returns the vertex which differ score is minimal using parallel stream.
 */
class ParallelVertexFinder(override val matcher: PartialSolutionMatcher) : VertexFinder() {
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
