package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceDirectoryRepository
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.finder.NaiveVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.NaiveHintVertexCalculator
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test
import java.io.File

class HintManagerTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::HintManagerTest)) {
    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)

    @Test
    fun testBasic() {
        val solutionSpaceRepository = SolutionSpaceDirectoryRepository(
            File("/Users/artembobrov/Documents/masters/ast-transform/coding-assistant/output")
        )
        val vertexFinder = NaiveVertexFinder(EditPartialSolutionMatcher)
        val hintFactory = HintFactoryImpl(solutionSpaceRepository, vertexFinder, NaiveHintVertexCalculator)
        val hintManager = HintManagerImpl(hintFactory)
        val codeFragment = """
            x = int(input())
            y = int(input())
            z = int(input())
            if x < y:
        """.trimIndent()
        val psiCreator = project.service<PsiCreator>()
        val psiFileWrapper = psiCreator.initFileToPsi(codeFragment)
        val datasetTask = DatasetTask.MAX_3
        val metaInfo = MetaInfo(12.0f, null, 0.32, datasetTask)
        val hintedFile = hintManager.getHintedFile(datasetTask, psiFileWrapper, metaInfo)
        println(
            """
Hinted:
${hintedFile?.text}
            """.trimIndent()
        )
        psiFileWrapper.deleteFile()
    }
}
