package org.jetbrains.research.ml.coding.assistant.dataset

import org.jetbrains.research.ml.coding.assistant.dataset.model.Dataset
import java.io.File

interface DatasetFetcher {
    fun fetchDataset(file: File): Dataset
}