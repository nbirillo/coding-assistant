package org.jetbrains.research.ml.coding.assistant.util

import org.jetbrains.research.ml.coding.assistant.dataset.TaskTrackerDatasetFetcher
import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceCachedRepository
import org.jetbrains.research.ml.coding.assistant.solutionSpace.repo.SolutionSpaceDirectoryRepository
import java.io.File

object DatasetUtils {
//    private const val DATASET_PATH = "src/test/resources/org/jetbrains/research/ml/coding/assistant/dataset"
    private const val DATASET_PATH = "/Users/artembobrov/Documents/masters/ast-transform/python"


    val DATASET_REPOSITORY = SolutionSpaceCachedRepository(
        SolutionSpaceDirectoryRepository(File(
            "/Users/artembobrov/Documents/masters/ast-transform/coding-assistant/output"
        ))
    )

    val DATASET: Dataset by lazy {
        TaskTrackerDatasetFetcher.fetchDataset(File(DATASET_PATH))
    }
}
