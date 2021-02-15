package org.jetbrains.research.ml.coding.assistant.unification.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo


data class IntermediateSolution(
    val id: String,
    // TODO: store TreeContext, equals <==> root.isIsomorphicTo
    val psiFragment: PsiFile,
    val commandsStorage: PerformedCommandStorage?,
    val metaInfo: MetaInfo
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntermediateSolution

        var isEquals = false
        ApplicationManager.getApplication().invokeAndWait {
            isEquals = psiFragment.textMatches(other.psiFragment)
        }
        if (!isEquals) return false

        return true
    }

    val isFinal: Boolean = metaInfo.testsResults == 1.0

    override fun hashCode(): Int {
        return psiFragment.text.hashCode()
    }

    override fun toString(): String {
        return "Vertex $id"
    }
}
