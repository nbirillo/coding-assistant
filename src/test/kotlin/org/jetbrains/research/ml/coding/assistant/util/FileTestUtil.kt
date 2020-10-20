/*
 * Copyright (c) 2020.  Anastasiia Birillo
 */

package org.jetbrains.research.ml.coding.assistant.util

import java.io.File

object FileTestUtil {
    fun getNestedFiles(directoryName: String, files: MutableList<File> = ArrayList()): Sequence<File> {
        val root = File(directoryName)
        root.listFiles()?.forEach {
            if (it.isFile) {
                files.add(it)
            } else if (it.isDirectory) {
                getNestedFiles(it.absolutePath, files)
            }
        }
        return files.asSequence()
    }
}
