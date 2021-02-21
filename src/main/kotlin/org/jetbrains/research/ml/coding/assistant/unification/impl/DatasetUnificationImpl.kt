package org.jetbrains.research.ml.coding.assistant.unification.impl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetRecord
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.unification.model.DatasetPartialSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicIntermediateSolution
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

class DatasetUnificationImpl(private val project: Project) : DatasetUnification {
    private val logger = Logger.getLogger(javaClass.name)
    private val fileFactory: PsiCreator = project.service()
    private val codeStyleManager = CodeStyleManager.getInstance(project)

    override fun transform(dynamicSolution: DynamicSolution): DynamicIntermediateSolution {
        val datasetRecords = dynamicSolution.records

        val counter = AtomicInteger(0)
        return datasetRecords
            .map {
                val counterValue = counter.incrementAndGet()
                logger.info { "Start unify $counterValue/${datasetRecords.size}" }
                unifyRecord(it)
            }
            .toList()
    }

    private fun unifyRecord(datasetRecord: DatasetRecord): DatasetPartialSolution {
        val psiFile = fileFactory.initFileToPsi(datasetRecord.fragment)
        WriteCommandAction.runWriteCommandAction(project) { // reformat the expected file
            codeStyleManager.reformat(psiFile)
        }
        val commandStorage = PerformedCommandStorage(psiFile)

        ApplicationManager.getApplication().invokeAndWait {
            logger.info { "Unification Started: ${psiFile.text}" }
            CompositeTransformation.forwardApply(psiFile, commandStorage)
            logger.info { "Unification Ended: ${psiFile.text}" }
        }

        return DatasetPartialSolution(datasetRecord.id, psiFile, datasetRecord.metaInfo)
    }
}
