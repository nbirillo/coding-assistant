package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import com.intellij.openapi.components.service
import kotlinx.serialization.json.Json
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@Ignore
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
        val inputDir = "/Users/artembobrov/Documents/masters/ast-transform/python/max_digit"
        val taskSolutions = TaskTrackerDatasetFetcher.fetchTaskSolutions(File(inputDir))
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

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(arrayOf("", ""))
    }
}
