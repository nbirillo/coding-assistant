package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask


/**
 * Repository to store and fetch the original code fragment by id
 */
interface CodeRepository {
    fun getCode(id: String): String

    companion object {
        fun filename(datasetTask: DatasetTask): String = "${datasetTask.taskName}_code_repo.json"
    }
}

typealias OriginalCodeData = Map<String, String>

class CodeRepositoryImpl(private val codeData: OriginalCodeData) : CodeRepository {
    override fun getCode(id: String): String = codeData[id] ?: error("Identifier has to be in the repository")
}
