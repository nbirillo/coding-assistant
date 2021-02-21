package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreatorUtil
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
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

    override fun main(args: Array<out String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(SolutionSpaceRunner::TransformationsRunnerArgs).run {
                inputDir = Paths.get(input).toString().removeSuffix("/")
                outputDir = Paths.get(output).toString().removeSuffix("/")
            }
            val project = ProjectUtil.openOrImport(PsiCreatorUtil.PROJECT_DIR, null, true)!!

            val psiCreator = project.service<PsiCreator>()

            // TODO: to create psi file you should use: psiCreator.initFileToPsi(pythonCode)
            //  Don't forget call psiCreator.deleteFile() to delete new file

            // TODO: create solution space

            val wrapper = psiCreator.initFileToPsi(
                """
        a=int(input())
        b=int(input())
        c=int(input())
        k=a
        if k<b:
            k=b
                            """.trimIndent()
            )

            println("Start: ${wrapper.text}")

            ApplicationManager.getApplication().invokeAndWait {
                CompositeTransformation.forwardApply(wrapper, null)
            }
            createFolder(outputDir)
            println(wrapper.text)
            // TODO: save solution space
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
