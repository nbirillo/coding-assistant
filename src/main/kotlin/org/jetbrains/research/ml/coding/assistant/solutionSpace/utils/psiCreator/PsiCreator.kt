package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator

interface PsiCreator {
    fun initFileToPsi(code: String): PsiFileWrapper
}
