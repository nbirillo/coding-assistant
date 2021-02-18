package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetRecord
import org.jetbrains.research.ml.coding.assistant.graph.generateImage
import org.jetbrains.research.ml.coding.assistant.graph.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import javax.imageio.ImageIO

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
            val imgFile = File("${taskSolutions.taskName}_graph.png").apply { createNewFile() }
            val intermediateSolutions = datasetUnification.transform(taskSolutions)
            val graph = SolutionSpace(intermediateSolutions)

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

        fun metaInfo() = DatasetRecord.MetaInfo(null, null, 0.0, "")

        val intermediateSolutions = listOf(
            IntermediateSolution(
                "1",
                getPsiFile("a=1+1"),
                null,
                metaInfo()
            ),
            IntermediateSolution(
                "2",
                getPsiFile("a=11"),
                null,
                metaInfo()
            ),
            IntermediateSolution(
                "3",
                getPsiFile("a=2+4*3"),
                null,
                metaInfo()
            ),
            IntermediateSolution(
                "4",
                getPsiFile("a=11"),
                null,
                DatasetRecord.MetaInfo(21.0f, null, 0.0, "")
            )
        )

//        val graph = createHeuristicsSupportGraph(intermediateSolutions)
        val graph = SolutionSpace(intermediateSolutions)
        val imgFile = File("testSolutionSpace_graph.png").apply { createNewFile() }
        val image = graph.generateImage()
        ImageIO.write(image, "PNG", imgFile)
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
