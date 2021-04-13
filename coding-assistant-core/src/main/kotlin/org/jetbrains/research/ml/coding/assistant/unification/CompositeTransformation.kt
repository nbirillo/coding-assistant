package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.ast.transformations.Transformation
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.ast.transformations.augmentedAssignment.AugmentedAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.commentsRemoval.CommentsRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.comparisonUnification.ComparisonUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.constantfolding.ConstantFoldingTransformation
import org.jetbrains.research.ml.ast.transformations.deadcode.DeadCodeRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.expressionUnification.ExpressionUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.ifRedundantLinesRemoval.IfRedundantLinesRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.multipleOperatorComparison.MultipleOperatorComparisonTransformation
import org.jetbrains.research.ml.ast.transformations.multipleTargetAssignment.MultipleTargetAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.outerNotElimination.OuterNotEliminationTransformation
import java.util.logging.Logger

/**
 * Transformation that run all inner transformations until nothing is changed
 */
object CompositeTransformation : Transformation() {
    private val LOG = Logger.getLogger(javaClass.name)

    override val key: String = "CompositeTransformation"
    private val transformations = arrayListOf(
        CommentsRemovalTransformation,
        AnonymizationTransformation,
        AugmentedAssignmentTransformation,
        DeadCodeRemovalTransformation,
        ConstantFoldingTransformation,
        MultipleOperatorComparisonTransformation,
        MultipleTargetAssignmentTransformation,
        IfRedundantLinesRemovalTransformation,
        ExpressionUnificationTransformation,
        ComparisonUnificationTransformation,
        OuterNotEliminationTransformation
    )

    override fun forwardApply(psiTree: PsiElement, commandsStorage: PerformedCommandStorage?) {
        LOG.fine { "Tree Started: ${psiTree.text}" }
        val psiDocumentManager = psiTree.project.service<PsiDocumentManager>()
        val document = (psiTree as? PsiFile)?.let { psiDocumentManager.getDocument(it) }
        var iterationNumber = 0
        do {
            ++iterationNumber
            val previousTree = psiTree.copy()
            try {
                transformations.forEach {
                    if (document != null) {
                        psiDocumentManager.commitDocument(document)
                    }
                    LOG.finer { "Transformation Started: ${it.key}" }
                    it.forwardApply(psiTree, commandsStorage)
                    LOG.finer { "Transformation Ended: ${it.key}" }
                }
            } catch (e: Throwable) {
                LOG.severe {
                    """Transformation error {$e}: 
                        |Previous Code=${previousTree.text}
                        |Current Code=${psiTree.text}
                        |""".trimMargin()
                }
                break
            }

            LOG.finer { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
            LOG.finer { "Current text[$iterationNumber]:\n${psiTree.text}\n\n" }
        } while (iterationNumber <= MAX_ITERATION_COUNT && !previousTree.textMatches(psiTree.text))
        LOG.fine { "Tree Ended[[$iterationNumber]]: ${psiTree.text}\n\n\n" }
    }

    private const val MAX_ITERATION_COUNT = 100
}