package org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator

import com.github.gumtreediff.actions.model.Action
import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jgrapht.Graph

class EdgeActionsCache(
    private val graph: Graph<SolutionSpaceVertex, SolutionSpaceEdge>,
    private val map: MutableMap<SolutionSpaceEdge, List<Action>> = mutableMapOf()
) : MutableMap<SolutionSpaceEdge, List<Action>> by map {

    override fun get(key: SolutionSpaceEdge): List<Action> {
        return map.getOrPut(key) {
            val sourceFragment = graph.getEdgeSource(key).fragment
            val targetFragment = graph.getEdgeTarget(key).fragment
            val matcher = Matcher(sourceFragment, targetFragment)
            matcher.getEditActions()
        }
    }
}
