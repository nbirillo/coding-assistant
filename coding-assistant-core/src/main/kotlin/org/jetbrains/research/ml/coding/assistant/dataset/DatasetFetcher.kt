package org.jetbrains.research.ml.coding.assistant.dataset

import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskSolutions
import java.io.File

/**
 * Load dataset model from file
 *
 * CSV file has to have at least "id", "task", "fragment", "testsResults" columns
 * Optional columns: "age", "programExperience"
 */
interface DatasetFetcher {
    /**
     * Fetch the whole dataset.
     */
    fun fetchDataset(file: File): Dataset

    /**
     * Fetch data for the one task.
     */
    fun fetchTaskSolutions(file: File): TaskSolutions
}
