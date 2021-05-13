package org.jetbrains.research.ml.coding.assistant.system.hint

import org.jetbrains.research.ml.coding.assistant.dataset.model.indexOfPreferredFor
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jgrapht.GraphPath
import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.DijkstraShortestPath

abstract class HintVertexCalculator {
    abstract fun calculateHintVertex(
        solutionSpace: SolutionSpace,
        closestVertex: SolutionSpaceVertex,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex?

    override fun toString(): String = this::class.simpleName ?: "HintVertexCalculator"
}

@Deprecated("use DijkstraHintVertexCalculator")
object NaiveHintVertexCalculator : HintVertexCalculator() {
    override fun calculateHintVertex(
        solutionSpace: SolutionSpace,
        closestVertex: SolutionSpaceVertex,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        val successors = Graphs.successorListOf(solutionSpace.graph, closestVertex)
        return successors.firstOrNull()
    }
}

object PoissonPathHintVertexCalculator : HintVertexCalculator() {
    override fun calculateHintVertex(
        solutionSpace: SolutionSpace,
        closestVertex: SolutionSpaceVertex,
        partialSolution: PartialSolution
    ): SolutionSpaceVertex? {
        val dijkstra = DijkstraShortestPath(solutionSpace.graph)
        val paths = dijkstra.getPaths(closestVertex)
        val reachablePaths = solutionSpace.finalSolutions
            .mapNotNull { sink -> paths.getPath(sink)?.let { sink to it } }
            .toMap()
        val preferredFinalVertex = preferredVertex(partialSolution, reachablePaths.keys) ?: return null
        return reachablePaths[preferredFinalVertex]?.let(this::getNextVertex)
    }

    private fun preferredVertex(
        partialSolution: PartialSolution,
        vertices: Iterable<SolutionSpaceVertex>
    ): SolutionSpaceVertex? {
        val metaInfos = vertices
            .mapNotNull { vertex ->
                val preferredInfoIndex =
                    vertex.info.map { it.metaInfo }.indexOfPreferredFor(partialSolution.metaInfo)
                        ?: return@mapNotNull null
                vertex to vertex.info[preferredInfoIndex]
            }
        val preferredInfoIndex =
            metaInfos.map { it.second.metaInfo }.indexOfPreferredFor(partialSolution.metaInfo) ?: return null
        return metaInfos[preferredInfoIndex].first
    }

    private fun getNextVertex(path: GraphPath<SolutionSpaceVertex, SolutionSpaceEdge>): SolutionSpaceVertex? {
        val firstEdge = path.edgeList?.firstOrNull() ?: return null
        return path.graph.getEdgeTarget(firstEdge)
    }
}
