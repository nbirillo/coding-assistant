package org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization

import com.jetbrains.python.PythonFileType
import kotlinx.serialization.json.Json
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.solutionSpace.Util
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class SolutionSpaceSerializerTest : ParametrizedBaseWithSdkTest(getTmpProjectDir(true)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @Test
    fun testSerial() {
        val psiFile = myFixture.configureByText(PythonFileType.INSTANCE, inFile!!.readText())
        val treeContext = Util.getTreeContext(psiFile)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(TreeContextSerializer, treeContext)
        val decodedContext = json.decodeFromString(TreeContextSerializer, jsonString)
        assertEquals(treeContext.toString(), decodedContext.toString())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInArray(::SolutionSpaceSerializerTest)
    }
}
