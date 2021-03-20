package org.jetbrains.research.ml.coding.assistant.dataset.model

import kotlinx.serialization.Serializable

// Represents one record in .csv file.
// Keys = columns. Map values = record values.
typealias CSVRecord = Map<String, String>

/**
 *  Model represents a single record entry in dataset
 *  Model for a row in .csv file
 */
data class DatasetRecord(
    val id: String,
    val fragment: String,
    val metaInfo: MetaInfo
) {
    internal constructor(record: CSVRecord) : this(
        id = record[Column.ID] ?: throwNullFieldError(Column.ID),
        fragment = record[Column.FRAGMENT] ?: "",
        metaInfo = MetaInfo(record)
    )
}

/**
 *  Model represents meta information attached to the dataset record
 */
@Serializable
data class MetaInfo(
    val age: Float?,
    val programExperience: ProgramExperience?,
    val testsResults: Double,
    val task: DatasetTask
) {
    internal constructor(record: CSVRecord) : this(
        age = record[Column.AGE]?.toFloatOrNullWithDefault(-1.0f),
        programExperience = record[Column.PROGRAM_EXPERIENCE].toProgramExperience(),
        testsResults = record[Column.TESTS_RESULTS]?.toDouble() ?: throwNullFieldError(Column.TESTS_RESULTS),
        task = DatasetTask.createFromString(record[Column.TASK] ?: throwNullFieldError(Column.TASK))
    )

    val isFinalSolution: Boolean get() = testsResults == 1.0

    enum class ProgramExperience {
        LESS_THAN_HALF_YEAR,
        FROM_HALF_TO_ONE_YEAR,
        FROM_ONE_TO_TWO_YEARS,
        FROM_TWO_TO_FOUR_YEARS,
        FROM_FOUR_TO_SIX_YEARS,
        MORE_THAN_SIX
    }
}

private fun String?.toFloatOrNullWithDefault(default: Float): Float? {
    val value = this?.toFloatOrNull()
    if (value == default) {
        return null
    }
    return value
}

private fun String?.toProgramExperience(): MetaInfo.ProgramExperience? =
    this?.let {
        try {
            MetaInfo.ProgramExperience.valueOf(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

private fun throwNullFieldError(fieldName: String): Nothing {
    throw IllegalArgumentException("Field \"$fieldName\" has to exist")
}

private object Column {
    const val FRAGMENT = "fragment"
    const val AGE = "age"
    const val PROGRAM_EXPERIENCE = "programExperience"
    const val TESTS_RESULTS = "testsResults"
    const val TASK = "task"
    const val ID = "id"
}
