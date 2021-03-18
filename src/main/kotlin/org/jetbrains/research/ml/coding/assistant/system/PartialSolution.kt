package org.jetbrains.research.ml.coding.assistant.system

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo

/**
 * Model that represents the student's partial solution
 */
data class PartialSolution(
    val context: TreeContext,
    val metaInfo: MetaInfo
)
