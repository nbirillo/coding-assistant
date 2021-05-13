package org.jetbrains.research.ml.coding.assistant.system.finder

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.PartialSolutionMatcher
import org.jgrapht.traverse.BreadthFirstIterator
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
class NaiveVertexFinder(override val matcher: PartialSolutionMatcher) : VertexFinder() {
    override fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        return solutionSpace.graph.vertexSet().minByOrNull { vertex ->
            matcher.differScore(vertex, partialSolution)
//                .also { score ->
//                    println(
//                        """
// Score = $score
// Current vertex(${vertex.id}) code:
// ${vertex.code}
//
// """.trimIndent()
//                    )
//                }
        }
    }
}

/**
 * Parallel vertex finder returns the vertex which differ score is minimal using parallel stream.
 */
class ParallelVertexFinder(override val matcher: PartialSolutionMatcher) : VertexFinder() {
    private val lock = ReentrantLock()
    override fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        return solutionSpace.graph.vertexSet()
            .parallelStream()
            .min(
                compareBy { vertex ->
                    matcher.differScore(vertex, partialSolution)
                        .also { score ->
                            lock.withLock {
                                println(
                                    """    
Score = $score
Current vertex(${vertex.id}) code:
${vertex.code}

""".trimIndent()
                                )
                            }
                        }
                }
            )
            .orElse(null)
    }
}

class BFSParallelVertexFinder(override val matcher: PartialSolutionMatcher) : VertexFinder() {
    override fun findCorrespondingVertex(
        solutionSpace: SolutionSpace,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        return solutionSpace.startSolutions
            .parallelStream()
            .map { vertex ->
                BreadthFirstIterator(solutionSpace.graph, vertex)
                    .asSequence().toList()
                    .parallelStream()
                    .map<Pair<SolutionSpaceVertex, Double>?> { it to matcher.differScore(it, partialSolution) }
                    .min(compareBy { it.second })
                    .orElse(null)
            }
            .min(compareBy { it.second })
            .orElse(null)?.first
    }
}
