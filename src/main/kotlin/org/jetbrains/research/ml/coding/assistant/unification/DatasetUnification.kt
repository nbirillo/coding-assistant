package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskDynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import java.io.File
import java.util.logging.Logger

fun createFile(path: String, content: String = ""): File {
    val file = File(path)
    if (!file.exists()) {
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                FileUtil.createIfDoesntExist(file)
                file.writeText(content)
            }
        }
    }
    return file
}

fun createFolder(path: String) {
    val file = File(path)
    if (file.exists() && file.isFile) {
        file.delete()
    }
    if (!file.exists()) {
        file.mkdirs()
    }
}

fun getTmpProjectDir(): String {
    val path = "${System.getProperty("java.io.tmpdir")}/tmpProject"
    createFolder(path)
    return path
}

class DatasetUnification(project: Project) {
    private val LOG = Logger.getLogger(javaClass.name)
    private val fileFactory = PsiFileFactory.getInstance(project)
    private val psiManager = PsiManager.getInstance(project)
    fun transform(dynamic: TaskDynamicSolution): DynamicSolution {
        val intermediateSolutions = dynamic.records.map { record ->
            val psiFile = ApplicationManager.getApplication().runReadAction<PsiFile> {
                fileFactory.createFileFromText(PythonLanguage.getInstance(), record.fragment)
            }
            val commandStorage = PerformedCommandStorage(psiFile)
            ApplicationManager.getApplication().invokeAndWait {
                LOG.info { "Unification Started: ${psiFile.text}" }
                CompositeTransformation.forwardApply(psiFile, commandStorage)
                LOG.info { "Unification Ended: ${psiFile.text}" }
            }
            IntermediateSolution(psiFile, commandStorage, record.metaInfo)
        }

        return DynamicSolution(intermediateSolutions)
    }
}
