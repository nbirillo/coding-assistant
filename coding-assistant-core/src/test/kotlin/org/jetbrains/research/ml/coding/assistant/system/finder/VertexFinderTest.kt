package org.jetbrains.research.ml.coding.assistant.system.finder

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.DatasetUtils
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.jetbrains.research.ml.coding.assistant.utils.Util
import org.junit.Ignore
import org.junit.Test

@Ignore
class VertexFinderTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::VertexFinderTest)) {
    @Test
    fun testBasic() {
        val taskSolution = DatasetUtils.DATASET.tasks.first()
        val datasetUnification = project.service<DatasetUnification>()

        val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
        taskSolution.dynamicSolutions
            .map { datasetUnification.unify(it) }
            .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

        val solutionSpace = solutionSpaceBuilder.build { CustomEdgeWeightCalculator(it) }
        val finder = ParallelVertexFinder(EditPartialSolutionMatcher)

        val fragment = """
            x = input()
            """.trimIndent()
        val psiFile = createPsiFile(fragment)
        val context = Util.getTreeContext(psiFile)
        val partialSolution = PartialSolution(
            taskSolution.datasetTask,
            context,
            psiFile,
            MetaInfo(10.0f, null, 0.1, DatasetTask.BRACKETS)
        )
        assertNoThrowable {
            finder.findCorrespondingVertex(solutionSpace, partialSolution)
        }
        psiFile.deleteFile()
    }

    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)

    private fun createPsiFile(text: String): PsiFileWrapper {
        return project.service<PsiCreator>().initFileToPsi(text).apply {
            ApplicationManager.getApplication().invokeAndWait {
                CompositeTransformation.forwardApply(this, null)
            }
        }
    }
}
