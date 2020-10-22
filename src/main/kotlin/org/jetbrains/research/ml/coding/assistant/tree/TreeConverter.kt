/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.tree

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import com.github.gumtreediff.tree.TreeUtils
import com.intellij.psi.PsiElement
import util.PsiUtil.label
import java.util.*

object TreeConverter {

    /*
    * Create a GumTree tree for PSI.  We use BFS to traversal the PSI. To indicate the level is ended we add null
    * into psiNodes. The algorithm works as follow:
    * Let we have a tree:
    *      [1]
    *   /   |   \
    * [2]  [3]  [4]
    *  | \
    * [5] [6]
    * ...
    * Each vertex from the list of vertex [2, 3, 4] we should link with the vertex 1.
    * After that we should do it for each level.
    * For example, for each vertex from the list [5, 6] we should link with the vertex 2.
    *
    * It means, we had the following psiNodes list: [2, 3, 4, null, 5, 6, ...]
    * and the following parentNodes list: [1, 2, ...].
    * After first null in psiNodes we will have the following psiNodes and parentNodes lists:
    * [5, 6, ...] and [2, ...]. It means for the nodes 5 and 6 the parent node is 2.
    * */
    fun convertTree(psiRoot: PsiElement): TreeContext {
        val context = TreeContext()
        val psiNodes: Queue<PsiElement?> = LinkedList(psiRoot.children.toMutableList())
        psiNodes.add(null)

        val treeRoot = context.createTree(psiRoot)
        context.root = treeRoot
        val parentNodes: Queue<ITree> = LinkedList(listOf(treeRoot))

        while (psiNodes.isNotEmpty()) {
            val currentPsi = psiNodes.poll()
            currentPsi?.let {
                val tree = context.createTree(currentPsi)
                tree.setParentAndUpdateChildren(parentNodes.peek())
                psiNodes.addAll(currentPsi.children)
                if (currentPsi.children.isNotEmpty()) {
                    psiNodes.add(null)
                    parentNodes.add(tree)
                }
            } ?: let {
                parentNodes.poll()
            }
        }
        // TODO: Is it ok to use preOrderValidate instead of context.validate() with postOder numbering?
        context.preOrderValidate()
        return context
    }

    // Create GumTree tree
    private fun TreeContext.createTree(psiTree: PsiElement): ITree {
        val typeLabel = psiTree.node.elementType.toString()
        return this.createTree(typeLabel.hashCode(), psiTree.label, typeLabel)
    }

    private fun TreeContext.preOrderValidate() {
        root.refresh()
        postOrderNumbering(root)
    }

    private fun postOrderNumbering(tree: ITree) {
        TreeUtils.numbering(tree.preOrder())
    }
}
