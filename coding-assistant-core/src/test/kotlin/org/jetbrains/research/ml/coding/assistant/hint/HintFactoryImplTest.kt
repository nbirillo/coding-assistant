package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.PoissonPathHintVertexCalculator
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditCountPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.util.DatasetUtils
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test

class HintFactoryImplTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::HintFactoryImplTest)) {
    @Test
    fun testBasic() {
        val fragment = """
            str = input()
            res = []
            for offset in range(len(str)):
                if offset < len(str) // 2:
                    pass
            """.trimIndent()
        val hintManager = HintManagerImpl(
            HintFactoryImpl(
                DatasetUtils.DATASET_REPOSITORY,
                ParallelVertexFinder(EditCountPartialSolutionMatcher),
                PoissonPathHintVertexCalculator
            )
        )
        val psiFile = createPsiFile(fragment)
        val metaInfo = MetaInfo(10.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS)
        val file = hintManager.getHintedFile(psiFile, metaInfo)
        print(file?.psiFragment?.text)
        psiFile.deleteFile()
    }

    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)

    private fun createPsiFile(text: String): PsiFileWrapper {
        return project.service<PsiCreator>().initFileToPsi(text)
    }
}
