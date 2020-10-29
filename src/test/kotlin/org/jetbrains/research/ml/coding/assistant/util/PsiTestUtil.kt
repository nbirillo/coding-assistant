/*
 * Copyright (c) 2020.  Anastasiia Birillo, Elena Lyulina
 */

package org.jetbrains.research.ml.coding.assistant.util

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.research.ml.coding.assistant.tree.Numbering.PsiTreeUtils.Companion.id
import org.jetbrains.research.ml.coding.assistant.util.psi.label
import org.jetbrains.research.ml.coding.assistant.util.psi.preOrder

object PsiTestUtil {
    fun PsiElement.equalTreeStructure(treeCtx: TreeContext, toCompareNumbering: Boolean = true): Boolean {
        val psiPreOrder = ApplicationManager.getApplication().runReadAction<List<PsiElement>> {
            this.preOrder().toList()
        }
        val treeCtxPreOrder = treeCtx.root.preOrder().toList()

        if (psiPreOrder.size != treeCtxPreOrder.size) {
            return false
        }
        return psiPreOrder.zip(treeCtxPreOrder).all { (psi, tree) ->
            compareStructure(
                psi,
                tree,
                treeCtx,
                toCompareNumbering
            )
        }
    }

    private fun compareStructure(
        psi: PsiElement,
        tree: ITree?,
        treeCtx: TreeContext,
        toCompareNumbering: Boolean = true
    ): Boolean {
        if (tree == null) {
            return false
        }
        // Compare type
        if (psi.elementType.toString() != treeCtx.getTypeLabel(tree.type)) {
            return false
        }
        // Compare labels
        if (psi.label != tree.label) {
            return false
        }
        if (toCompareNumbering) {
            // Compare ids
            if (psi.id == null || psi.id != tree.id) {
                return false
            }
        }
        return true
    }
}
