package org.jetbrains.research.ml.coding.assistant.problems.deadCodeRemoval

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.transformations.deadcode.DeadCodeRemovalTransformation
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.problems.anonymization.AnonymizationProblemTest
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.test.assertNotEquals

@RunWith(Parameterized::class)
class DeadCodeProblemTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::AnonymizationProblemTest)) {
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
            DeadCodeRemovalTransformation.forwardApply(psiFile)
        }
        assertNotEquals(outFile!!.readText(), psiFile.text)
        psiFile.deleteFile()
    }

    private fun createPsiFile(text: String): PsiFileWrapper {
        return project.service<PsiCreator>().initFileToPsi(text)
    }

    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData(): List<Array<File>> {
            return getInAndOutArray(::AnonymizationProblemTest)
        }
    }
}
