package org.jetbrains.research.ml.coding.assistant.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.apache.log4j.PropertyConfigurator
import org.junit.BeforeClass
import org.junit.Ignore
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KFunction

@Ignore
open class ParametrizedBaseTest(private val testDataRoot: String) : BasePlatformTestCase() {
    protected val logger: Logger = Logger.getLogger(javaClass.name)

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

        fun getInArray(
            cls: KFunction<ParametrizedBaseTest>,
            resourcesRootName: String = resourcesRoot,
        ): List<Array<File>> {
            val inAndOutFilesMap = FileTestUtil.getInAndOutFilesMap(
                getResourcesRootPath(cls, resourcesRootName),
            )
            return inAndOutFilesMap.keys.map { arrayOf(it) }
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
}
