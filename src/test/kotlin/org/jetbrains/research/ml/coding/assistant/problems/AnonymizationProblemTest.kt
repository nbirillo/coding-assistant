package org.jetbrains.research.ml.coding.assistant.problems

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class AnonymizationProblemTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::AnonymizationProblemTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    @Test
    fun testAnonymizationError() {
        val inText = inFile!!.readText()
        val psiFile = createPsiFile(inText)
        ApplicationManager.getApplication().invokeAndWait {
            AnonymizationTransformation.forwardApply(psiFile, null)
        }
        assertEquals(outFile!!.readText(), psiFile.text)
    }

    private fun createPsiFile(text: String): PsiFile {
        return project.service<PsiCreator>().initFileToPsi(text)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData(): List<Array<File>> {
            return getInAndOutArray(::AnonymizationProblemTest)
        }
    }
}
