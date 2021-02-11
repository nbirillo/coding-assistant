package org.jetbrains.research.ml.coding.assistant.dataset.model

data class DynamicSolutionTaskDataset(
    val taskName: String,
    val solutions: List<TaskDynamicSolution>
)
