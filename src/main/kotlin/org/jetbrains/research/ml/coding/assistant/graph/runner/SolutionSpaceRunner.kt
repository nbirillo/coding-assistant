package org.jetbrains.research.ml.coding.assistant.graph.runner

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.coding.assistant.util.createFolder
import java.nio.file.Paths
import kotlin.system.exitProcess

object SolutionSpaceRunner : ApplicationStarter {
    private lateinit var inputDir: String
    private lateinit var outputDir: String

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "solution-space"

    class TransformationsRunnerArgs(parser: ArgParser) {
        val input by parser.storing(
            "-i",
            "--input_path",
            help = "Input directory with csv files"
        )

        val output by parser.storing(
            "-o",
            "--output_path",
            help = "Output directory"
        )
    }

    private fun getTmpProjectDir(): String {
        val path = "${System.getProperty("java.io.tmpdir")}/tmpProject"
        createFolder(path)
        return path
    }

    override fun main(args: Array<out String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(SolutionSpaceRunner::TransformationsRunnerArgs).run {
                inputDir = Paths.get(input).toString().removeSuffix("/")
                outputDir = Paths.get(output).toString().removeSuffix("/")
            }
            val tmpProjectDir = getTmpProjectDir()
            val project = ProjectUtil.openOrImport(tmpProjectDir, null, true)
            project?.let {
                val psiCreator = PsiCreator(project, tmpProjectDir)

                // TODO: to create psi file you should use: psiCreator.initFileToPsi(pythonCode)
                //  Don't forget call psiCreator.deleteFile() to delete new file

                // TODO: create solution space

                createFolder(outputDir)
                // TODO: save solution space
            }
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
