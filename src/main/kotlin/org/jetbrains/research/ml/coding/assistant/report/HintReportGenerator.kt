package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import java.io.File

interface HintReportGenerator {
    fun generate(intoFile: File, report: HintReport)
}

// for report only. very bad complexity
internal fun SolutionSpaceGraphBuilder.getCode(id: String): String {
    return graph.vertexSet()
        .first { vertex -> vertex.partialSolutions.any { it.id == id } }
        .representativeSolution.psiFragment.text
}
