package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertexID

object SolutionSpaceSerializer : KSerializer<SolutionSpace> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Color") {
        element<Set<SolutionSpaceVertex>>("vertices")
        element<List<Pair<SolutionSpaceVertexID, SolutionSpaceVertexID>>>("edges")
    }

    override fun deserialize(decoder: Decoder): SolutionSpace {
        return decoder.decodeStructure(descriptor) {
            var vertices: Set<SolutionSpaceVertex>? = null
            var edgePairs: List<Pair<SolutionSpaceVertexID, SolutionSpaceVertexID>>? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> vertices = decodeSerializableElement(descriptor, 0, serializer())
                    1 -> edgePairs = decodeSerializableElement(descriptor, 1, serializer())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            SolutionSpace(
                vertices!!,
                edgePairs!!
            )
        }
    }

    override fun serialize(encoder: Encoder, value: SolutionSpace) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, serializer(), value.graph.vertexSet())
            encodeSerializableElement(descriptor, 1, serializer(), value.edgesIdPairs())
        }
    }

    private fun SolutionSpace.edgesIdPairs(): List<Pair<SolutionSpaceVertexID, SolutionSpaceVertexID>> {
        return graph.edgeSet().map { edge ->
            graph.getEdgeSource(edge).id to graph.getEdgeTarget(edge).id
        }
    }
}
