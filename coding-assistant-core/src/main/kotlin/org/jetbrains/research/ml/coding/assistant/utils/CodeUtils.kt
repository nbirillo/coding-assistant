package org.jetbrains.research.ml.coding.assistant.utils

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.ast.gumtree.diff.PsiElementTransformer
import org.jetbrains.research.ml.ast.gumtree.diff.PsiTransformation
import org.jetbrains.research.ml.ast.gumtree.tree.Numbering
import org.jetbrains.research.ml.ast.gumtree.tree.PostOrderNumbering
import org.jetbrains.research.ml.ast.gumtree.tree.PsiTreeConverter

object Util {
    fun number(psiElement: PsiElement, treeContext: TreeContext, numbering: Numbering = PostOrderNumbering) {
        numbering.number(psiElement, treeContext)
    }

    fun getTreeContext(psiFile: PsiFile, numbering: Numbering = PostOrderNumbering): TreeContext {
        return ApplicationManager.getApplication().runReadAction<TreeContext> {
            PsiTreeConverter.convertTree(psiFile, numbering)
        }
    }
}

fun PsiElement.applyActions(
    actions: List<Action>,
    dstPsi: PsiElement,
    numbering: Numbering = PostOrderNumbering,
    toIgnoreWhiteSpaces: Boolean = true
) {
    val transformation = PsiTransformation(this, dstPsi, numbering, toIgnoreWhiteSpaces)
    val transformer = PsiElementTransformer(project)
    transformer.applyActions(actions, transformation)
}

fun TreeContext.calculateEditActions(dst: TreeContext): List<Action> {
    return Matcher(this, dst).getEditActions()
}
