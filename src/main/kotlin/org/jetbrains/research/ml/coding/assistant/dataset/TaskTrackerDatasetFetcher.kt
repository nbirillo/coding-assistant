package org.jetbrains.research.ml.coding.assistant.dataset

import de.siegmar.fastcsv.reader.NamedCsvReader
import org.jetbrains.research.ml.coding.assistant.dataset.model.*
import org.jetbrains.research.ml.coding.assistant.utils.FileExtension
import org.jetbrains.research.ml.coding.assistant.utils.getListFiles
import org.jetbrains.research.ml.coding.assistant.utils.isTypeOf
import java.io.File
import java.nio.charset.Charset
import kotlin.streams.toList

/**
 * Loads the data produced by the TaskTracker plugin.
 */
object TaskTrackerDatasetFetcher : DatasetFetcher {
    /**
     * Fetch the whole dataset.
     * Directory Format:
     * `file`:
     *  ├── task1
     *  |    ├── dynamic_solution_1.csv
     *  |    ├── dynamic_solution_2.csv
     *  |    └── dynamic_solution_3.csv
     *  └── task2
     *       ├── dynamic_solution_1.csv
     *       └── dynamic_solution_2.csv
     */
    override fun fetchDataset(file: File): Dataset {
        require(file.isDirectory) { "Argument has to be directory with tasks" }
        val taskSolutions = file.getListFiles().parallelStream()
            .filter { it.isDirectory }
            .map(this::fetchTaskSolutions).toList()
        return Dataset(taskSolutions)
    }

    /**
     * Fetch data for the one task.
     *
     * Directory Format:
     *  task1
     *   ├── dynamic_solution_1.csv
     *   ├── dynamic_solution_2.csv
     *   └── dynamic_solution_3.csv
     */
    override fun fetchTaskSolutions(file: File): TaskSolutions {
        require(file.isDirectory) { "Argument has to be directory with solution files" }
        val solutions = file.getListFiles().parallelStream()
            .map(this::fetchDynamicSolution)
            .filter { it.hasFinalSolution() }
        return TaskSolutions(
            datasetTask = DatasetTask.createFromString(file.name),
            dynamicSolutions = solutions?.toList() ?: emptyList()
        )
    }

    /**
     * Fetch a single dynamic solution from the .csv file.
     */
    private fun fetchDynamicSolution(file: File): DynamicSolution {
        require(file.isFile && file.isTypeOf(FileExtension.CSV)) { "The file has to be csv" }
        val csvReader = NamedCsvReader.builder()
            .build(file.toPath(), Charset.defaultCharset())
        val records = csvReader.map { DatasetRecord(it.fields) }
        return DynamicSolution(records).run {
            copy(records = records.dropLastWhile { !it.metaInfo.isFinalSolution })
        }
    }
}
