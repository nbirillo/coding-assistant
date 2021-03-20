package org.jetbrains.research.ml.coding.assistant.utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager

fun <T : PsiElement> T.reformatInAction(): T {
    val codeStyleManager = CodeStyleManager.getInstance(project)
    return WriteCommandAction.runWriteCommandAction<T>(project) {
        @Suppress("UNCHECKED_CAST")
        codeStyleManager.reformat(this) as T
    }
}
