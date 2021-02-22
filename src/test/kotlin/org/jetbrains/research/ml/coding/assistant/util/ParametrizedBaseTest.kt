/*
 * Copyright (c) 2020.  Anastasiia Birillo, Elena Lyulina
 */

package org.jetbrains.research.ml.coding.assistant.util

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.apache.log4j.PropertyConfigurator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.impl.TestPsiCreator
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KFunction

@Ignore
open class ParametrizedBaseTest(private val testDataRoot: String) : BasePlatformTestCase() {
    protected val LOG = Logger.getLogger(javaClass.name)

    // We should define the root resources folder
    override fun getTestDataPath() = testDataRoot

    companion object {
        fun getInAndOutArray(
            cls: KFunction<ParametrizedBaseTest>,
            resourcesRootName: String = resourcesRoot,
        ): List<Array<File>> {
            val inAndOutFilesMap = FileTestUtil.getInAndOutFilesMap(
                getResourcesRootPath(cls, resourcesRootName),
                outFormat = TestFileFormat("out", Extension.Py, Type.Output)
            )
            return inAndOutFilesMap.entries.map { (inFile, outFile) -> arrayOf(inFile, outFile!!) }
        }
        // We can not get the root of the class resources automatically
        private const val resourcesRoot: String = "data"

        fun getResourcesRootPath(
            cls: KFunction<ParametrizedBaseTest>,
            resourcesRootName: String = resourcesRoot
        ): String = cls.javaClass.getResource(resourcesRootName).path

        @JvmStatic
        @BeforeClass
        fun setupLog() {
            // Configure log4j
            PropertyConfigurator.configure(getResourcesRootPath(::ParametrizedBaseTest, "log4j.properties"))
        }
    }

    /*
    *  Older JUnit was calling this setUp method automatically, and newer one stopped to do that, and now requires
    *  an explicit @Before annotation.
    * */
    @Before
    open fun mySetUp() {
        super.setUp()
    }

    @After
    fun myDispose() {
        super.tearDown()
    }
}
