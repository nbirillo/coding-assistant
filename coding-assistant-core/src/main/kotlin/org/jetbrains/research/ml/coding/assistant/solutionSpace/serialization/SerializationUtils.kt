package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.jetbrains.research.ml.coding.assistant.report.OriginalCodeData
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace

object SerializationUtils {
    private val json = Json {
        prettyPrint = true
        allowSpecialFloatingPointValues = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val protoBuf = ProtoBuf

    fun encodeSolutionSpace(solutionSpace: SolutionSpace): ByteArray {
        return protoBuf.encodeToByteArray(SolutionSpaceSerializer, solutionSpace)
    }

    fun decodeSolutionSpace(byteArray: ByteArray): SolutionSpace {
        return protoBuf.decodeFromByteArray(SolutionSpaceSerializer, byteArray)
    }

    fun encodeCodeData(solutionSpace: OriginalCodeData): String {
        return json.encodeToString(solutionSpace)
    }

    fun decodeCodeData(content: String): OriginalCodeData {
        return json.decodeFromString(content)
    }
}
