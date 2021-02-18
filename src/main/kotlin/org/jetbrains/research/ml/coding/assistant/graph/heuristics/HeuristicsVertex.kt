package org.jetbrains.research.ml.coding.assistant.graph.heuristics

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution

data class HeuristicsVertex(
    val psiNodesCount: Int,
    val intermediateSolutions: List<IntermediateSolution>,
) {
    constructor(intermediateSolution: IntermediateSolution) : this(
        intermediateSolution.psiFragment.nodesCount(),
        listOf(intermediateSolution)
    )

    init {
        require(intermediateSolutions.isNotEmpty()) { "Associated solutions set cannot be empty." }
    }

    fun containsAll(solutions: List<IntermediateSolution>): Boolean {
        return intermediateSolutions.containsAll(solutions)
    }

    val representativeSolution: IntermediateSolution get() = intermediateSolutions.first()

    val isFinal: Boolean = intermediateSolutions.firstOrNull { it.isFinal } != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeuristicsVertex

        if (intermediateSolutions != other.intermediateSolutions) return false

        return true
    }

    override fun hashCode(): Int {
        return intermediateSolutions.hashCode()
    }

    override fun toString(): String = intermediateSolutions.joinToString { it.toString() }
}

private fun PsiElement.nodesCount(): Int {
    var counter = 0
    ApplicationManager.getApplication().invokeAndWait {
        accept(
            object : PyRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    counter++
                    super.visitElement(element)
                }
            }
        )
    }
    return counter
}
