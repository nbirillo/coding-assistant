package org.jetbrains.research.ml.coding.assistant.dataset

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetRecord
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolutionDataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolutionTaskDataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskDynamicSolution
import java.io.File
import kotlin.streams.toList

object TaskTrackerDatasetFetcher : DatasetFetcher {
    override fun fetchDataset(file: File): DynamicSolutionDataset {
        require(file.isDirectory)
        val taskDirectories: List<File> = file.listFiles()?.toList() ?: listOf()
        val taskSolutions = taskDirectories.parallelStream()
            .filter { it.isDirectory }
            .map(this::fetchTaskSolutions).toList()
        return DynamicSolutionDataset(taskSolutions)
    }


    private fun fetchTaskSolutions(file: File): DynamicSolutionTaskDataset {
        val solutions = file.listFiles()?.asList()?.parallelStream()?.map(this::fetchDynamicSolution)
        return DynamicSolutionTaskDataset(
            taskName = file.name,
            solutions?.toList() ?: listOf()
        )
    }

    private fun fetchDynamicSolution(file: File): TaskDynamicSolution {
        require(file.isFile)
        val records = csvReader().readAllWithHeader(file).map { DatasetRecord(it) }
        return TaskDynamicSolution(records)
    }
}
