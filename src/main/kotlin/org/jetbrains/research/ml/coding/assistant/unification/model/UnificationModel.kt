package org.jetbrains.research.ml.coding.assistant.unification.model

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.RecordMetaInfo
import java.time.ZoneOffset

data class IntermediateSolution(
    val taskSolution: PsiElement,
    val commandsStorage: PerformedCommandStorage,
    val metaInfo: RecordMetaInfo
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntermediateSolution

        if (!taskSolution.textMatches(other.taskSolution)) return false

        return true
    }

    override fun hashCode(): Int {
        return taskSolution.hashCode()
    }

    private val label = metaInfo.date?.toInstant(ZoneOffset.UTC)?.epochSecond.toString()
    override fun toString(): String {
        return "$label-${metaInfo.timestamp}"
    }
}

data class DynamicSolution(
    val solutions: List<IntermediateSolution>
)
