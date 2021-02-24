package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.psi.PsiElement
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.ast.transformations.Transformation
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.ast.transformations.augmentedAssignment.AugmentedAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.commentsRemoval.CommentsRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.comparisonUnification.ComparisonUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.constantfolding.ConstantFoldingTransformation
import org.jetbrains.research.ml.ast.transformations.deadcode.DeadCodeRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.if_redundant_lines_removal.IfRedundantLinesRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.multipleOperatorComparison.MultipleOperatorComparisonTransformation
import org.jetbrains.research.ml.ast.transformations.multipleTargetAssignment.MultipleTargetAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.outerNotElimination.OuterNotEliminationTransformation
import java.util.logging.Logger

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
        ComparisonUnificationTransformation,
        OuterNotEliminationTransformation
    )

    override fun forwardApply(psiTree: PsiElement, commandsStorage: PerformedCommandStorage?) {
        LOG.info { "Tree Started: ${psiTree.text}" }

        var iterationNumber = 0
        loop@ do {
            ++iterationNumber
            val previousTree = psiTree.copy()
            try {
                transformations.forEach {
                    LOG.info { "Transformation Started: ${it.key}" }
                    try {
                        it.forwardApply(psiTree, commandsStorage)
                    } catch (e: Throwable) {
                        throw e
                    }
                    LOG.info { "Transformation Ended: ${it.key}" }
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

            LOG.info { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
            LOG.info { "Current text[$iterationNumber]:\n${psiTree.text}\n\n" }
        } while (!previousTree.textMatches(psiTree.text))
        LOG.info { "Tree Ended[[$iterationNumber]]: ${psiTree.text}\n\n\n" }
    }
}