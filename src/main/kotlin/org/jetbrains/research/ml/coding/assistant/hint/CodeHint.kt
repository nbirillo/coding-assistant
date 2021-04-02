package org.jetbrains.research.ml.coding.assistant.hint

import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

data class CodeHint(
    val partialSolution: PartialSolution,
    val hintCode: String
)
