package org.jetbrains.research.ml.coding.assistant.report

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


interface CodeRepository {
    fun getCode(id: String): String

    companion object {
        fun filename(taskName: String): String = "${taskName}_code_repo.json"
    }
}

class CodeRepositoryImpl(file: File) : CodeRepository {
    private val content = Json.decodeFromString<Map<String, String>>(file.readText())
    override fun getCode(id: String): String = content[id]!!
}
