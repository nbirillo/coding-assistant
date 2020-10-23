/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.tree

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import junit.framework.TestCase
import org.jetbrains.research.ml.coding.assistant.util.FileTestUtil.getNestedFiles
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseTest
import org.jetbrains.research.ml.coding.assistant.util.PsiTestUtil.equalTreeStructure
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class PsiTreeConverterTest : ParametrizedBaseTest(getResourcesRootPath(::PsiTreeConverterTest)) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        // TODO: which tests cases should I have?
        fun getTestData(): List<Array<Any>> {
            val files = getNestedFiles(getResourcesRootPath(::PsiTreeConverterTest))
            val numberings = listOf(PreOrderNumbering, PostOrderNumbering)
            return files.flatMap { f -> numberings.map { n -> arrayOf(f, n) } }.toList()
        }
    }

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var numbering: Numbering? = null

    @Test
    fun `converting PSI to GumTree tree test`() {
        val inFilePsi = myFixture.configureByFile(inFile!!.name)
        val inContext = ApplicationManager.getApplication().runReadAction<TreeContext> {
            PsiTreeConverter.convertTree(inFilePsi, numbering!!)
        }
        TestCase.assertTrue(inFilePsi.equalTreeStructure(inContext))
    }
}
