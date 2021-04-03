package org.jetbrains.research.ml.coding.assistant.hint

import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceRepository
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.finder.VertexFinder
import org.jetbrains.research.ml.coding.assistant.system.hint.HintVertexCalculator

abstract class HintFactory {
    protected abstract val vertexFinder: VertexFinder
    protected abstract val hintVertexCalculator: HintVertexCalculator

    abstract fun createHint(partialSolution: PartialSolution): CodeHint?

    override fun toString(): String {
        return "${this::class.simpleName}(finder=$vertexFinder, calculator=$hintVertexCalculator)"
    }
}

class HintFactoryImpl(
    private val repository: SolutionSpaceRepository,
    override val vertexFinder: VertexFinder,
    override val hintVertexCalculator: HintVertexCalculator
) : HintFactory() {
    override fun createHint(partialSolution: PartialSolution): CodeHint? {
        val solutionSpace = repository.fetchSolutionSpace(partialSolution.datasetTask)
        val closestVertex = vertexFinder.findCorrespondingVertex(solutionSpace, partialSolution) ?: return null
        println("Closest Vertex = ${closestVertex.id}")
        val hintVertex =
            hintVertexCalculator.calculateHintVertex(solutionSpace, closestVertex, partialSolution) ?: return null
        println("Hint Vertex = ${hintVertex.id}")
        return CodeHint(partialSolution, hintVertex)
    }
}
