package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.research.ml.coding.assistant.report.OriginalCodeData
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace

object SerializationUtils {
    private val json = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }

    fun encodeSolutionSpace(solutionSpace: SolutionSpace): String {
        return json.encodeToString(SolutionSpaceSerializer, solutionSpace)
    }

    fun decodeSolutionSpace(content: String): SolutionSpace {
        return json.decodeFromString(SolutionSpaceSerializer, content)
    }

    fun encodeCodeData(solutionSpace: OriginalCodeData): String {
        return json.encodeToString(solutionSpace)
    }

    fun decodeCodeData(content: String): OriginalCodeData {
        return json.decodeFromString(content)
    }
}
