package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import org.jgrapht.graph.DefaultWeightedEdge
import java.text.DecimalFormat

/**
 * Weighted edge with pretty formatted toString
 */
open class WeightedEdge : DefaultWeightedEdge() {
    override fun toString(): String = "[${format.format(weight)}]"

    companion object {
        val format = DecimalFormat().apply {
            maximumFractionDigits = 2
        }
    }
}
