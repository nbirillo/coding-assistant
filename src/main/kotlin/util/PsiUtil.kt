/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import java.util.*

object PsiUtil {
    private val psiId: Key<Int> = Key("ID")

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

    private fun numbering(iterable: Iterable<PsiElement>) {
        iterable.forEachIndexed { i, t ->
            t.putCopyableUserData(psiId, i)
        }
    }

    fun PsiElement.preOrderNumbering() {
        numbering(this.preOrder())
    }

    val PsiElement.label: String
        get() = ApplicationManager.getApplication().runReadAction<String> {
            if (this.children.isEmpty()) this.text else ""
        }

    val PsiElement.id: Int?
        get() = ApplicationManager.getApplication().runReadAction<Int?> {
            this.getCopyableUserData(psiId)
        }
}
