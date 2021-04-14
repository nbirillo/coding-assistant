package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceRepository
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.finder.VertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.HintVertexCalculator

class HintReportFactory(
    private val repository: SolutionSpaceRepository,
    finders: List<VertexFinder>,
    hintCalculators: List<HintVertexCalculator>
) {
    private val hintFactories = finders
        .flatMap { finder -> hintCalculators.map { finder to it } }

    fun createHintReports(partialSolution: PartialSolution): List<HintReport> {
        val solutionSpace = repository.fetchSolutionSpace(partialSolution.datasetTask)
        return hintFactories.map { (finder, calculator) ->
            val closestVertex = finder.findCorrespondingVertex(solutionSpace, partialSolution)
            val hintVertex = closestVertex?.let {
                calculator.calculateHintVertex(solutionSpace, it, partialSolution)
            }
            HintReport(
                partialSolution.datasetTask,
                finder.toString(),
                partialSolution,
                solutionSpace,
                closestVertex,
                hintVertex,
            )
        }
    }
}
