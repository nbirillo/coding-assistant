package org.jetbrains.research.ml.coding.assistant.system.finder

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.Util
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.DatasetUtils
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@Ignore
@RunWith(Parameterized::class)
class VertexFinderTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::VertexFinderTest)) {
    @Test
    fun testBasic() {
        val taskSolutions = DatasetUtils.DATASET.tasks.first()
        val datasetUnification = project.service<DatasetUnification>()

        val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
        taskSolutions.dynamicSolutions
            .map { datasetUnification.unify(it) }
            .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

        val solutionSpace = solutionSpaceBuilder.build { CustomEdgeWeightCalculator(it) }
        val finder = ParallelVertexFinder(EditPartialSolutionMatcher)

        val context = Util.getTreeContext(
            createPsiFile(
                """
                    x = input()
                """.trimIndent()
            ),
        )
        val partialSolution = PartialSolution(
            context,
            MetaInfo(10.0f, null, 0.1, DatasetTask.BRACKETS)
        )
        val vertex = finder.findCorrespondingVertex(solutionSpace, partialSolution)
        print(vertex)
    }

    private fun createPsiFile(text: String): PsiFile {
        return project.service<PsiCreator>().initFileToPsi(text).apply {
            ApplicationManager.getApplication().invokeAndWait {
                CompositeTransformation.forwardApply(this, null)
            }
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(arrayOf("", ""))
    }
}
