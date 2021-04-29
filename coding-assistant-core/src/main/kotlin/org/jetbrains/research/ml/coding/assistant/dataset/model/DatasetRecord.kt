package org.jetbrains.research.ml.coding.assistant.dataset.model

import kotlinx.serialization.Serializable
import kotlin.math.abs

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
    val age: Float,
    val programExperience: ProgramExperience,
    val testsResults: Double,
    val task: DatasetTask
) {
    internal constructor(record: CSVRecord) : this(
        age = record[Column.AGE]?.toFloatOrNull() ?: -1.0f,
        programExperience = record[Column.PROGRAM_EXPERIENCE].toProgramExperience()
            ?: ProgramExperience.LESS_THAN_HALF_YEAR,
        testsResults = record[Column.TESTS_RESULTS]?.toDouble() ?: 0.0,
        task = DatasetTask.createFromString(record[Column.TASK] ?: throwNullFieldError(Column.TASK))
    )

    fun toComparableVector(): List<Double> =
        listOf(programExperience.peLevel.toDouble(), age.toDouble(), testsResults)

    val isFinalSolution: Boolean get() = testsResults == 1.0

    enum class ProgramExperience(val peLevel: Int, val maxMonths: Int) : Comparable<ProgramExperience> {
        LESS_THAN_HALF_YEAR(0, 6),
        FROM_HALF_TO_ONE_YEAR(1, 12),
        FROM_ONE_TO_TWO_YEARS(2, 24),
        FROM_TWO_TO_FOUR_YEARS(3, 48),
        FROM_FOUR_TO_SIX_YEARS(4, 72),
        MORE_THAN_SIX(5, Int.MAX_VALUE);

        companion object {
            fun createFromMonths(months: Int): ProgramExperience {
                return values().first { it.maxMonths < months }
            }
        }
    }

    companion object {
        private fun String?.toProgramExperience(): ProgramExperience? =
            this?.let {
                try {
                    ProgramExperience.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
    }
}

fun List<MetaInfo>.indexOfPreferredFor(metaInfo: MetaInfo): Int? {
    val metaInfoVector = metaInfo.toComparableVector()

    val vectors = map { info ->
        (metaInfoVector zip info.toComparableVector()).map { abs(it.first - it.second) }
    }

    return vectors.indices.minByOrNull { LexicographicallyCompare(vectors[it]) }
}

data class LexicographicallyCompare(val list: List<Comparable<*>>) : Comparable<LexicographicallyCompare> {
    override fun compareTo(other: LexicographicallyCompare): Int {
        for ((element, otherElement) in list zip other.list) {
            if (element.javaClass != otherElement.javaClass) {
                throw IllegalArgumentException()
            }

            compareValues(element, otherElement).let {
                if (it != 0) return it
            }
        }
        return compareValues(list.size, other.list.size)
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
