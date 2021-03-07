package org.jetbrains.research.ml.coding.assistant.dataset

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.jetbrains.research.ml.coding.assistant.dataset.model.*
import org.jetbrains.research.ml.coding.assistant.utils.getListFiles
import java.io.File
import kotlin.streams.toList

object TaskTrackerDatasetFetcher : DatasetFetcher {
    override fun fetchDataset(file: File): Dataset {
        require(file.isDirectory) { "Argument has to be directory with tasks" }
        val taskSolutions = file.getListFiles().parallelStream()
            .filter { it.isDirectory }
            .map(this::fetchTaskSolutions).toList()
        return Dataset(taskSolutions)
    }

    fun fetchTaskSolutions(file: File): TaskSolutions {
        require(file.isDirectory) { "Argument has to be directory with solution files" }
        val solutions = file.getListFiles().parallelStream()
            .map(this::fetchDynamicSolution)
            .filter { it.hasFinalSolution() }
        return TaskSolutions(
            taskName = DatasetTask.createFromString(file.name),
            solutions?.toList() ?: listOf()
        )
    }

    private fun fetchDynamicSolution(file: File): DynamicSolution {
        require(file.isFile && file.extension == "csv") { "The file has to be csv" }
        val records = csvReader().readAllWithHeader(file).map { DatasetRecord(it) }
        return DynamicSolution(records).run {
            copy(records = records.dropLastWhile { !it.metaInfo.isFinalSolution })
        }
    }
}
