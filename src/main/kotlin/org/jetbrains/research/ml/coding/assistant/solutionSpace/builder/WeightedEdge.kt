package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import org.jgrapht.graph.DefaultWeightedEdge
import java.text.DecimalFormat

open class WeightedEdge : DefaultWeightedEdge() {
    override fun toString(): String = "[${format.format(weight)}]"

    companion object {
        val format = DecimalFormat().apply {
            maximumFractionDigits = 2
        }
    }
}
