// ktlint-disable filename

package org.jetbrains.research.ml.coding.assistant.unification.model

import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper

typealias DynamicIntermediateSolution = List<DatasetPartialSolution>

data class DatasetPartialSolution(
    val id: String,
    val psiFragment: PsiFileWrapper,
    val metaInfo: MetaInfo
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DatasetPartialSolution

        var isEquals = false
        ApplicationManager.getApplication().invokeAndWait {
            isEquals = psiFragment.textMatches(other.psiFragment)
        }
        if (!isEquals) return false

        return true
    }

    override fun hashCode(): Int {
        return psiFragment.text.hashCode()
    }

    override fun toString(): String {
        return "Vertex $id"
    }
}
