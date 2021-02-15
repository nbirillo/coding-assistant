package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.heuristics.createHeuristicsSupportGraph
import org.jetbrains.research.ml.coding.assistant.solutionSpace.heuristics.createSolutionSpaceSupportGraph
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.generateImage
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import javax.imageio.ImageIO
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


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
            val intermediateSolutions = datasetUnification.transform(taskSolutions)
            val graph = SolutionSpace(intermediateSolutions)

            val imgFile = File("${taskSolutions.taskName}_graph.png").apply { createNewFile() }
            val image = graph.generateImage()
            ImageIO.write(image, "PNG", imgFile)
        }
    }

    @ExperimentalTime
    @Test
    fun testTime() {
        val datasetUnification = DatasetUnification(project)

        for (taskSolutions in taskTrackerDataset.tasks) {
            val time = measureTime {
                val intermediateSolutions = datasetUnification.transform(taskSolutions)
                val graph = SolutionSpace(intermediateSolutions)
            }
            val textFile = File("${taskSolutions.taskName}_time.text").apply { createNewFile() }
            textFile.writeText("Time to create ss ${taskSolutions.taskName} is $time")
        }
    }

    @Test
    fun testSolutionSpaceSupportConnectedComponent() {
        val factory = PsiFileFactory.getInstance(project)
        fun getPsiFile(text: String) = ApplicationManager.getApplication().runReadAction<PsiFile> {
            factory.createFileFromText(
                PythonLanguage.getInstance(),
                text
            )
        }

        var id = 0

        fun metaInfo(score: Double = 0.0) = MetaInfo(null, null, score, "")
        fun intermediateSolution(code: String, score: Double = 0.0) = IntermediateSolution(
            (id++).toString(),
            getPsiFile(code),
            null,
            metaInfo(score)
        )

        val intermediateSolutions = listOf(
            intermediateSolution("a=1+1", 0.7),
            intermediateSolution("a=11"),
            intermediateSolution("a=2+4*3", 1.0),
            intermediateSolution("a=11"),
            intermediateSolution("a=1+1+1+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+1"),
            intermediateSolution("a=1+1+1+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11+11"),
        )

        val vertices = intermediateSolutions.map { SolutionSpaceVertex(it) }
        val heuristicsSupportGraph = createHeuristicsSupportGraph(vertices)
        val solutionSpaceSupportGraph = createSolutionSpaceSupportGraph(heuristicsSupportGraph, 1)
        File("heuristicsSupportGraph.png").apply {
            createNewFile()
            ImageIO.write(heuristicsSupportGraph.generateImage(), "PNG", this)
        }
        File("solutionSpaceSupportGraph.png").apply {
            createNewFile()
            ImageIO.write(solutionSpaceSupportGraph.generateImage(), "PNG", this)
        }
    }

    companion object {
        lateinit var taskTrackerDataset: Dataset

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
