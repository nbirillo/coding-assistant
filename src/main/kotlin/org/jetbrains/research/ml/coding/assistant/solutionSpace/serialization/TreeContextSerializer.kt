package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import com.github.gumtreediff.io.TreeIoUtils
import com.github.gumtreediff.tree.TreeContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

object TreeContextSerializer : KSerializer<TreeContext> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("treeContext", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TreeContext {
        val xmlString = decoder.decodeString()
        return TreeIoUtils.fromXml().generateFromString(xmlString)
    }

    override fun serialize(encoder: Encoder, value: TreeContext) {
        val outputStream = ByteArrayOutputStream()
        TreeIoUtils.toXml(value).writeTo(outputStream)
        val xmlString = outputStream.toString(StandardCharsets.UTF_8)
        encoder.encodeString(xmlString)
    }
}
