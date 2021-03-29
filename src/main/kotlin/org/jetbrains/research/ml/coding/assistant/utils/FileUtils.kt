package org.jetbrains.research.ml.coding.assistant.utils

import java.io.File

fun File.getListFiles(): List<File> {
    return listFiles()?.toList() ?: emptyList()
}

enum class FileExtension(val extension: String) {
    CSV("csv"),
    Py("py")
}

fun File.isTypeOf(ext: FileExtension): Boolean = extension == ext.extension
