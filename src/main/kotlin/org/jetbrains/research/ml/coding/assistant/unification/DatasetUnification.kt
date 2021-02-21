package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.components.Service
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicIntermediateSolution

@Service
interface DatasetUnification {
    fun transform(dynamicSolution: DynamicSolution): DynamicIntermediateSolution
}
