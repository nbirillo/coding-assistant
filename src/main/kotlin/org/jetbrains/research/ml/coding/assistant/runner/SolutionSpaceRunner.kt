package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.generateImage
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreatorUtil
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.util.createFolder
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO
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

            val taskSolutions = TaskTrackerDatasetFetcher.fetchTaskSolutions(File(inputDir))
            println(taskSolutions.dynamicSolutions.size)
            val datasetUnification = project.service<DatasetUnification>()

            val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
            taskSolutions.dynamicSolutions
                .map { datasetUnification.transform(it) }
                .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

            val solutionSpace = solutionSpaceBuilder.build()

            val imgFile = File("${taskSolutions.taskName}_graph_runner.png").apply { createNewFile() }
            val image = solutionSpace.generateImage()
            ImageIO.write(image, "PNG", imgFile)
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
