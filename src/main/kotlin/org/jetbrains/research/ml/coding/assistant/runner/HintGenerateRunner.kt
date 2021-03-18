package org.jetbrains.research.ml.coding.assistant.runner

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.report.CodeRepository
import org.jetbrains.research.ml.coding.assistant.report.CodeRepositoryImpl
import org.jetbrains.research.ml.coding.assistant.report.HintReport
import org.jetbrains.research.ml.coding.assistant.report.MarkdownHintReportGenerator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.Util
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SerializationUtils
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.finder.ParallelVertexFinder
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

object HintGenerateRunner : ApplicationStarter {
    private lateinit var solutionSpacePath: Path
    private lateinit var codeRepositoryPath: Path
    private lateinit var outputDir: Path
    private lateinit var datasetTask: DatasetTask

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "hint-generation"

    class TransformationsRunnerArgs(parser: ArgParser) {
        val taskName by parser.storing(
            "-t",
            "--task_name",
            help = "Name of the task"
        )

        val solutionSpacePath by parser.storing(
            "-s",
            "--space_path",
            help = "Path to serialized solution space"
        )

        val codeRepositoryPath by parser.storing(
            "-c",
            "--code_repository_path",
            help = "Path to serialized"
        )

        val outputDir by parser.storing(
            "-o",
            "--output_path",
            help = "Output directory"
        )
    }

    @ExperimentalTime
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                this@HintGenerateRunner.solutionSpacePath = Paths.get(solutionSpacePath)
                this@HintGenerateRunner.codeRepositoryPath = Paths.get(codeRepositoryPath)
                this@HintGenerateRunner.outputDir = Paths.get(outputDir)
                this@HintGenerateRunner.datasetTask = DatasetTask.createFromString(this.taskName)
            }

            val project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
            project?.let { p ->
                setSdkToProject(p, getTmpProjectDir(toCreateFolder = false))
                val solutionSpaceFile = solutionSpacePath.toFile()

                val codeRepository = createCodeRepository(codeRepositoryPath.toFile())
                val solutionSpace = SerializationUtils.decodeSolutionSpace(solutionSpaceFile.readText())

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
                val reportFile =
                    outputDir.toFile().resolve("${datasetTask.taskName}_report.md").apply { createNewFile() }

                val nextVertex = solutionSpace.graph
                    .outgoingEdgesOf(closestVertex)
                    .firstOrNull()
                    ?.let(solutionSpace.graph::getEdgeTarget)
                reportFile.outputStream().use { stream ->
                    reportGenerator.generate(
                        stream,
                        HintReport(
                            datasetTask,
                            "ParallelVertexFinder",
                            code,
                            solutionSpace,
                            closestVertex!!,
                            nextVertex
                        )
                    )
                }
            } ?: error("Internal error: the temp project was not created")
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
