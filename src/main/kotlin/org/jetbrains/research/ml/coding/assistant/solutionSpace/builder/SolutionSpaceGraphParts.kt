package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.ml.coding.assistant.unification.model.DatasetPartialSolution

typealias SolutionSpaceGraphEdge = WeightedEdge

/**
 * Builder's graph vertex.
 * Able to store multiple student's code fragments.
 */
data class SolutionSpaceGraphVertex(
    val psiNodesCount: Int,
    val partialSolutions: List<DatasetPartialSolution>,
) {
    constructor(partialSolution: DatasetPartialSolution) : this(
        partialSolution.psiFragment.nodesCount(),
        listOf(partialSolution)
    )

    init {
        require(partialSolutions.isNotEmpty()) { "Associated solutions set cannot be empty." }
    }

    fun containsAll(solutions: List<DatasetPartialSolution>): Boolean {
        return partialSolutions.containsAll(solutions)
    }

    val representativeSolution: DatasetPartialSolution get() = partialSolutions.first()

    val codeFragment: String = representativeSolution.psiFragment.text

    fun merged(other: SolutionSpaceGraphVertex): SolutionSpaceGraphVertex {
        val newIntermediateSolutions = partialSolutions.plus(other.partialSolutions)
        return copy(partialSolutions = newIntermediateSolutions)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SolutionSpaceGraphVertex

        if (partialSolutions != other.partialSolutions) return false

        return true
    }

    override fun hashCode(): Int {
        return partialSolutions.hashCode()
    }

    override fun toString(): String = partialSolutions.joinToString { it.toString() }
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
