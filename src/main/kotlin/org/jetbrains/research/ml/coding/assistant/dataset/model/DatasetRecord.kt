package org.jetbrains.research.ml.coding.assistant.dataset.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class RecordMetaInfo(
    val date: LocalDateTime?,
    val timestamp: Int?,
    val filename: String?,
    val fileHashCode: String?,
    val documentHashCode: Int?,
    val chosenTask: String?,
    val writtenTask: String?,
    val age: Int?,
    val programExperience: String?, // TODO: enum
    val taskStatus: String?, // TODO: enum
    val activePane: String?,  // TODO: enum
    val testsResults: List<Int>,
    val language: String?,
    val timestampAti: LocalDateTime?,
    val eventType: String?, // TODO: enum
    val eventData: String? // TODO: enum
) {
    internal constructor(record: Map<String, String>) : this(
        date = localDateFromString(record[DATE]),
        timestamp = record[TIMESTAMP]?.toIntOrNull(),
        filename = record[FILE_NAME],
        fileHashCode = record[FILE_HASH_CODE],
        documentHashCode = record[DOCUMENT_HASH_CODE]?.toIntOrNull(),
        chosenTask = record[CHOSEN_TASK],
        writtenTask = record[WRITTEN_TASK],
        age = record[AGE]?.toIntOrNull(),
        programExperience = record[PROGRAM_EXPERIENCE],
        taskStatus = record[TASK_STATUS],
        activePane = record[ACTIVE_PANE],
        testsResults = listOfInt(record[TESTS_RESULTS]),
        language = record[LANGUAGE],
        timestampAti = localDateFromString(record[TIMESTAMP_ATI]),
        eventType = record[EVENT_TYPE],
        eventData = record[EVENT_DATA],
    )

    companion object {
        private const val DATE = "date"
        private const val TIMESTAMP = "timestamp"
        private const val FILE_NAME = "fileName"
        private const val FILE_HASH_CODE = "fileHashCode"
        private const val DOCUMENT_HASH_CODE = "documentHashCode"
        private const val CHOSEN_TASK = "chosenTask"
        private const val WRITTEN_TASK = "writtenTask"
        private const val AGE = "age"
        private const val PROGRAM_EXPERIENCE = "programExperience"
        private const val TASK_STATUS = "taskStatus"
        private const val ACTIVE_PANE = "activePane"
        private const val TESTS_RESULTS = "testsResults"
        private const val LANGUAGE = "language"
        private const val TIMESTAMP_ATI = "timestampAti"
        private const val EVENT_TYPE = "eventType"
        private const val EVENT_DATA = "eventData"
    }
}

data class DatasetRecord(
    val fragment: String,
    val metaInfo: RecordMetaInfo
) {
    constructor(record: Map<String, String>) : this(record[Keys.FRAGMENT] ?: "", RecordMetaInfo(record))

    companion object Keys {
        private const val FRAGMENT = "fragment"
    }
}

private fun localDateFromString(string: String?): LocalDateTime? = string.takeUnless { it.isNullOrEmpty() }?.let {
    LocalDateTime.parse(
        it,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
    )
}

private fun listOfInt(string: String?): List<Int> = listOf()

