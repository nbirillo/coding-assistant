/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.util

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import java.util.*

fun PsiElement.preOrder(): Iterable<PsiElement> {
    return object : Iterable<PsiElement> {
        override fun iterator(): Iterator<PsiElement> {
            return object : Iterator<PsiElement> {
                val currentNodes: Stack<PsiElement> = Stack()

                init {
                    currentNodes.add(this@preOrder)
                }

                override fun hasNext(): Boolean {
                    return currentNodes.size != 0
                }

                override fun next(): PsiElement {
                    val c = currentNodes.pop()
                    currentNodes.addAll(c.children.reversed())
                    return c
                }
            }
        }
    }
}

fun PsiElement.equalTreeStructure(treeCtx: TreeContext): Boolean {
    val psiPreOrder = ApplicationManager.getApplication().runReadAction<List<PsiElement>> {
        this.preOrder().toList()
    }
    val treeCtxPreOrder = treeCtx.root.preOrder().toList()

    if (psiPreOrder.size != treeCtxPreOrder.size) {
        return false
    }
    psiPreOrder.zip(treeCtxPreOrder).forEach { (psi, tree) ->
        tree?.let {
            if (psi.elementType.toString() != treeCtx.getTypeLabel(tree.type)) {
                return false
            }
            val label = ApplicationManager.getApplication().runReadAction<String> {
                if (psi.children.isEmpty()) psi.text else ""
            }
            if (label != tree.label) {
                return false
            }
        } ?: return false
    }
    return true
}
