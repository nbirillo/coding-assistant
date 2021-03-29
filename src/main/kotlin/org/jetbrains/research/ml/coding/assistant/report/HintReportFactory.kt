package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.finder.VertexFinder

class HintReportFactory(
    private val datasetTask: DatasetTask,
    private val finders: List<VertexFinder>
) {
    fun createHintReports(solutionSpace: SolutionSpace, partialSolution: PartialSolution): List<HintReport> {
        return finders.map { finder ->
            val closestVertex = finder.findCorrespondingVertex(solutionSpace, partialSolution)
            HintReport(
                datasetTask,
                finder.toString(),
                partialSolution,
                solutionSpace,
                closestVertex,
                null,
            )
        }
    }
}
