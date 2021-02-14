package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetRecord
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskSolutions
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

class DatasetUnification(private val project: Project) {
    private val logger = Logger.getLogger(javaClass.name)
    private val fileFactory = PsiFileFactory.getInstance(project)
    private val codeStyleManager = CodeStyleManager.getInstance(project)

    fun transform(taskSolutions: TaskSolutions): List<IntermediateSolution> {
        val datasetRecords = taskSolutions.dynamicSolutions.flatMap { it.records }


        val counter = AtomicInteger(0)
        return datasetRecords
            .shuffled()
            .take(20)
            .map {
                val counterValue = counter.incrementAndGet()
                logger.info { "Start unify $counterValue/${datasetRecords.size}" }
                unifyRecord(it)
            }
            .toList()
    }

    private fun unifyRecord(datasetRecord: DatasetRecord): IntermediateSolution {
        val psiFile = ApplicationManager.getApplication().runReadAction<PsiFile> {
            fileFactory.createFileFromText(PythonLanguage.getInstance(), datasetRecord.fragment)
        }
        WriteCommandAction.runWriteCommandAction(project) { // reformat the expected file
            codeStyleManager.reformat(psiFile)
        }
        val commandStorage = PerformedCommandStorage(psiFile)

        ApplicationManager.getApplication().invokeAndWait {
            logger.info { "Unification Started: ${psiFile.text}" }
            CompositeTransformation.forwardApply(psiFile, commandStorage)
            logger.info { "Unification Ended: ${psiFile.text}" }
        }

        return IntermediateSolution(datasetRecord.id, psiFile, commandStorage, datasetRecord.metaInfo)
    }
}
