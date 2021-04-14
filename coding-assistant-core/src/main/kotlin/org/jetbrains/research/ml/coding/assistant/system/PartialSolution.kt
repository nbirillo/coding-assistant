package org.jetbrains.research.ml.coding.assistant.system

import com.github.gumtreediff.tree.TreeContext
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo

/**
 * Model that represents the student's partial solution
 * It has to contain known meta information about the student (age, programming experience etc.)
 * For further calculations it has to store the gumtree tree (`treeContext`).
 */
data class PartialSolution(
    val datasetTask: DatasetTask,
    val treeContext: TreeContext,
    val psiFragment: PsiFile,
    val metaInfo: MetaInfo
)
