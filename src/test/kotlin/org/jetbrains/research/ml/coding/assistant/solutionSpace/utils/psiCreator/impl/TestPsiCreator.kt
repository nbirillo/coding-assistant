package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.impl

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreatorUtil
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper
import org.jetbrains.research.ml.coding.assistant.util.createFile
import java.io.File

class TestPsiCreator : PsiCreator {
    var fixture: CodeInsightTestFixture? = null
    private var counter = 0

    override fun initFileToPsi(code: String): PsiFileWrapper {
        val file = createFile("${PsiCreatorUtil.PROJECT_DIR}/test_tmp_${counter++}.py", code)
        val psiFile = fixture!!.configureByFile(file.absolutePath)

        return TestPsiFileWrapper(file, psiFile)
    }

    private inner class TestPsiFileWrapper(
        private val file: File,
        private val psiFile: PsiFile
    ) : PsiFile by psiFile, PsiFileWrapper {
        override fun deleteFile() {
            file.delete()
        }

        override fun forceDeleteTmpData(): Boolean = true
    }
}
