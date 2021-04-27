package org.jetbrains.research.ml.coding.assistant.unification.anon

import com.intellij.psi.PsiElement
import com.intellij.util.containers.reverse
import com.jetbrains.python.psi.PyFile
import org.jetbrains.research.ml.ast.transformations.Transformation
import org.jetbrains.research.ml.ast.transformations.commands.ICommandPerformer

class AnonymizationTransformation : Transformation() {
    override val key: String = "Anonymization"

    lateinit var renames: Map<String, String>

    override fun forwardApply(psiTree: PsiElement, commandPerformer: ICommandPerformer) {
        val visitor = AnonymizationVisitor(psiTree.containingFile as PyFile)
        psiTree.accept(visitor)
        renames = visitor.getAllRenames()
        visitor.performAllRenames()
    }

    fun undo(psiTree: PsiElement) {
        val visitor = DeanonymizationVisitor(psiTree.project, renames.reverse())
        psiTree.accept(visitor)
        visitor.performAllRenames()
    }
}
