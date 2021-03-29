package org.jetbrains.research.ml.coding.assistant.solutionSpace

/**
 * Factory to create vertex identifier unique to factory instance
 */
interface SolutionSpaceIdentifierFactory {
    /**
     * Creates unique solution space vertex identifier
     * @return unique identifier
     */
    fun uniqueIdentifier(): SolutionSpaceVertexID
}

class SolutionSpaceIdentifierFactoryImpl : SolutionSpaceIdentifierFactory {
    private var counter = 0
    override fun uniqueIdentifier(): SolutionSpaceVertexID = counter++
}
