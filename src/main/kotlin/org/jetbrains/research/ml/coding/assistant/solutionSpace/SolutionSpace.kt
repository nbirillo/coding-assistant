package org.jetbrains.research.ml.coding.assistant.solutionSpace

import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge

class SolutionSpace {
    val graph: Graph<IntermediateSolution, DefaultWeightedEdge> =
        DefaultDirectedWeightedGraph(DefaultWeightedEdge::class.java)

    fun add(dynamicSolution: DynamicSolution) {
        for (solution in dynamicSolution.solutions.filter { !graph.containsVertex(it) })
            graph.addVertex(solution)
        for ((source, target) in dynamicSolution.solutions.windowed(2, partialWindows = false)) {
            graph.addEdge(source, target)
        }
    }
}
