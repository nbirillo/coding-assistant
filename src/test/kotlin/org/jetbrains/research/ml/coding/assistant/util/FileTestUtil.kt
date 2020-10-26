/*
 * Copyright (c) 2020.  Anastasiia Birillo, Elena Lyulina
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

    fun getInAndOutFilesMap(folder: String): Map<File, File> {
        val inFileRegEx = "in_\\d*.py".toRegex()
        val inOutFileRegEx = "(in|out)_\\d*.(py|xml)".toRegex()
        val (inFiles, outFiles) = getNestedFiles(folder)
            .filter { it.isFile && inOutFileRegEx.containsMatchIn(it.name) }
            .partition { inFileRegEx.containsMatchIn(it.name) }
        if (inFiles.size != outFiles.size) {
            throw IllegalArgumentException(
                "Size of the list of \"in\" files does not equal to size of the list of \"out\" files if in the " +
                    "folder: $folder"
            )
        }
        return inFiles.associateWith { inFile ->
            // TODO: can I do it better?
            val outFileName = inFile.name.replace("in", "out").replace("py", "xml")
            val outFile = File("${inFile.parent}/$outFileName")
            if (!outFile.exists()) {
                throw IllegalArgumentException("Out file $outFile does not exist!")
            }
            outFile
        }
    }

    val File.content: String
        get() = this.readText().removeSuffix("\n")
}
