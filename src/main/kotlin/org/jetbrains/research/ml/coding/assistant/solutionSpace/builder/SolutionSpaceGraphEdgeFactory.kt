package org.jetbrains.research.ml.coding.assistant.solutionSpace.builder

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.ast.gumtree.tree.Numbering
import org.jetbrains.research.ml.ast.gumtree.tree.PsiTreeConverter
import org.jgrapht.EdgeFactory

class SolutionSpaceGraphEdgeFactory(
    private val cache: TreeContextCache
) : EdgeFactory<SolutionSpaceGraphVertex, SolutionSpaceGraphEdge> {

    override fun createEdge(
        sourceVertex: SolutionSpaceGraphVertex,
        targetVertex: SolutionSpaceGraphVertex
    ): SolutionSpaceGraphEdge {
        val sourceTreeContext = cache[sourceVertex]
        val targetTreeContext = cache[targetVertex]
        val matcher = Matcher(sourceTreeContext, targetTreeContext)

        return SolutionSpaceGraphEdge(matcher.getEditActions())
    }
}

object Util {
    fun getTreeContext(psiFile: PsiFile, numbering: Numbering): TreeContext {
        return ApplicationManager.getApplication().runReadAction<TreeContext> {
            PsiTreeConverter.convertTree(psiFile, numbering)
        }
    }
}
