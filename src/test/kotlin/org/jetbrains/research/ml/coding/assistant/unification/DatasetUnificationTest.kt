package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolutionDataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.RecordMetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import javax.imageio.ImageIO


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
        for (task in taskTrackerDataset.tasks) {
            val imgFile = File("${task.taskName}_graph.png").apply { createNewFile() }
            val graph = SolutionSpace()
            task.solutions.parallelStream()
                .map { datasetUnification.transform(it) }
                .forEach {
                    synchronized(graph) {
                        graph.add(
                            it
                        )
                    }
                }

            val image = graph.generateImage()
            ImageIO.write(image, "PNG", imgFile)
        }
    }

    @Test
    fun testSolutionSpace() {
        val factory = PsiFileFactory.getInstance(project)
        fun getPsiFile(text: String) = ApplicationManager.getApplication().runReadAction<PsiFile> {
            factory.createFileFromText(
                PythonLanguage.getInstance(),
                text
            )
        }

        val graph = SolutionSpace()
        graph.add(
            DynamicSolution(
                listOf(
                    IntermediateSolution(getPsiFile("a=1"), null, RecordMetaInfo()),
                    IntermediateSolution(getPsiFile("a=11"), null, RecordMetaInfo()),
                )
            )
        )
        graph.add(
            DynamicSolution(
                listOf(
                    IntermediateSolution(getPsiFile("a=2"), null, RecordMetaInfo()),
                    IntermediateSolution(getPsiFile("a=11"), null, RecordMetaInfo()),
                )
            )
        )

        val imgFile = File("testSolutionSpace_graph.png").apply { createNewFile() }
        val image = graph.generateImage()
        ImageIO.write(image, "PNG", imgFile)
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
