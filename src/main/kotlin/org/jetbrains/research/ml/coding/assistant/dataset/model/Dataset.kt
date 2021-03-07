package org.jetbrains.research.ml.coding.assistant.dataset.model

data class DynamicSolution(
    val records: List<DatasetRecord>
) {
    fun hasFinalSolution() = records.any { it.metaInfo.isFinalSolution }
}

data class TaskSolutions(
    val datasetTask: DatasetTask,
    val dynamicSolutions: List<DynamicSolution>
)

data class Dataset(
    val tasks: List<TaskSolutions>
)

enum class DatasetTask {
    MAX_DIGIT, BRACKETS, MAX_3, PIES, VOTING, ZERO;

    val taskName: String get() = name.toLowerCase()

    companion object {
        fun createFromString(string: String): DatasetTask {
            return valueOf(string.toUpperCase())
        }
    }
}
