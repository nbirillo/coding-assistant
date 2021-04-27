package org.jetbrains.research.ml.coding.assistant.unification.anon

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyRecursiveElementVisitor

class AnonymizationVisitor(file: PyFile) : PyRecursiveElementVisitor() {
    private val project = file.project
    private val anonymizer = ElementAnonymizer()

    override fun visitElement(element: PsiElement) {
        anonymizer.registerElement(element)
        super.visitElement(element)
    }

    fun getAllRenames(): Map<String, String> = anonymizer.getAllRenames().associate { it.first.text to it.second }

    fun performAllRenames() {
        val renames = anonymizer.getAllRenames().map { RenameUtil.renameElementDelayed(it.first, it.second) }
        renames.forEach { it() }
    }
}

