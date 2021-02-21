package org.jetbrains.research.ml.coding.assistant.dataset.model

data class DynamicSolution(
    val records: List<DatasetRecord>
) {
    fun hasFinalSolution() = records.any { it.metaInfo.isFinalSolution }
}

data class TaskSolutions(
    val taskName: String,
    val dynamicSolutions: List<DynamicSolution>
)

data class Dataset(
    val tasks: List<TaskSolutions>
)
