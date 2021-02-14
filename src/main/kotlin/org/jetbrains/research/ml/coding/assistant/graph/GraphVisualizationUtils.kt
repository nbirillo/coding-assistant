package org.jetbrains.research.ml.coding.assistant.graph

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import com.mxgraph.util.mxConstants
import com.mxgraph.view.mxStylesheet
import org.jetbrains.research.ml.coding.assistant.graph.GraphVisualizationUtils.FINAL_SOLUTION_NAME
import org.jetbrains.research.ml.coding.assistant.graph.solutionSpace.SolutionSpace
import org.jgrapht.Graph
import org.jgrapht.ext.JGraphXAdapter
import java.awt.Color
import java.awt.image.BufferedImage

fun <V, E> Graph<V, E>.generateImage(): BufferedImage {
    val graphAdapter = JGraphXAdapter(this)
    graphAdapter.stylesheet.defaultVertexStyle[mxConstants.STYLE_FONTSIZE] = 6
    graphAdapter.stylesheet.defaultEdgeStyle[mxConstants.STYLE_FONTSIZE] = 4
    graphAdapter.stylesheet.defaultEdgeStyle[mxConstants.STYLE_SPACING_RIGHT] = 4
    graphAdapter.isAutoSizeCells = true
    val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
    layout.execute(graphAdapter.defaultParent)

    return mxCellRenderer.createBufferedImage(
        graphAdapter, null, 3.0, Color.WHITE, true, null
    )
}


fun SolutionSpace.generateImage(): BufferedImage {
    val graphAdapter = JGraphXAdapter(graph)
    GraphVisualizationUtils.updateDefaultStyle(graphAdapter.stylesheet)
    graphAdapter.stylesheet.styles[FINAL_SOLUTION_NAME] = graphAdapter.stylesheet.defaultVertexStyle.toMutableMap()
    GraphVisualizationUtils.updateFinalSolutionStyle(graphAdapter.stylesheet)
    graphAdapter.isAutoSizeCells = true

    graph.vertexSet()
        .filter { it.isFinal }
        .forEach {
            graphAdapter.vertexToCellMap[it]?.style = FINAL_SOLUTION_NAME
        }

    val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
    layout.execute(graphAdapter.defaultParent)

    return mxCellRenderer.createBufferedImage(
        graphAdapter, null, 3.0, Color.WHITE, true, null
    )
}

private object GraphVisualizationUtils {
    const val FINAL_SOLUTION_NAME = "final_solution"

    fun updateDefaultStyle(stylesheet: mxStylesheet) {
        stylesheet.defaultVertexStyle[mxConstants.STYLE_FONTSIZE] = 6
        stylesheet.defaultEdgeStyle[mxConstants.STYLE_FONTSIZE] = 4
        stylesheet.defaultEdgeStyle[mxConstants.STYLE_SPACING_RIGHT] = 4
    }

    fun updateFinalSolutionStyle(stylesheet: mxStylesheet) {
        stylesheet.finalSolutionStyle[mxConstants.STYLE_FILLCOLOR] = "#FF0000"
    }
}

private val mxStylesheet.finalSolutionStyle get() = styles[FINAL_SOLUTION_NAME]!!
