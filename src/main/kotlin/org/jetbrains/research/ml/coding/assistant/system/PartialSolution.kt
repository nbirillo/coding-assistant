package org.jetbrains.research.ml.coding.assistant.system

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo

data class PartialSolution(
    val context: TreeContext,
    val metaInfo: MetaInfo
)
