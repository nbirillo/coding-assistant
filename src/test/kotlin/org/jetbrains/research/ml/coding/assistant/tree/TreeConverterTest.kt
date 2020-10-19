/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.tree

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import junit.framework.TestCase
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseTest
import org.jetbrains.research.ml.coding.assistant.util.equalTreeStructure
import org.jetbrains.research.ml.coding.assistant.util.getNestedFiles
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TreeConverterTest : ParametrizedBaseTest(getResourcesRootPath(::TreeConverterTest)) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0})")
        // TODO: which tests cases should I have?
        fun getTestData(): List<Array<File>> = getNestedFiles(getResourcesRootPath(::TreeConverterTest)).map {
            arrayOf(
                it
            )
        }.toList()
    }

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @Test
    fun test() {
        val psiInFile = myFixture.configureByFile(inFile!!.name)
        val inContext = ApplicationManager.getApplication().runReadAction<TreeContext> {
            TreeConverter.getTree(psiInFile)
        }
        TestCase.assertTrue(psiInFile.equalTreeStructure(inContext))
    }
}
