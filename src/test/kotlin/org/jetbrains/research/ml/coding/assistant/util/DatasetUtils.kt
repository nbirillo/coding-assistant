package org.jetbrains.research.ml.coding.assistant.util

import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import java.io.File

object DatasetUtils {
    private const val DATASET_PATH = "src/test/resources/org/jetbrains/research/ml/coding/assistant/dataset"

    val DATASET: Dataset by lazy {
        TaskTrackerDatasetFetcher.fetchDataset(File(DATASET_PATH))
    }
}
