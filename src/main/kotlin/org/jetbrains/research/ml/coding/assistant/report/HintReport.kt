package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex

data class HintReport(
    val taskName: DatasetTask,
    val algorithmName: String,
    val studentCode: String,
    val space: SolutionSpace,
    val closestVertex: SolutionSpaceVertex,
    val nextNode: SolutionSpaceVertex?
)
