package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.tree.TreeContext
import kotlinx.serialization.Serializable
import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.WeightedEdge
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.TreeContextSerializer
import org.jgrapht.EdgeFactory

class SolutionSpaceEdge(val actions: List<Action>) : WeightedEdge()

typealias SolutionSpaceVertexID = Int

@Serializable
data class SolutionSpaceVertex(
    val id: SolutionSpaceVertexID,
    @Serializable(with = TreeContextSerializer::class)
    val fragment: TreeContext,
    val info: List<StudentInfo>
) {
    override fun toString(): String {
        return info.joinToString { "Vertex ${it.id}" }
    }

    val studentInfo: StudentInfo get() = info.first()

    val isFinal: Boolean = info.any { it.metaInfo.isFinalSolution }
}

@Serializable
data class StudentInfo(
    val id: String,
    val metaInfo: MetaInfo
)


object SolutionSpaceEdgeFactory : EdgeFactory<SolutionSpaceVertex, SolutionSpaceEdge> {
    override fun createEdge(sourceVertex: SolutionSpaceVertex, targetVertex: SolutionSpaceVertex): SolutionSpaceEdge {
        val matcher = Matcher(sourceVertex.fragment, targetVertex.fragment)
        return SolutionSpaceEdge(matcher.getEditActions())
    }
}
