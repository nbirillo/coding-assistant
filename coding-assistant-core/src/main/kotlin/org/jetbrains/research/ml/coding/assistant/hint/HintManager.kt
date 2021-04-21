package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.impl.PythonLanguageLevelPusher
import org.jetbrains.research.ml.ast.transformations.commands.CommandPerformer
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.utils.Util
import org.jetbrains.research.ml.coding.assistant.utils.applyActions
import org.jetbrains.research.ml.coding.assistant.utils.calculateEditActions
import org.jetbrains.research.ml.coding.assistant.utils.reformatInWriteAction

interface HintManager {
    fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile?
}

class HintManagerImpl(private val hintFactory: HintFactory) : HintManager {
    override fun getHintedFile(psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {
        val documentManager = psiFragment.project.service<PsiDocumentManager>()
        val psiFileFactory = psiFragment.project.service<PsiFileFactory>()
        val studentPsiFile = psiFragment.reformatInWriteAction()
        val commandStorage = CommandPerformer(studentPsiFile, true)

        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
            CompositeTransformation.forwardApply(studentPsiFile, commandStorage)
        }
        val partialSolution = PartialSolution(
            metaInfo.task,
            Util.getTreeContext(studentPsiFile),
            studentPsiFile,
            metaInfo
        )
        println("Student code:\n${studentPsiFile.text}\n")
        val hint = hintFactory.createHint(partialSolution) ?: return null
        val studentTreeContext = Util.getTreeContext(studentPsiFile)
        val hintPsiFile = psiFileFactory.createFileFromText(PythonLanguage.getInstance(), hint.hintVertex.code)
        Util.number(hintPsiFile, hint.hintVertex.fragment)
        val editActions = studentTreeContext.calculateEditActions(hint.hintVertex.fragment)
        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
            studentPsiFile.applyActions(editActions, hintPsiFile)
        }

        val studentDocument = documentManager.getDocument(studentPsiFile) ?: return hintPsiFile
        documentManager.commitDocument(studentDocument)

        commandStorage.undoAllPerformedCommands()

        return studentPsiFile
    }
}
