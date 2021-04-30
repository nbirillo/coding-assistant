package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.report.CodeRepository
import org.jetbrains.research.ml.coding.assistant.report.OriginalCodeData
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceDirectoryRepository
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SerializationUtils
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.generateImage
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.PoissonTimeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.unification.model.DatasetPartialSolution
import org.jetbrains.research.ml.coding.assistant.utils.ProjectUtils
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

object SolutionSpaceRunner : ApplicationStarter {
    private lateinit var inputDir: File
    private lateinit var outputDir: File

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "solution-space"

    class TransformationsRunnerArgs(parser: ArgParser) {
        val input by parser.storing(
            "-i",
            "--input_path",
            help = "Input directory with csv files"
        ) { File(this) }

        val output by parser.storing(
            "-o",
            "--output_path",
            help = "Output directory"
        ) { File(this) }
    }

    override fun main(args: List<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(SolutionSpaceRunner::TransformationsRunnerArgs).run {
                inputDir = input
                outputDir = output.apply { mkdirs() }
            }

            val project = ProjectUtils.setUpProjectWithSdk(getTmpProjectDir())

            val taskSolutions = TaskTrackerDatasetFetcher.fetchTaskSolutions(inputDir)
            val datasetUnification = project.service<DatasetUnification>()

            val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
            val unifiedSolutions = taskSolutions.dynamicSolutions
                .map { datasetUnification.unify(it) }

            unifiedSolutions
                .forEach { solutionSpaceBuilder.addDynamicSolution(it) }
            val solutionSpace = solutionSpaceBuilder.build(::PoissonTimeWeightCalculator)

            // dump debug info for report
            dumpCodeMap(outputDir, taskSolutions.datasetTask, unifiedSolutions.flatten())

            // dump solution space
            val solutionSpaceRepository = SolutionSpaceDirectoryRepository(outputDir)
            solutionSpaceRepository.storeSolutionSpace(taskSolutions.datasetTask, solutionSpace)

            // dump solution space graph image
            val imageFile = outputDir
                .resolve("${taskSolutions.datasetTask.taskName}_graph.png")
                .apply { createNewFile() }
            val image = solutionSpace.generateImage()
            ImageIO.write(image, "PNG", imageFile)
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }

    private fun dumpCodeMap(dirFile: File, taskName: DatasetTask, unifiedSolutions: List<DatasetPartialSolution>) {
        val file = dirFile.resolve(CodeRepository.filename(taskName)).apply { createNewFile() }
        val content: OriginalCodeData = unifiedSolutions.map { it.id to it.psiFragment.text }.toMap()
        val jsonString = SerializationUtils.encodeCodeData(content)
        file.writeText(jsonString)
    }
}
