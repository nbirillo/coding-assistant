package org.jetbrains.research.ml.coding.assistant.unification

import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.generateImage
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.jgrapht.Graph
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import javax.imageio.ImageIO
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Ignore
@RunWith(Parameterized::class)
class DatasetUnificationTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::DatasetUnificationTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: String? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: String? = null

    @Test
    fun testBasic() {
        val datasetUnification = DatasetUnification(project)

        for (taskSolutions in taskTrackerDataset.tasks) {
            val builder = SolutionSpaceGraphBuilder()
            taskSolutions.dynamicSolutions
                .take(3)
                .map {
                    datasetUnification.transform(it)
                }
                .forEach {
                    builder.addDynamicSolution(it)
                }

            val graph = builder.build()
            val imgFile = File("${taskSolutions.taskName}_graph.png").apply { createNewFile() }
            val image = graph.generateImage()
            ImageIO.write(image, "PNG", imgFile)
        }
    }

    private fun <V, E> getInfo(graph: Graph<V, E>): String {
        return """
                Vertex count: ${graph.vertexSet().size}
                Edge count: ${graph.edgeSet().size}
                Vertices: ${graph.vertexSet().map { it.toString() }.sorted().joinToString()}
                Edges: ${
            graph.edgeSet().map {
                "| (${graph.getEdgeSource(it)}, ${graph.getEdgeTarget(it)})  ${it.toString()}|"
            }.sorted().joinToString()
        }
                """.trimIndent()
    }

    @ExperimentalTime
    @Test
    fun testTime() {
        val datasetUnification = DatasetUnification(project)

        for (taskSolutions in taskTrackerDataset.tasks) {
            var space: SolutionSpace
            var builder: SolutionSpaceGraphBuilder
            val time = measureTime {
                builder = SolutionSpaceGraphBuilder()

                taskSolutions.dynamicSolutions
                    .map {
                        datasetUnification.transform(it)
                    }
                    .forEach {
                        builder.addDynamicSolution(it)
                    }


                space = builder.build()
            }

            val textFile = File("${taskSolutions.taskName}_time.text").apply { createNewFile() }
            val text = buildString {
                appendLine("Time: $time")
                appendLine(getInfo(builder.graph))
//                appendLine(getInfo(space.graph))
            }

            textFile.writeText(text)


        }
    }

    companion object {
        lateinit var taskTrackerDataset: Dataset

        @BeforeClass
        @JvmStatic
        fun setup() {
            val path = "/Users/artembobrov/Documents/masters/ast-transform/python"
            taskTrackerDataset = TaskTrackerDatasetFetcher.fetchDataset(File(path))
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(arrayOf("", ""))
    }
}
