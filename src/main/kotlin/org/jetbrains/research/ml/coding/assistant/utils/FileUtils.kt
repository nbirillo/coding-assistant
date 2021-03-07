package org.jetbrains.research.ml.coding.assistant.utils

import java.io.File

fun File.getListFiles(): List<File> {
    return listFiles()?.toList() ?: listOf()
}
