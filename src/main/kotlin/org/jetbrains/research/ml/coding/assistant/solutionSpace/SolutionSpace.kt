package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import com.mxgraph.util.mxConstants
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jgrapht.Graph
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.awt.Color
import java.awt.image.BufferedImage

class SolutionSpace {
    private val graph: Graph<IntermediateSolution, DefaultWeightedEdge> =
        SimpleDirectedWeightedGraph(DefaultWeightedEdge::class.java)

    fun add(dynamicSolution: DynamicSolution) {
        for (solution in dynamicSolution.solutions)
            graph.addVertex(solution)
    }

    fun generateImage(): BufferedImage {
        val graphAdapter = JGraphXAdapter(graph)
        graphAdapter.stylesheet.defaultVertexStyle[mxConstants.STYLE_FONTSIZE] = 6
        graphAdapter.stylesheet.defaultEdgeStyle[mxConstants.STYLE_FONTSIZE] = 6
        graphAdapter.isAutoSizeCells = true
        val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
        layout.execute(graphAdapter.defaultParent)

        return mxCellRenderer.createBufferedImage(
            graphAdapter, null, 3.0, Color.WHITE, true, null
        )
    }
}
