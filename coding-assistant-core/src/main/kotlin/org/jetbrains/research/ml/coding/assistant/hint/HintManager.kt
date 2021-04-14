package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
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
        val commandStorage = PerformedCommandStorage(studentPsiFile)

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
        val psiCreator = studentPsiFile.project.service<PsiCreator>()
        return psiCreator.initFileToPsi(hint.hintVertex.code)
    }
}
