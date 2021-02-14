package org.jetbrains.research.ml.coding.assistant.dataset.model

data class DynamicSolution(
    val records: List<DatasetRecord>
)

data class TaskSolutions(
    val taskName: String,
    val dynamicSolutions: List<DynamicSolution>
)

data class Dataset(
    val tasks: List<TaskSolutions>
)
