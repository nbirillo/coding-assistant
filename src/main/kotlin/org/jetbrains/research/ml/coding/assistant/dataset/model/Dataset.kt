package org.jetbrains.research.ml.coding.assistant.dataset.model

/**
 *  Model that accumulates all records for single person
 *  Model for one .csv file
 */
data class DynamicSolution(
    val records: List<DatasetRecord>
) {
    fun hasFinalSolution() = records.any { it.metaInfo.isFinalSolution }
}

/**
 * Model for all solutions in the dataset for one task
 */
data class TaskSolutions(
    val datasetTask: DatasetTask,
    val dynamicSolutions: List<DynamicSolution>
)

/**
 * Model for all solutions in the dataset for several tasks
 */
data class Dataset(
    val tasks: List<TaskSolutions>
)

enum class DatasetTask {
    MAX_DIGIT, BRACKETS, MAX_3, PIES, VOTING, ZERO;

    val taskName: String get() = name.toLowerCase()

    companion object {
        fun createFromString(string: String): DatasetTask {
            return when (val upperString = string.toUpperCase()) {
                "ZERO", "IS_ZERO" -> ZERO
                else -> valueOf(upperString)
            }
        }
    }
}
