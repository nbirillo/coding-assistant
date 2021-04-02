package org.jetbrains.research.ml.coding.assistant.system.hint

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jgrapht.Graphs

interface HintVertexCalculator {
    fun calculateHintVertex(
        solutionSpace: SolutionSpace,
        closestVertex: SolutionSpaceVertex,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex?
}

object NaiveHintVertexCalculator : HintVertexCalculator {
    override fun calculateHintVertex(
        solutionSpace: SolutionSpace,
        closestVertex: SolutionSpaceVertex,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        val successors = Graphs.successorListOf(solutionSpace.graph, closestVertex)
        println(successors.map { it.toString() })
        return successors.firstOrNull()
    }
}
