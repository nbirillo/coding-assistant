package org.jetbrains.research.ml.coding.assistant.dataset

import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.dataset.model.TaskSolutions
import java.io.File

/**
 * Load dataset model from file
 */
interface DatasetFetcher {
    fun fetchDataset(file: File): Dataset

    fun fetchTaskSolutions(file: File): TaskSolutions
}
