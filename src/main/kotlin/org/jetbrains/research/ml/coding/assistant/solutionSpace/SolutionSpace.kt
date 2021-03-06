package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.gumtree.tree.Numbering
import org.jetbrains.research.ml.ast.gumtree.tree.PostOrderNumbering
import org.jetbrains.research.ml.ast.gumtree.tree.PsiTreeConverter
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.addVertices
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.EdgeWeightCalculatorFactory
import org.jgrapht.Graph
import org.jgrapht.graph.AsUnmodifiableGraph
import org.jgrapht.graph.SimpleDirectedWeightedGraph

class SolutionSpace(val graph: Graph<SolutionSpaceVertex, SolutionSpaceEdge>) {
    internal constructor(graphBuilder: SolutionSpaceGraphBuilder) : this(
        transferGraph({ CustomEdgeWeightCalculator(it) }, graphBuilder)
    )

    internal constructor(
        vertices: Collection<SolutionSpaceVertex>,
        edgePairs: Collection<Pair<SolutionSpaceVertexID, SolutionSpaceVertexID>>
    ) : this(buildGraph({ CustomEdgeWeightCalculator(it) }, vertices, edgePairs))
}

private fun transferGraph(
    weightCalculatorFactory: EdgeWeightCalculatorFactory<SolutionSpaceVertex, SolutionSpaceEdge>,
    builder: SolutionSpaceGraphBuilder
): Graph<SolutionSpaceVertex, SolutionSpaceEdge> {
    val graph = SimpleDirectedWeightedGraph(SolutionSpaceEdgeFactory)
    val weightCalculator = weightCalculatorFactory(graph)
    val oldVertices = builder.graph.vertexSet().toList()
    val newVertices = oldVertices.map { it.toSolutionSpaceVertex() }
    graph.addVertices(newVertices)

    val mapping = (oldVertices zip newVertices).toMap()

    for ((oldVertex, newVertex) in oldVertices zip newVertices) {
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
                if (newEdge == null)
                    continue
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

private fun buildGraph(
    weightCalculatorFactory: EdgeWeightCalculatorFactory<SolutionSpaceVertex, SolutionSpaceEdge>,
    vertices: Collection<SolutionSpaceVertex>,
    edgePairs: Collection<Pair<SolutionSpaceVertexID, SolutionSpaceVertexID>>
): Graph<SolutionSpaceVertex, SolutionSpaceEdge> {
    val graph = SimpleDirectedWeightedGraph(SolutionSpaceEdgeFactory)
    val weightCalculator = weightCalculatorFactory(graph)
    graph.addVertices(vertices)
    val idToVertex = vertices.map { it.id to it }.toMap()
    for ((sourceId, targetId) in edgePairs) {
        val newEdge = graph.addEdge(idToVertex[sourceId], idToVertex[targetId])
        val calculatedWeight = weightCalculator.getWeight(newEdge)
        graph.setEdgeWeight(newEdge, calculatedWeight)
    }

    return AsUnmodifiableGraph(graph)
}

private var idCounter = 0

private fun SolutionSpaceGraphVertex.toSolutionSpaceVertex(): SolutionSpaceVertex {
    val treeContext = Util.getTreeContext(representativeSolution.psiFragment)
    return SolutionSpaceVertex(
        idCounter++,
        treeContext,
        partialSolutions.map { StudentInfo(it.id, it.metaInfo) }
    )
}

object Util {
    fun getTreeContext(psiFile: PsiFile, numbering: Numbering = PostOrderNumbering): TreeContext {
        return ApplicationManager.getApplication().runReadAction<TreeContext> {
            PsiTreeConverter.convertTree(psiFile, numbering)
        }
    }
}

