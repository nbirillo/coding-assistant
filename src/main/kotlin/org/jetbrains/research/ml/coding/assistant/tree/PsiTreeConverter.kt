/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.tree

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import com.github.gumtreediff.tree.TreeUtils
import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.coding.assistant.util.getLabel
import java.util.*

object PsiTreeConverter {
    /**
     * Convert PSI to GumTree, storing already converted GumTree parent nodes and corresponding PSI child nodes:
     *       PSI:              GumTree:
     *  | children of A |       | A |
     *  | children of B |       | B |
     *  | children of C |       | C |
     *        ....               ...
     *  | children of Z |       | Z |
     *
     *  On each iteration children from PSI are converted to GumTree format and added to GumTree parents one by one;
     *  their PSI children are added to the corresponding place in the PSI children collection.
     */
    fun convertTree(psiRoot: PsiElement, numbering: Numbering? = null): TreeContext {
        val context = TreeContext()
        context.root = context.createTree(psiRoot)
        val gumTreeParents: Queue<ITree> = LinkedList(listOf(context.root))
        val psiChildren: Queue<List<PsiElement>> = LinkedList(listOf(psiRoot.children.toList()))

        while (psiChildren.isNotEmpty()) {
            val parent = gumTreeParents.poll()
            psiChildren.poll().forEach {
                val tree = context.createTree(it)
                tree.setParentAndUpdateChildren(parent)
                gumTreeParents.add(tree)
                psiChildren.add(it.children.toList())
            }
        }

        numbering?.number(psiRoot, context)
        return context
    }

    private fun TreeContext.preOrderValidate() {
        root.refresh()
        postOrderNumbering(root)
    }

    private fun postOrderNumbering(tree: ITree) {
        TreeUtils.numbering(tree.preOrder())
    }
    // Create GumTree tree
    private fun TreeContext.createTree(psiTree: PsiElement): ITree {
        val typeLabel = psiTree.node.elementType.toString()
        return this.createTree(typeLabel.hashCode(), psiTree.getLabel(), typeLabel)
    }
}
