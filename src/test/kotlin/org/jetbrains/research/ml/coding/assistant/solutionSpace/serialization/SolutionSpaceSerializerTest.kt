package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import com.intellij.openapi.components.service
import com.jetbrains.python.PythonFileType
import kotlinx.serialization.json.Json
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.Util
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SolutionSpaceSerializerTest : ParametrizedBaseWithSdkTest(getTmpProjectDir(true)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: String? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: String? = null

    @Test
    fun testBasic() {
        val taskSolutions = TaskTrackerDatasetFetcher.fetchTaskSolutions(File(INPUT_DIR))
        val datasetUnification = project.service<DatasetUnification>()

        val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
        taskSolutions.dynamicSolutions
            .take(1)
            .map { datasetUnification.transform(it) }
            .forEach { solutionSpaceBuilder.addDynamicSolution(it.take(1)) }

        val solutionSpace = solutionSpaceBuilder.build()

        val json = Json { prettyPrint = true }
        val encodedSolutionSpace = json.encodeToString(SolutionSpaceSerializer, solutionSpace)
        val decodedSolutionSpace = json.decodeFromString(SolutionSpaceSerializer, encodedSolutionSpace)
        val encodedDecodedSolutionSpace = json.encodeToString(SolutionSpaceSerializer, decodedSolutionSpace)
        assertEquals(encodedSolutionSpace, encodedDecodedSolutionSpace)
        assertEquals(solutionSpace, decodedSolutionSpace)
    }

    @Test
    fun testSerial() {
        val code = """
print(max(input()))

    """.trimIndent()
        val psiFile = myFixture.configureByText(PythonFileType.INSTANCE, code)
        val treeContext = Util.getTreeContext(psiFile)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(TreeContextSerializer, treeContext)
        val decodedContext = json.decodeFromString(TreeContextSerializer, jsonString)
        assertEquals(treeContext.toString(), decodedContext.toString())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(arrayOf("", ""))
        const val INPUT_DIR: String = "specify your path the dataset task's solution"
    }
}
