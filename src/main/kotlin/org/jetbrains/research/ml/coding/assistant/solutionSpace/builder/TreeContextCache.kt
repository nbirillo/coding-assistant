package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.ml.ast.gumtree.tree.PostOrderNumbering

class TreeContextCache(
    private val map: MutableMap<SolutionSpaceGraphVertex, TreeContext> = mutableMapOf()
) : Map<SolutionSpaceGraphVertex, TreeContext> by map {

    override fun get(key: SolutionSpaceGraphVertex): TreeContext {
        return map.getOrPut(key) {
            Util.getTreeContext(key.representativeSolution.psiFragment, PostOrderNumbering)
        }
    }
}
