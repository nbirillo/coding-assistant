package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.DatasetUtils
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test

@Ignore
class DatasetSolutionSpaceSerializerTest : ParametrizedBaseWithSdkTest(getTmpProjectDir(true)) {
    @Test
    fun testBasic() {
        val datasetUnification = project.service<DatasetUnification>()
        for (taskSolutions in DatasetUtils.DATASET.tasks) {
            val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
            taskSolutions.dynamicSolutions
                .map(datasetUnification::unify)
                .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

            val solutionSpace = solutionSpaceBuilder.build { CustomEdgeWeightCalculator(it) }

            assertNoThrowable {
                val encodedSolutionSpace = SerializationUtils.encodeSolutionSpace(solutionSpace)
                val decodedSolutionSpace = SerializationUtils.decodeSolutionSpace(encodedSolutionSpace)
                SerializationUtils.encodeSolutionSpace(decodedSolutionSpace)
            }
        }
    }

    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)
}
