package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetRecord
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.unification.model.DatasetPartialSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicIntermediateSolution
import org.jetbrains.research.ml.coding.assistant.utils.reformatInAction
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

/**
 * Unifies all code fragments from the dataset
 */
@Service
interface DatasetUnification {
    /**
     * Unifies the given solution dynamic
     * @param dynamicSolution raw dataset dynamic solution
     * @return unified solution using ast transformation
     */
    fun unify(dynamicSolution: DynamicSolution): DynamicIntermediateSolution
}

class DatasetUnificationImpl(project: Project) : DatasetUnification {
    private val logger = Logger.getLogger(javaClass.name)
    private val fileFactory: PsiCreator = project.service()

    override fun unify(dynamicSolution: DynamicSolution): DynamicIntermediateSolution {
        val datasetRecords = dynamicSolution.records

        val counter = AtomicInteger(0)
        return datasetRecords
            .map {
                val counterValue = counter.incrementAndGet()
                logger.finer { "Start unify(id=${it.id}) $counterValue/${datasetRecords.size}" }
                unifyRecord(it)
            }
            .toList()
    }

    private fun unifyRecord(datasetRecord: DatasetRecord): DatasetPartialSolution {
        val psiFile = fileFactory.initFileToPsi(datasetRecord.fragment).reformatInAction()

        ApplicationManager.getApplication().invokeAndWait {
            logger.finer { "Unification Started: ${psiFile.text}" }
            CompositeTransformation.forwardApply(psiFile, null)
            logger.finer { "Unification Ended: ${psiFile.text}" }
        }

        return DatasetPartialSolution(datasetRecord.id, psiFile, datasetRecord.metaInfo)
    }
}
