package org.jetbrains.research.ml.coding.assistant.hint

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
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
import org.jetbrains.research.ml.coding.assistant.utils.calculateEditActions
import org.jetbrains.research.ml.coding.assistant.utils.reformatInWriteAction
import java.nio.charset.Charset

interface HintManager {
    fun getHintedFile(datasetTask: DatasetTask, psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile?
}

class HintManagerImpl(private val hintFactory: HintFactory) : HintManager {
    override fun getHintedFile(datasetTask: DatasetTask, psiFragment: PsiFile, metaInfo: MetaInfo): PsiFile? {

        val studentPsiFile = psiFragment.reformatInWriteAction()
        println(studentPsiFile.project.name)
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
//        WriteCommandAction.runWriteCommandAction(studentPsiFile.project) {
//            studentPsiFile.applyActions(editActions, hintPsiFile)
//        }

        prettyPrint("Content Before Commit", studentPsiFile, PrintType.FILE)
        prettyPrint("Content Before Commit", studentPsiFile, PrintType.TEXT)

        commitPsiFile(studentPsiFile)

        prettyPrint("Content After Commit", studentPsiFile, PrintType.FILE)
        prettyPrint("Content After Commit", studentPsiFile, PrintType.TEXT)

        val hintedPsiFile = commandStorage.undoPerformedCommands()

        prettyPrint("Content After Undo", studentPsiFile, PrintType.TEXT)

        hintPsiFile.deleteFile()
        return hintedPsiFile as PsiFile?
    }

    private fun commitPsiFile(psiFile: PsiFile) {
        LocalFileSystem.getInstance().refreshFiles(listOf(psiFile.virtualFile))
        VfsUtil.markDirtyAndRefresh(false, true, true, psiFile.virtualFile);
        val documentManager = PsiDocumentManager.getInstance(psiFile.project)
        val document = psiFile.viewProvider.document ?: error("No document found for $psiFile")
        documentManager.commitDocument(document)
        documentManager.doPostponedOperationsAndUnblockDocument(document)
        VfsUtil.markDirtyAndRefresh(false, true, true, psiFile.virtualFile);

        LocalFileSystem.getInstance().refreshFiles(listOf(psiFile.virtualFile))
    }

    private fun commitAndReturnPsiFile(psiElement: PsiFile): PsiFile {
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

    private enum class PrintType {
        TEXT, FILE;

        fun toString(psiFile: PsiFile): String {
            return when(this) {
                TEXT -> psiFile.text
                FILE -> psiFile.containingFile.virtualFile.contentsToByteArray().toString(Charset.defaultCharset())
            }
        }
    }

    private fun prettyPrint(header: String, psiFile: PsiFile, printType: PrintType) {
        val toPrint = printType.toString(psiFile)
        println("\n# $header\n$toPrint\n")
    }
}

