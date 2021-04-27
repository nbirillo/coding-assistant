package org.jetbrains.research.ml.coding.assistant.problems.anonymization

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation.forwardApply
import org.jetbrains.research.ml.coding.assistant.unification.anon.AnonymizationTransformation
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.test.assertNotEquals

/**
 * There are some issue with a standalone PsiFile.
 * To anonymize the code fragment for every symbol in fragment Intellij has to calculate it's usages.
 * But without the physical file on a disk inside a project directory it cannot resolve references properly.
 */
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
        val anon = AnonymizationTransformation()
        ApplicationManager.getApplication().invokeAndWait {
            anon.forwardApply(psiFile, null)
        }
        assertNotEquals(outFile!!.readText(), psiFile.text)
        ApplicationManager.getApplication().invokeAndWait {
            anon.undo(psiFile)
        }
        assertEquals(inText, psiFile.text)
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
