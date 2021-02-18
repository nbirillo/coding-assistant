package org.jetbrains.research.ml.coding.assistant.compositeTransformation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.jetbrains.python.PythonLanguage
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@Ignore
@RunWith(Parameterized::class)
class CompositeTransformationTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::CompositeTransformationTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    @Test
    fun testCompositeTransformation() {
        val inText = inFile!!.readText()
        val psiFile = createPsiFile(inText)
        ApplicationManager.getApplication().invokeAndWait {
            CompositeTransformation.forwardApply(psiFile, null)
        }
        assertEquals(outFile!!.readText(), psiFile.text)
    }

    private fun createPsiFile(text: String): PsiFile {
        val factory = PsiFileFactory.getInstance(project)
        return ApplicationManager.getApplication().runReadAction<PsiFile> {
            factory.createFileFromText(PythonLanguage.getInstance(), text)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData(): List<Array<File>> {
            return getInAndOutArray(::CompositeTransformationTest)
        }
    }
}
