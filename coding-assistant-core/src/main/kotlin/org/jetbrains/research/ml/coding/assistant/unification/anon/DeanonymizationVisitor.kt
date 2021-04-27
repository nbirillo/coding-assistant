package org.jetbrains.research.ml.coding.assistant.unification.anon

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.ml.ast.transformations.commands.ICommandPerformer


class DeanonymizationVisitor(private val project: Project, private val anonToOrigin: Map<String, String>) : PyRecursiveElementVisitor() {
    private val toAnonymize = mutableMapOf<PsiElement, String>()

    override fun visitPyElement(node: PyElement) {
        val oldName = node.name
        if (oldName != null) {
            val newName = anonToOrigin[oldName]
            if (newName != null && !toAnonymize.containsValue(newName)) {
                toAnonymize[node] = newName
            }
        }
        super.visitPyElement(node)
    }

    fun performAllRenames() {
        val renames = toAnonymize.toList().map { RenameUtil.renameElementDelayed(it.first, it.second) }
        renames.forEach { it() }
    }
}
