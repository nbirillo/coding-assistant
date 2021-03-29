package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.report.*
import org.jetbrains.research.ml.coding.assistant.solutionSpace.Util
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SerializationUtils
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.utils.ProjectUtils
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

object HintGenerateRunner : ApplicationStarter {
    private lateinit var solutionSpaceFile: File
    private lateinit var codeRepositoryFile: File
    private lateinit var outputDir: File
    private lateinit var datasetTask: DatasetTask

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "hint-generation"

    class TransformationsRunnerArgs(parser: ArgParser) {
        val taskName by parser.storing(
            "-t",
            "--task_name",
            help = "Name of the task"
        )

        val solutionSpacePath: Path by parser.storing(
            "-s",
            "--space_path",
            help = "Path to serialized solution space"
        ) { Paths.get(this) }

        val codeRepositoryPath: Path by parser.storing(
            "-c",
            "--code_repository_path",
            help = "Path to serialized"
        ) { Paths.get(this) }

        val outputPath: Path by parser.storing(
            "-o",
            "--output_path",
            help = "Output directory"
        ) { Paths.get(this) }
    }

    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                this@HintGenerateRunner.solutionSpaceFile = solutionSpacePath.toFile()
                this@HintGenerateRunner.codeRepositoryFile = codeRepositoryPath.toFile()
                this@HintGenerateRunner.outputDir = outputPath.toFile().apply { mkdirs() }
                this@HintGenerateRunner.datasetTask = DatasetTask.createFromString(taskName)
            }

            val project = ProjectUtils.setUpProjectWithSdk(getTmpProjectDir())

            val codeRepository = createCodeRepository(codeRepositoryFile)
            val solutionSpace = SerializationUtils.decodeSolutionSpace(solutionSpaceFile.readText())

            val code = """print(max(input()))""".trimIndent()
            val psiCreator = project.service<PsiCreator>()
            val file = psiCreator.initFileToPsi(code)
            val partialSolution = PartialSolution(
                Util.getTreeContext(file),
                code,
                MetaInfo(123.0f, null, 0.32, datasetTask)
            )

            val reportGenerator = CompositeMarkdownHintReportGenerator(MarkdownHintReportGenerator(codeRepository))
            val reportFile = outputDir.resolve("${datasetTask.taskName}_report.md").apply { createNewFile() }

            val reportFactory = HintReportFactory(
                datasetTask,
                listOf(ParallelVertexFinder(EditPartialSolutionMatcher))
            )
            val hintReports = reportFactory.createHintReports(solutionSpace, partialSolution)
            reportFile.outputStream().use { stream ->
                reportGenerator.generate(stream, hintReports)
            }
            file.deleteFile()
        } catch (ex: Exception) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }

    private fun createCodeRepository(file: File): CodeRepository {
        return CodeRepositoryImpl(SerializationUtils.decodeCodeData(file.readText()))
    }
}
