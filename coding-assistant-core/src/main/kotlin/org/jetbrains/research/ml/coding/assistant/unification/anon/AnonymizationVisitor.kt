package org.jetbrains.research.ml.coding.assistant.unification.anon

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.ast.transformations.safePerformCommand

class AnonymizationVisitor(file: PyFile) : PyRecursiveElementVisitor() {
    private val project = file.project
    private val anonymizer = ElementAnonymizer()

    override fun visitElement(element: PsiElement) {
        anonymizer.registerElement(element)
        super.visitElement(element)
    }

    fun getAllRenames(): Map<String, String> = anonymizer.getAllRenames().associate { it.first.text to it.second }

    fun performAllRenames(commandsStorage: PerformedCommandStorage?) {
        val renames = anonymizer.getAllRenames().map { RenameUtil.renameElementDelayed(it.first, it.second) }
        WriteCommandAction.runWriteCommandAction(project) {
            renames.forEach { commandsStorage.safePerformCommand(it, "Anonymize element") }
        }
    }
}

