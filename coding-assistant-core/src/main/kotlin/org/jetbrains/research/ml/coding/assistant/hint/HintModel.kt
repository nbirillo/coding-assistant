package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution

data class VertexHint(
    val partialSolution: PartialSolution,
    val hintVertex: SolutionSpaceVertex
)

data class CodeHint(
    val vertexHint: VertexHint,
    val psiFragment: PsiElement
)
