package org.jetbrains.research.ml.coding.assistant.system.finder

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import de.siegmar.fastcsv.writer.CsvWriter
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.report.CodeRepository
import org.jetbrains.research.ml.coding.assistant.report.OriginalCodeData
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceDirectoryRepository
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SerializationUtils
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiFileWrapper
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.CustomEdgeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.weightCalculator.PoissonTimeWeightCalculator
import org.jetbrains.research.ml.coding.assistant.system.PartialSolution
import org.jetbrains.research.ml.coding.assistant.system.matcher.EditPartialSolutionMatcher
import org.jetbrains.research.ml.coding.assistant.unification.CompositeTransformation
import org.jetbrains.research.ml.coding.assistant.unification.DatasetUnification
import org.jetbrains.research.ml.coding.assistant.unification.model.DatasetPartialSolution
import org.jetbrains.research.ml.coding.assistant.util.DatasetUtils
import org.jetbrains.research.ml.coding.assistant.util.ParametrizedBaseWithSdkTest
import org.jetbrains.research.ml.coding.assistant.utils.Util
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

class VertexFinderTest : ParametrizedBaseWithSdkTest(getResourcesRootPath(::VertexFinderTest)) {
    @Test
    fun testBasic() {
        val taskSolution = DatasetUtils.DATASET.tasks.first()
        val datasetUnification = project.service<DatasetUnification>()

        val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
        taskSolution.dynamicSolutions
            .map { datasetUnification.unify(it) }
            .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

        val solutionSpace = solutionSpaceBuilder.build { CustomEdgeWeightCalculator(it) }
        val finder = ParallelVertexFinder(EditPartialSolutionMatcher)

        val fragment = """
            x = input()
            """.trimIndent()
        val psiFile = createPsiFile(fragment)
        val context = Util.getTreeContext(psiFile)
        val partialSolution = PartialSolution(
            taskSolution.datasetTask,
            context,
            psiFile,
            MetaInfo(10.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.1, DatasetTask.BRACKETS)
        )
        assertNoThrowable {
            finder.findCorrespondingVertex(solutionSpace, partialSolution)
        }
        psiFile.deleteFile()
    }


    @Test
    fun testSolutionCSV() {
        val outputDir = File(
            "/Users/artembobrov/Documents/masters/ast-transform/coding-assistant/output"
        )
        for (taskSolutions in DatasetUtils.DATASET.tasks) {
            val datasetUnification = project.service<DatasetUnification>()

            val solutionSpaceBuilder = SolutionSpaceGraphBuilder()
            val unifiedSolutions = taskSolutions.dynamicSolutions
                .map { datasetUnification.unify(it) }
            unifiedSolutions
                .forEach { solutionSpaceBuilder.addDynamicSolution(it) }

            var solutionSpace = solutionSpaceBuilder.build(::PoissonTimeWeightCalculator)

            // dump debug info for report
            dumpCodeMap(outputDir, taskSolutions.datasetTask, unifiedSolutions.flatten())

            // dump solution space
            val solutionSpaceRepository = SolutionSpaceDirectoryRepository(outputDir)
            solutionSpaceRepository.storeSolutionSpace(taskSolutions.datasetTask, solutionSpace)

            val repository = DatasetUtils.DATASET_REPOSITORY
            solutionSpace = repository.fetchSolutionSpace(taskSolutions.datasetTask)

            val vertexFile = outputDir.resolve("vertex-${taskSolutions.datasetTask.taskName}.csv").apply { createNewFile() }
            val vertexWriter = CsvWriter.builder().build(vertexFile.toPath(), Charset.defaultCharset())

            vertexWriter.writeRow("ID", "Vertex", "Code", "Is Final")
            solutionSpace.graph.vertexSet()
                .forEach {
                    vertexWriter.writeRow(it.id.toString(), it.toString(), it.code, it.isFinal.toString())
                }
            vertexWriter.close()
            val edgeFile = File(
                "/Users/artembobrov/Documents/masters/ast-transform/coding-assistant/output/edge-${taskSolutions.datasetTask.taskName}.csv"
            ).apply { createNewFile() }
            val edgeWriter = CsvWriter.builder().build(edgeFile.toPath(), Charset.defaultCharset())
            edgeWriter.writeRow("Source", "Target")
            solutionSpace.graph.edgeSet()
                .forEach {
                    edgeWriter.writeRow(
                        listOf(
                            solutionSpace.graph.getEdgeSource(it).id.toString(),
                            solutionSpace.graph.getEdgeTarget(it).id.toString()
                        )
                    )
                }
            edgeWriter.close()
        }
    }

    private fun dumpCodeMap(dirFile: File, taskName: DatasetTask, unifiedSolutions: List<DatasetPartialSolution>) {
        val file = dirFile.resolve(CodeRepository.filename(taskName)).apply { createNewFile() }
        val content: OriginalCodeData = unifiedSolutions.map { it.id to it.psiFragment.text }.toMap()
        val jsonString = SerializationUtils.encodeCodeData(content)
        file.writeText(jsonString)
    }

    override fun getTestDataPath(): String = getTmpProjectDir(toCreateFolder = false)

    private fun createPsiFile(text: String): PsiFileWrapper {
        return project.service<PsiCreator>().initFileToPsi(text).apply {
            val transformation = CompositeTransformation()
            ApplicationManager.getApplication().invokeAndWait {
                transformation.forwardApply(this)
            }
        }
    }
}
