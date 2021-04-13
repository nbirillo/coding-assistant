package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.utils.Util
import org.jetbrains.research.ml.coding.assistant.utils.applyActions
import org.jetbrains.research.ml.coding.assistant.utils.calculateEditActions
import org.jetbrains.research.ml.coding.assistant.utils.reformatInWriteAction
import java.nio.charset.Charset

interface HintManager {
    fun getHintedFile(datasetTask: DatasetTask, psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile?
}

class HintManagerImpl(private val hintFactory: HintFactory) : HintManager {
    override fun getHintedFile(datasetTask: DatasetTask, psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {
        val studentPsiFile = psiFragment.reformatInWriteAction()
        val commandStorage = PerformedCommandStorage(studentPsiFile)

        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
            CompositeTransformation.forwardApply(studentPsiFile, commandStorage)
        }
        val partialSolution = PartialSolution(
            datasetTask,
            Util.getTreeContext(studentPsiFile),
            studentPsiFile,
            metaInfo
        )
        println("Student code:\n${studentPsiFile.text}\n")
        val hint = hintFactory.createHint(partialSolution) ?: return null
        val psiCreator = studentPsiFile.project.service<PsiCreator>()
        val studentTreeContext = Util.getTreeContext(studentPsiFile)
        val hintPsiFile = psiCreator.initFileToPsi(hint.hintVertex.code)
        Util.number(hintPsiFile, hint.hintVertex.fragment)
        val editActions = studentTreeContext.calculateEditActions(hint.hintVertex.fragment)
        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
            studentPsiFile.applyActions(editActions, hintPsiFile)
        }

        println(
            """
# Content Before Commit
${
                studentPsiFile.containingFile.virtualFile.contentsToByteArray().toString(
                    Charset.defaultCharset()
                )
            }

            """.trimIndent()
        )
        val newStudentFile = commitPsiFile(studentPsiFile)
        println(
            """
# Content After Commit
${
                studentPsiFile.containingFile.virtualFile.contentsToByteArray().toString(
                    Charset.defaultCharset()
                )
            }

# New file text After Commit
${newStudentFile.text}

            """.trimIndent()
        )
        val hintedPsiFile = commandStorage.undoPerformedCommands()
        println(
            """
# Content After Undo
${
                studentPsiFile.containingFile.virtualFile.contentsToByteArray().toString(
                    Charset.defaultCharset()
                )
            }

# New file text  After Undo
${newStudentFile.text}

            """.trimIndent()
        )
        hintPsiFile.deleteFile()
        return hintedPsiFile as PsiFile?
    }

    private fun commitPsiFile(psiElement: PsiFile): PsiFile {
        val documentManager = PsiDocumentManager.getInstance(psiElement.project)
        val document = documentManager.getDocument(psiElement) ?: error("")
        println("Commit document:\n\n${document.charsSequence}\n\n")
        documentManager.commitAllDocumentsUnderProgress()
        documentManager.commitDocument(document)
        documentManager.commitAllDocuments()
        documentManager.doPostponedOperationsAndUnblockDocument(document)
        val file = psiElement.virtualFile
        return PsiManager.getInstance(psiElement.project).findFile(file)!!
    }
}
