package org.jetbrains.research.ml.coding.assistant.dataset.model

typealias Record = Map<String, String>

data class DatasetRecord(
    val id: String,
    val fragment: String,
    val metaInfo: MetaInfo
) {

    internal constructor(record: Record) : this(
        id = record[ID]!!,
        fragment = record[FRAGMENT] ?: "",
        metaInfo = MetaInfo(record)
    )

    data class MetaInfo(
        val age: Float?,
        val programExperience: ProgramExperience?,
        val testsResults: Double,
        val task: String
    ) {
        internal constructor(record: Record) : this(
            age = record[AGE]?.toFloatOrNull(),
            programExperience = record[PROGRAM_EXPERIENCE].toProgramExperience(),
            testsResults = record[TESTS_RESULTS]!!.toDouble(),
            task = record[TASK]!!
        )
    }

    val isMeaningful: Boolean get() = fragment.trim(' ', '\n').isNotEmpty()

    enum class ProgramExperience {
        LESS_THAN_HALF_YEAR,
        FROM_HALF_TO_ONE_YEAR,
        FROM_ONE_TO_TWO_YEARS,
        FROM_TWO_TO_FOUR_YEARS,
        FROM_FOUR_TO_SIX_YEARS,
        MORE_THAN_SIX
    }

    companion object {
        private const val FRAGMENT = "fragment"
        private const val AGE = "age"
        private const val PROGRAM_EXPERIENCE = "programExperience"
        private const val TESTS_RESULTS = "testsResults"
        private const val TASK = "task"
        private const val ID = "id"
    }
}

private fun String?.toProgramExperience(): DatasetRecord.ProgramExperience? =
    this?.let {
        try {
            DatasetRecord.ProgramExperience.valueOf(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
