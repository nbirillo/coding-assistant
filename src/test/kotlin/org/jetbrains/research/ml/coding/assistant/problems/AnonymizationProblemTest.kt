package org.jetbrains.research.ml.coding.assistant.problems

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@Ignore
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
        /*
        в доке написанно
        The process of resolving references is distinct from parsing and is not performed at the same time.
        https://plugins.jetbrains.com/docs/intellij/psi-references.html#contributed-references
        хз, как сделать, чтобы работали
         */
//        return myFixture.configureByText(PythonFileType.INSTANCE, text) // так всё работает
        val factory = PsiFileFactory.getInstance(project)
        return ApplicationManager.getApplication().runReadAction<PsiFile> {
            factory.createFileFromText(PythonLanguage.getInstance(), text)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData(): List<Array<File>> {
            return getInAndOutArray(::AnonymizationProblemTest)
        }
    }
}
