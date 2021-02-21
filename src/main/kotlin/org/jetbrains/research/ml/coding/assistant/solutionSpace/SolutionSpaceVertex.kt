package org.jetbrains.research.ml.coding.assistant.solutionSpace

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo

data class SolutionSpaceVertex(
    val fragment: TreeContext,
    val info: List<StudentInfo>
) {
    override fun toString(): String {
        return info.joinToString { "Vertex ${it.id}" }
    }

    val isFinal: Boolean = info.any { it.metaInfo.isFinalSolution }
}


data class StudentInfo(
    val id: String,
    val metaInfo: MetaInfo
)
