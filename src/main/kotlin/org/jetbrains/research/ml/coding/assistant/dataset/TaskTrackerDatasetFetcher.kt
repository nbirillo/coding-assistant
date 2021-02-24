package org.jetbrains.research.ml.coding.assistant.dataset

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.DatasetRecord
import org.jetbrains.research.ml.coding.assistant.dataset.model.DynamicSolution
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskSolutions
import java.io.File
import kotlin.streams.toList

object TaskTrackerDatasetFetcher : DatasetFetcher {
    override fun fetchDataset(file: File): Dataset {
        require(file.isDirectory)
        val taskDirectories: List<File> = file.listFiles()?.toList() ?: listOf()
        val taskSolutions = taskDirectories.parallelStream()
            .filter { it.isDirectory }
            .map(this::fetchTaskSolutions).toList()
        return Dataset(taskSolutions)
    }

    fun fetchTaskSolutions(file: File): TaskSolutions {
        val files = file.listFiles()
        println("files in dir: ${files.joinToString("\n") { it.name }}")
        println()
        val solutions = files?.asList()?.parallelStream()
            ?.map(this::fetchDynamicSolution)
            ?.filter { it.hasFinalSolution() }
        return TaskSolutions(
            taskName = file.name,
            solutions?.toList() ?: listOf()
        )
    }

    private fun fetchDynamicSolution(file: File): DynamicSolution {
        require(file.isFile)
        val records = csvReader().readAllWithHeader(file).map { DatasetRecord(it) }
        return DynamicSolution(records).run {
            println("processed file: ${file.name}")
            copy(records = records.dropLastWhile { !it.metaInfo.isFinalSolution })
        }
    }
}
