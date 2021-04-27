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
import org.jetbrains.research.ml.coding.assistant.utils.FileExtension
import java.io.File

/**
 * Wrapper for PsiFile with ability to delete physical file from the disk.
 */
interface PsiFileWrapper : PsiFile {
    /**
     * Deletes file from the disk.
     */
    fun deleteFile()

    /**
     * Deletes the directory with all the temporary files.
     */
    fun forceDeleteTmpData(): Boolean
}

/**
 * Interface to handle creation of psi file based on fragment.
 * It creates a physical file in project directory, so it is necessary to delete it after use.
 */
interface PsiCreator {
    fun initFileToPsi(code: String): PsiFileWrapper
}

/*
 Create a temporary file and build PSI by this file.
 To get PSI successfully all file names have to be unique.
 */
class PsiCreatorImpl(project: Project) : PsiCreator {
    private val extension = FileExtension.Py
    private val tmpDataPath: String = project.basePath ?: getTmpProjectDir(toCreateFolder = false)
    private val psiManager: PsiManager = project.service()
    private var counter: Int = 0

    inner class PsiFileWrapperImpl(private val file: File, private val psi: PsiFile) : PsiFile by psi, PsiFileWrapper {
        private constructor(file: File) : this(file, createPsi(file))
        constructor(code: String) : this(createFile("$tmpDataPath/tmp_${counter++}.${extension.name}", code))

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
