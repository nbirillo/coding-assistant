package org.jetbrains.research.ml.coding.assistant.unification

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.ast.transformations.PerformedCommandStorage
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskDynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.unification.model.IntermediateSolution
import java.util.logging.Logger

class DatasetUnification(project: Project) {
    private val LOG = Logger.getLogger(javaClass.name)
    private val fileFactory = PsiFileFactory.getInstance(project)

    fun transform(dynamic: TaskDynamicSolution): DynamicSolution {
        val intermediateSolutions = dynamic.records.takeLast(10).map { record ->
            val psiFile = ApplicationManager.getApplication().runReadAction(Computable {
                fileFactory.createFileFromText(PythonLanguage.INSTANCE, record.fragment)
            })
            LOG.info { "Unification Started: ${psiFile.text}" }
            val commandStorage = PerformedCommandStorage(psiFile)
            ApplicationManager.getApplication().invokeAndWait {
                CompositeTransformation.forwardApply(psiFile, commandStorage)
            }
            LOG.info { "Unification Ended: ${psiFile.text}" }
            IntermediateSolution(psiFile, commandStorage, record.metaInfo)
        }

        return DynamicSolution(intermediateSolutions)
    }
}
