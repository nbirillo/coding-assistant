package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator

import com.intellij.psi.PsiFile

interface PsiFileWrapper : PsiFile {
    fun deleteFile()

    fun forceDeleteTmpData(): Boolean
}
