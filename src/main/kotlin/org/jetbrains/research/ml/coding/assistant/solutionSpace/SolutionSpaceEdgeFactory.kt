package org.jetbrains.research.ml.coding.assistant.solutionSpace

import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jgrapht.EdgeFactory

object SolutionSpaceEdgeFactory : EdgeFactory<SolutionSpaceVertex, SolutionSpaceEdge> {
    override fun createEdge(sourceVertex: SolutionSpaceVertex, targetVertex: SolutionSpaceVertex): SolutionSpaceEdge {
        val matcher = Matcher(sourceVertex.fragment, targetVertex.fragment)
        return SolutionSpaceEdge(matcher.getEditActions())
    }
}
