package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject
import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.report.CodeRepository
import org.jetbrains.research.ml.coding.assistant.report.OriginalCodeData
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SerializationUtils
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.generateImage
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.unification.model.DatasetPartialSolution
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

    override fun main(args: List<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(SolutionSpaceRunner::TransformationsRunnerArgs).run {
                inputDir = Paths.get(input).toString().removeSuffix("/")
                outputDir = Paths.get(output).toString().removeSuffix("/")
            }

            val project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
            project?.let { p ->
                setSdkToProject(p, getTmpProjectDir(toCreateFolder = false))
                val outputDirFile = File(outputDir).apply { mkdirs() }
                val inputDirFile = File(inputDir)
                val taskSolutions = TaskTrackerDatasetFetcher.fetchTaskSolutions(inputDirFile)
                val datasetUnification = p.service<DatasetUnification>()

                val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
                val unifiedSolutions = taskSolutions.dynamicSolutions
                    .map { datasetUnification.transform(it) }

                unifiedSolutions
                    .forEach { solutionSpaceBuilder.addDynamicSolution(it) }
                val solutionSpace = solutionSpaceBuilder.build { CustomEdgeWeightCalculator(it) }

                // dump debug info for report
                dumpCodeMap(outputDirFile, taskSolutions.datasetTask, unifiedSolutions.flatten())

                // dump solution space
                val solutionSpaceFile = outputDirFile.resolve(
                    "${taskSolutions.datasetTask.taskName}_solution_space.json"
                )
                    .apply { createNewFile() }
                val encodedSolutionSpace = SerializationUtils.encodeSolutionSpace(solutionSpace)
                solutionSpaceFile.writeText(encodedSolutionSpace)

                // dump solution space graph image
                val imageFile = outputDirFile
                    .resolve("${taskSolutions.datasetTask.taskName}_graph.png")
                    .apply { createNewFile() }
                val image = solutionSpace.generateImage()
                ImageIO.write(image, "PNG", imageFile)
            } ?: error("Internal error: the temp project was not created")
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
