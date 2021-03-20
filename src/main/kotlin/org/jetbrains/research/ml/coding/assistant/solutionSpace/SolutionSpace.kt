package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.gumtree.tree.Numbering
import org.jetbrains.research.ml.ast.gumtree.tree.PreOrderNumbering
import org.jetbrains.research.ml.ast.gumtree.tree.PsiTreeConverter
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SolutionSpaceEdgeModel
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.addVertices
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.EdgeWeightCalculatorFactory
import org.jgrapht.Graph
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.SimpleDirectedWeightedGraph

/**
 * Solution space structure.
 * This structure designed to be immutable.
 * Vertices are identifiable with student's code and meta information.
 * Edges store list of edits to transform source vertex code into target vertex code.
 * EdgeWeightCalculatorFactory is a factory to calculate edge's weight based on its stored information.
 * @property graph inner solution space's graph
 */
class SolutionSpace(val graph: Graph<SolutionSpaceVertex, SolutionSpaceEdge>) {
    internal constructor(
        weightFactory: EdgeWeightCalculatorFactory<SolutionSpaceVertex, SolutionSpaceEdge>,
        graphBuilder: SolutionSpaceGraphBuilder
    ) : this(
        transferGraph(weightFactory, graphBuilder)
    )

    internal constructor(
        vertices: Collection<SolutionSpaceVertex>,
        edgePairs: Collection<SolutionSpaceEdgeModel>
    ) : this(buildGraph(vertices, edgePairs))
}

/**
 * Creates immutable solution space inner graph for `builder` with weights based on `weightFactory`
 */
private fun transferGraph(
    weightFactory: EdgeWeightCalculatorFactory<SolutionSpaceVertex, SolutionSpaceEdge>,
    builder: SolutionSpaceGraphBuilder
): AsUnmodifiableGraph<SolutionSpaceVertex, SolutionSpaceEdge> {
    val graph = SimpleDirectedWeightedGraph<SolutionSpaceVertex, SolutionSpaceEdge>(SolutionSpaceEdge::class.java)
    val weightCalculator = weightFactory(graph)
    val oldVertices = builder.graph.vertexSet().toList()
    val idFactory = SolutionSpaceIdentifierFactoryImpl()
    val newVertices = oldVertices.map { it.toSolutionSpaceVertex(idFactory) }
    graph.addVertices(newVertices)

    val zippedVertices = oldVertices zip newVertices
    val mapping = zippedVertices.toMap()

    for ((oldVertex, newVertex) in zippedVertices) {
        fun transferEdges(edges: Iterable<SolutionSpaceGraphEdge>, isOutgoing: Boolean) {
            for (outgoingEdge in edges) {
                val neighbour = if (isOutgoing) {
                    builder.graph.getEdgeTarget(outgoingEdge)
                } else {
                    builder.graph.getEdgeSource(outgoingEdge)
                }
                val newTarget = mapping[neighbour]!!
                val newEdge: SolutionSpaceEdge? = if (isOutgoing) {
                    graph.addEdge(newVertex, newTarget)
                } else {
                    graph.addEdge(newTarget, newVertex)
                }
                if (newEdge == null) {
                    continue
                }
                val calculatedWeight = weightCalculator.getWeight(newEdge)
                graph.setEdgeWeight(newEdge, calculatedWeight)
            }
        }

        val outgoingEdges = builder.graph.outgoingEdgesOf(oldVertex).toSet()
        transferEdges(outgoingEdges, isOutgoing = true)

        val incomingEdges = builder.graph.incomingEdgesOf(oldVertex).toSet()
        transferEdges(incomingEdges, isOutgoing = false)
    }

    return AsUnmodifiableGraph(graph)
}

/**
 * Creates immutable solution space inner graph using vertex and edge information
 */
private fun buildGraph(
    vertices: Collection<SolutionSpaceVertex>,
    edges: Collection<SolutionSpaceEdgeModel>
): AsUnmodifiableGraph<SolutionSpaceVertex, SolutionSpaceEdge> {
    val graph = SimpleDirectedWeightedGraph<SolutionSpaceVertex, SolutionSpaceEdge>(SolutionSpaceEdge::class.java)
    graph.addVertices(vertices)
    val idToVertex = vertices.map { it.id to it }.toMap()
    for ((sourceId, targetId, weight) in edges) {
        val newEdge = graph.addEdge(idToVertex[sourceId], idToVertex[targetId])
        graph.setEdgeWeight(newEdge, weight)
    }

    return AsUnmodifiableGraph(graph)
}

private fun SolutionSpaceGraphVertex.toSolutionSpaceVertex(
    idFactory: SolutionSpaceIdentifierFactoryImpl
): SolutionSpaceVertex {
    val treeContext = Util.getTreeContext(representativeSolution.psiFragment)
    return SolutionSpaceVertex(
        idFactory.uniqueIdentifier(),
        treeContext,
        codeFragment,
        partialSolutions.map { StudentInfo(it.id, it.metaInfo) }
    )
}

object Util {
    fun getTreeContext(psiFile: PsiFile, numbering: Numbering = PreOrderNumbering): TreeContext {
        return ApplicationManager.getApplication().runReadAction<TreeContext> {
            PsiTreeConverter.convertTree(psiFile, numbering)
        }
    }
}
