/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
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

fun PsiElement.postOrder(): Iterable<PsiElement> {
    return object : Iterable<PsiElement> {
        override fun iterator(): Iterator<PsiElement> {
            return object : Iterator<PsiElement> {
                val currentNodes: Stack<PsiElement> = Stack()

                init {
                    currentNodes.add(this@postOrder)
                    addAllUntilLeftLeaf()
                }

                override fun hasNext(): Boolean {
                    return currentNodes.size != 0
                }

                override fun next(): PsiElement {
                    val c = currentNodes.pop()
                    if (hasNext() && currentNodes.peek().children.lastOrNull() != c) {
                        addAllUntilLeftLeaf()
                    }
                    return c
                }

                private fun addAllUntilLeftLeaf() {
                    var peek = currentNodes.peek()
                    while (peek.children.isNotEmpty()) {
                        currentNodes.addAll(peek.children.reversed())
                        peek = currentNodes.peek()
                    }
                }
            }
        }
    }
}

val PsiElement.isLeaf: Boolean
    get() = this.children.isEmpty()

// TODO: we don't store "in" keyword for loops in labels. Is it ok?
val PsiElement.intermediateElementLabel: String
    get() = ApplicationManager.getApplication().runReadAction<String> {
        when (this) {
            // TODO: is it ok that I use operator?.specialMethodName (__add__ for example) for PyBinaryExpressionImpl
            //  and operator.toString() (PY:PLUS for example) for PyPrefixExpressionImpl??
            is PyBinaryExpressionImpl -> operator?.specialMethodName ?: operator.toString()
            // Expression like -1 or not a
            is PyPrefixExpressionImpl -> operator.toString()
            // Expression like +=, -= and so on, for example a += 5
            // TODO: is it ok to get text?
            is PyAugAssignmentStatementImpl -> operation?.text ?: ""
            // TODO: is it ok to store content: f"text {1}"
            is PyFormattedStringElementImpl -> content
            is PyElementImpl -> name ?: ""
            else -> ""
        }
    }

val PsiElement.label: String
    get() = ApplicationManager.getApplication().runReadAction<String> {
        val label = if (this.isLeaf) {
            this.text
        } else {
            this.intermediateElementLabel
        }
        label
    }
