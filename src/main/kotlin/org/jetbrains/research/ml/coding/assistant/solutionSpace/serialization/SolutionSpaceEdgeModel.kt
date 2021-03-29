package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import kotlinx.serialization.Serializable
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertexID

@Serializable
data class SolutionSpaceEdgeModel(
    val sourceId: SolutionSpaceVertexID,
    val targetId: SolutionSpaceVertexID,
    val weight: Double
)
