package org.jetbrains.research.ml.coding.assistant.graph.runner

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.research.ml.coding.assistant.util.createFile

/*
 Create a temporary file and build PSI by this file.
 To get PSI successfully all file names have to be unique.
 */
class PsiCreator(private val project: Project, private val tmpDataPath: String) {
    private val extension: String = ".py"
    private var counter: Int = 0

    // TODO: should we rename it?
    inner class FileToPsi(code: String) {
        private val file = createFile("$tmpDataPath/tmp_${counter}$extension", code)
        val psi = createPsi()

        private fun createPsi(): PsiElement {
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
                ?: error("The virtual file ${file.path} was not created")
            return ApplicationManager.getApplication().runReadAction<PsiElement> {
                PsiManager.getInstance(project).findFile(virtualFile) as PsiElement
            }
        }

        fun deleteFile() {
            ApplicationManager.getApplication().invokeAndWait {
                ApplicationManager.getApplication().runWriteAction {
                    FileUtil.delete(file)
                }
            }
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
        }
    }

    fun initFileToPsi(code: String): FileToPsi = FileToPsi(code)
}
