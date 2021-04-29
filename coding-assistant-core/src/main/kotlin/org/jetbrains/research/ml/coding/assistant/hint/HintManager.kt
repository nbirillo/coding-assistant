package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.utils.Util
import org.jetbrains.research.ml.coding.assistant.utils.reformatInWriteAction

interface HintManager {
    fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile?
}

class HintManagerImpl(private val hintFactory: HintFactory) : HintManager {
    override fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {
        val studentPsiFile = psiFragment.reformatInWriteAction()
        val transformation = CompositeTransformation()

        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
            transformation.forwardApply(studentPsiFile)
        }
        val partialSolution = PartialSolution(
            metaInfo.task,
            Util.getTreeContext(studentPsiFile),
            studentPsiFile,
            metaInfo
        )
        println("Student code:\n${studentPsiFile.text}\n")
        val hint = hintFactory.createHint(partialSolution) ?: return null
        val psiCreator = studentPsiFile.project.service<PsiCreator>()
        val hintPsiFile = updateDocument(psiFragment, hint.hintVertex.code) ?: psiCreator.initFileToPsi(hint.hintVertex.code)
        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
            transformation.inverseApply(hintPsiFile)
        }
        return hintPsiFile
    }


    private fun updateDocument(psiFragment: PsiFile, newContent: String): PsiFile? {
        val documentManager = psiFragment.project.service<PsiDocumentManager>()
        val document = documentManager.getDocument(psiFragment) ?: return null
        document.setText(newContent)
        documentManager.commitDocument(document)
        return documentManager.getPsiFile(document)
    }

}
