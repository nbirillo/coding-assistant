package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.ml.ast.util.createFile
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import java.io.File

interface PsiFileWrapper : PsiFile {
    fun deleteFile()

    fun forceDeleteTmpData(): Boolean
}

interface PsiCreator {
    fun initFileToPsi(code: String): PsiFileWrapper
}

/*
 Create a temporary file and build PSI by this file.
 To get PSI successfully all file names have to be unique.
 */
class PsiCreatorImpl(project: Project) : PsiCreator {
    private val extension: String = ".py"
    private val tmpDataPath: String = getTmpProjectDir(toCreateFolder = false)
    private val psiManager: PsiManager = project.service()
    private var counter: Int = 0

    inner class PsiFileWrapperImpl(private val file: File, val psi: PsiFile) : PsiFile by psi, PsiFileWrapper {
        private constructor(file: File) : this(file, createPsi(file))
        constructor(code: String) : this(createFile("$tmpDataPath/tmp_${counter++}$extension", code))

        override fun deleteFile() {
            ApplicationManager.getApplication().invokeAndWait {
                ApplicationManager.getApplication().runWriteAction {
                    FileUtil.delete(file)
                }
            }
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
        }

        override fun forceDeleteTmpData(): Boolean = File(tmpDataPath).delete()
    }

    private fun createPsi(file: File): PsiFile {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            ?: error("The virtual file ${file.path} was not created")
        return ApplicationManager.getApplication().runReadAction<PsiFile> {
            psiManager.findFile(virtualFile)
        }
    }

    override fun initFileToPsi(code: String): PsiFileWrapper = PsiFileWrapperImpl(code)
}
