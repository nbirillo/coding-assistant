package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.transformations.InversableTransformation
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.ast.transformations.augmentedAssignment.AugmentedAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.commentsRemoval.CommentsRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.comparisonUnification.ComparisonUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.constantfolding.ConstantFoldingTransformation
import org.jetbrains.research.ml.ast.transformations.deadcode.DeadCodeRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.expressionUnification.ExpressionUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.ifRedundantLinesRemoval.IfRedundantLinesRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.inputDescriptionElimination.InputDescriptionEliminationTransformation
import org.jetbrains.research.ml.ast.transformations.multipleOperatorComparison.MultipleOperatorComparisonTransformation
import org.jetbrains.research.ml.ast.transformations.multipleTargetAssignment.MultipleTargetAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.outerNotElimination.OuterNotEliminationTransformation
import java.util.logging.Logger

/**
 * Transformation that run all inner transformations until nothing is changed
 */
class CompositeTransformation : InversableTransformation() {
    private val logger = Logger.getLogger(javaClass.name)

    override val key: String = "CompositeTransformation"
    private val anonymization = AnonymizationTransformation()

    private val transformations = arrayListOf(
        CommentsRemovalTransformation,
        InputDescriptionEliminationTransformation,
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

    override fun forwardApply(psiTree: PsiElement) {
        anonymization.forwardApply(psiTree)
        logger.fine { "Tree Started: ${psiTree.text}" }
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
                    logger.finer { "Transformation Started: ${it.key}" }
                    it.forwardApply(psiTree)
                    logger.finer { "Transformation Ended: ${it.key}" }
                }
            } catch (e: Throwable) {
                logger.severe {
                    """Transformation error {$e}: 
                        |Previous Code=${previousTree.text}
                        |Current Code=${psiTree.text}
                        |""".trimMargin()
                }
                break
            }

            logger.finer { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
            logger.finer { "Current text[$iterationNumber]:\n${psiTree.text}\n\n" }
        } while (iterationNumber <= MAX_ITERATION_COUNT && !previousTree.textMatches(psiTree.text))
        logger.fine { "Tree Ended[[$iterationNumber]]: ${psiTree.text}\n\n\n" }
    }

    override fun inverseApply(psiTree: PsiElement) {
        anonymization.inverseApply(psiTree)
    }

    companion object {
        private const val MAX_ITERATION_COUNT = 100
    }
}
