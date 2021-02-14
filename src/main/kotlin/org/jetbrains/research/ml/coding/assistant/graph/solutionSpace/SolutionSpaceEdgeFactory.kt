package org.jetbrains.research.ml.coding.assistant.graph.solutionSpace

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.gumtree.diff.Matcher
import org.jetbrains.research.ml.ast.gumtree.tree.Numbering
import org.jetbrains.research.ml.ast.gumtree.tree.PostOrderNumbering
import org.jetbrains.research.ml.ast.gumtree.tree.PsiTreeConverter
import org.jetbrains.research.ml.coding.assistant.graph.getOrCreate
import org.jgrapht.EdgeFactory

class SolutionSpaceEdgeFactory(
    private val cache: TreeContextCache
) : EdgeFactory<SolutionSpaceVertex, SolutionSpaceEdge> {

    override fun createEdge(sourceVertex: SolutionSpaceVertex, targetVertex: SolutionSpaceVertex): SolutionSpaceEdge {
        val sourceTreeContext = cache.getOrCreate(sourceVertex) {
            Util.getTreeContext(sourceVertex.representativeSolution.psiFragment, PostOrderNumbering)
        }

        val targetTreeContext = cache.getOrCreate(targetVertex) {
            Util.getTreeContext(targetVertex.representativeSolution.psiFragment, PostOrderNumbering)
        }

        val matcher = Matcher(sourceTreeContext, targetTreeContext)

        return SolutionSpaceEdge(matcher.getEditActions())
    }
}


object Util {
    fun getTreeContext(psiFile: PsiFile, numbering: Numbering): TreeContext {
        return ApplicationManager.getApplication().runReadAction<TreeContext> {
            PsiTreeConverter.convertTree(psiFile, numbering)
        }
    }
}
