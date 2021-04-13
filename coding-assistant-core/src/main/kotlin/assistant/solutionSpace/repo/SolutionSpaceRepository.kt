package org.jetbrains.research.ml.coding.assistant.solutionSpace.repo

import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetTask
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpace
import org.jetbrains.research.ml.coding.assistant.solutionSpace.serialization.SerializationUtils
import java.io.File

interface SolutionSpaceRepository {
    fun fetchSolutionSpace(datasetTask: DatasetTask): SolutionSpace

    fun storeSolutionSpace(datasetTask: DatasetTask, solutionSpace: SolutionSpace)
}

class SolutionSpaceDirectoryRepository(private val directory: File) : SolutionSpaceRepository {
    override fun fetchSolutionSpace(datasetTask: DatasetTask): SolutionSpace {
        val solutionSpaceFile = directory.resolve(filename(datasetTask))
        return SerializationUtils.decodeSolutionSpace(solutionSpaceFile.readText())
    }

    override fun storeSolutionSpace(datasetTask: DatasetTask, solutionSpace: SolutionSpace) {
        val solutionSpaceFile = directory.resolve(filename(datasetTask))
            .apply { createNewFile() }
        val encodedSolutionSpace = SerializationUtils.encodeSolutionSpace(solutionSpace)
        solutionSpaceFile.writeText(encodedSolutionSpace)
    }

    companion object {
        private fun filename(datasetTask: DatasetTask) = "${datasetTask.taskName}_solution_space.json"
    }
}

class SolutionSpaceFileRepository(private val solutionSpaceFiles: Map<DatasetTask, File>) : SolutionSpaceRepository {
    override fun fetchSolutionSpace(datasetTask: DatasetTask): SolutionSpace {
        val solutionSpaceFile = solutionSpaceFiles[datasetTask] ?: error("Unknown dataset task $datasetTask")
        return SerializationUtils.decodeSolutionSpace(solutionSpaceFile.readText())
    }

    override fun storeSolutionSpace(datasetTask: DatasetTask, solutionSpace: SolutionSpace) {
        val solutionSpaceFile = solutionSpaceFiles[datasetTask] ?: error("Unknown dataset task $datasetTask")
        solutionSpaceFile.createNewFile()
        val encodedSolutionSpace = SerializationUtils.encodeSolutionSpace(solutionSpace)
        solutionSpaceFile.writeText(encodedSolutionSpace)
    }
}
