package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.util.DatasetUtils
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test

class DatasetUnificationTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::DatasetUnificationTest)) {
    @Test
    fun testSolutionSpaceBuild() {
        val datasetUnification = project.service<DatasetUnification>()

        for (taskSolutions in DatasetUtils.DATASET.tasks) {
            val builder = SolutionSpaceGraphBuilder()
            taskSolutions.dynamicSolutions
                .map(datasetUnification::unify)
                .forEach { builder.addDynamicSolution(it) }
            assertNoThrowable {
                builder.build { CustomEdgeWeightCalculator(it) }
            }
        }
    }

    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)
}
