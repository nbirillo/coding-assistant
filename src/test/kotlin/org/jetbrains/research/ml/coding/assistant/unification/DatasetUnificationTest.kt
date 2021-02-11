package org.jetbrains.research.ml.coding.assistant.unification

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import com.mxgraph.util.mxConstants.DEFAULT_FONTSIZE
import com.mxgraph.util.mxConstants.STYLE_FONTSIZE
import com.mxgraph.view.mxStylesheet
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolutionDataset
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.jgrapht.ext.JGraphXAdapter
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO


@RunWith(Parameterized::class)
class DatasetUnificationTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::DatasetUnificationTest)) {
    @Before
    override fun mySetUp() {
        super.mySetUp()
    }

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: String? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: String? = null

    @Test
    fun testBasic() {
        val datasetUnification = DatasetUnification(project)
        for (task in taskTrackerDataset.tasks.take(1)) {
            val imgFile = File("${task.taskName}_graph.png").apply { createNewFile() }
            val graph = SolutionSpace()
            task.solutions.take(1).map { datasetUnification.transform(it) }.forEach {
                synchronized(graph) {
                    graph.add(
                        it
                    )
                }
            }
            val graphAdapter = JGraphXAdapter(graph.graph)
            graphAdapter.stylesheet.defaultVertexStyle[STYLE_FONTSIZE] = 6
            graphAdapter.stylesheet.defaultEdgeStyle[STYLE_FONTSIZE] = 6
            graphAdapter.isAutoSizeCells = true
            graphAdapter.updateCellSize(graphAdapter)
            val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
            layout.execute(graphAdapter.defaultParent)
            val image =
                mxCellRenderer.createBufferedImage(
                    graphAdapter, null, 2.0, Color.WHITE, true, null
                )
            ImageIO.write(image, "PNG", imgFile)
        }
    }

    companion object {
        lateinit var taskTrackerDataset: DynamicSolutionDataset

        @BeforeClass
        @JvmStatic
        fun setup() {
            taskTrackerDataset =
                TaskTrackerDatasetFetcher.fetchDataset(File("/Users/artembobrov/Documents/masters/ast-transform/python"))
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(arrayOf("", ""))
    }
}
