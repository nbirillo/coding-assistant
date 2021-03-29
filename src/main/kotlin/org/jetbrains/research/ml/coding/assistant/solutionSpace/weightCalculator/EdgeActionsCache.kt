package org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator

import com.github.gumtreediff.actions.model.Action
import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jgrapht.Graph

/**
 * In memory cache to store for every edge in the `graph` a list of edits
 * to transform source vertex into target vertex.
 * Calculates a list of edits only if there is no already calculated edits list for this edge in cache.
 */
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
