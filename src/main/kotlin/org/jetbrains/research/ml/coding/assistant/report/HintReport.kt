package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

/**
 * Model that represents algorithm's result
 */
data class HintReport(
    val taskName: DatasetTask,
    val algorithmName: String,
    val partialSolution: PartialSolution,
    val space: SolutionSpace,
    val closestVertex: SolutionSpaceVertex?,
    val nextNode: SolutionSpaceVertex?
)
