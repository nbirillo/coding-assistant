package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex

object SolutionSpaceSerializer : KSerializer<SolutionSpace> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SolutionSpace") {
        element<Set<SolutionSpaceVertex>>("vertices")
        element<Set<SolutionSpaceEdgeModel>>("edges")
    }

    override fun deserialize(decoder: Decoder): SolutionSpace {
        return decoder.decodeStructure(descriptor) {
            var vertices: Set<SolutionSpaceVertex>? = null
            var edgeModels: Set<SolutionSpaceEdgeModel>? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    ElementIndex.VERTICES -> vertices = decodeSerializableElement(descriptor, index, serializer())
                    ElementIndex.EDGES -> edgeModels = decodeSerializableElement(descriptor, index, serializer())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            if (vertices == null || edgeModels == null) {
                error("Serialization error")
            }
            SolutionSpace(
                vertices,
                edgeModels
            )
        }
    }

    override fun serialize(encoder: Encoder, value: SolutionSpace) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, ElementIndex.VERTICES, serializer(), value.graph.vertexSet())
            encodeSerializableElement(descriptor, ElementIndex.EDGES, serializer(), value.edgeModels())
        }
    }

    private fun SolutionSpace.edgeModels(): Set<SolutionSpaceEdgeModel> {
        return graph.edgeSet().map { edge ->
            SolutionSpaceEdgeModel(
                graph.getEdgeSource(edge).id,
                graph.getEdgeTarget(edge).id,
                graph.getEdgeWeight(edge)
            )
        }.toSet()
    }

    object ElementIndex {
        const val VERTICES = 0
        const val EDGES = 1
    }
}
