package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import kotlinx.serialization.json.Json
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.report.CodeRepository
import org.jetbrains.research.ml.coding.assistant.report.CodeRepositoryImpl
import org.jetbrains.research.ml.coding.assistant.report.HintReport
import org.jetbrains.research.ml.coding.assistant.report.MarkdownHintReportGenerator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.Util
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SolutionSpaceSerializer
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

object HintGenerateRunner : ApplicationStarter {
    private lateinit var inputDir: String
    private lateinit var datasetTask: DatasetTask

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "hint-generation"

    class TransformationsRunnerArgs(parser: ArgParser) {
        val taskName by parser.storing(
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

    @ExperimentalTime
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                inputDir = Paths.get(output).toString().removeSuffix("/")
                this@HintGenerateRunner.datasetTask = DatasetTask.createFromString(this.taskName)
            }

            val project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
            project?.let { p ->
                setSdkToProject(p, getTmpProjectDir(toCreateFolder = false))
                val inputDirFile = File(inputDir)
                val solutionSpaceFile = inputDirFile.resolve("${datasetTask}_solution_space.json")

                val codeRepository = createCodeRepository(inputDirFile, datasetTask)
                val solutionSpace = Json.decodeFromString(SolutionSpaceSerializer, solutionSpaceFile.readText())

                val finder = ParallelVertexFinder(EditPartialSolutionMatcher)
                val code = """
print(max(input()))
""".trimIndent()
                val psiCreator = p.service<PsiCreator>()
                val file = psiCreator.initFileToPsi(code)
                val partialSolution = PartialSolution(
                    Util.getTreeContext(file),
                    MetaInfo(123.0f, null, 0.32, datasetTask)
                )
                val closestVertex = finder.findCorrespondingVertex(solutionSpace, partialSolution)

                val reportGenerator = MarkdownHintReportGenerator(codeRepository)
                val reportFile = inputDirFile.resolve("${datasetTask.taskName}_report.md").apply { createNewFile() }

                val nextVertex = solutionSpace.graph
                    .outgoingEdgesOf(closestVertex)
                    .firstOrNull()
                    ?.let(solutionSpace.graph::getEdgeTarget)
                reportGenerator.generate(
                    reportFile,
                    HintReport(datasetTask, "ParallelVertexFinder", code, solutionSpace, closestVertex!!, nextVertex)
                )
            } ?: error("Internal error: the temp project was not created")
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }

    private fun createCodeRepository(dirFile: File, task: DatasetTask): CodeRepository {
        return CodeRepositoryImpl(dirFile.resolve(CodeRepository.filename(task)))
    }
}
