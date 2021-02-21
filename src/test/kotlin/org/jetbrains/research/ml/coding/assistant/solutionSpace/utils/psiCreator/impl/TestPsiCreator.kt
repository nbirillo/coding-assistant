package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.impl

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.jetbrains.python.PythonFileType
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper


class TestPsiCreator : PsiCreator {
    var fixture: CodeInsightTestFixture? = null

    override fun initFileToPsi(code: String): PsiFileWrapper {
        val file = fixture!!.configureByText(PythonFileType.INSTANCE, code)
        return TestPsiFileWrapper(file.copy() as PsiFile)
    }

    private inner class TestPsiFileWrapper(private val file: PsiFile) : PsiFile by file, PsiFileWrapper {
        override fun deleteFile() = Unit

        override fun forceDeleteTmpData(): Boolean = true
    }
}
