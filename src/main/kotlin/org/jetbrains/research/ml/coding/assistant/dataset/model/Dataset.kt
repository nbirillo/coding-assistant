package org.jetbrains.research.ml.coding.assistant.dataset.model

/**
 *  Model that accumulates all record for single person
 *  Model for one .csv file
 */
data class DynamicSolution(
    val records: List<DatasetRecord>
) {
    fun hasFinalSolution() = records.any { it.metaInfo.isFinalSolution }
}

/**
 * Model for all solution in dataset
 */
data class TaskSolutions(
    val datasetTask: DatasetTask,
    val dynamicSolutions: List<DynamicSolution>
)

/**
 * Model for the whole dataset
 */
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
