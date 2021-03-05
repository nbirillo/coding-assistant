package org.jetbrains.research.ml.coding.assistant.system.finder

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.gumtree.tree.PostOrderNumbering
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.Util
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.impl.TestPsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@Ignore
@RunWith(Parameterized::class)
class VertexFinderTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::VertexFinderTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: String? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: String? = null

    override fun setUp() {
        super.setUp()
        (project.service<PsiCreator>() as? TestPsiCreator)?.fixture = myFixture
    }

    @Test
    fun testBasic() {
        val inputDir = "/Users/artembobrov/Documents/masters/ast-transform/python/max_digit"
        val taskSolutions = TaskTrackerDatasetFetcher.fetchTaskSolutions(File(inputDir))
        println(taskSolutions.dynamicSolutions.size)
        val datasetUnification = project.service<DatasetUnification>()

        val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
        taskSolutions.dynamicSolutions
            .map { datasetUnification.transform(it) }
            .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

        val solutionSpace = solutionSpaceBuilder.build()
        val finder = ParallelVertexFinder(EditPartialSolutionMatcher)

        val context = Util.getTreeContext(
            createPsiFile(
                """
                    x = input()
                """.trimIndent()
            ),
            PostOrderNumbering
        )
        val partialSolution = PartialSolution(
            context,
            MetaInfo(10.0f, null, 0.1, "test")
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
