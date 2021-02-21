package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.github.gumtreediff.actions.model.Action
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.WeightedEdge

class SolutionSpaceEdge(
    val calculatedWeight: Double,
    val actions: List<Action>
) : WeightedEdge()
