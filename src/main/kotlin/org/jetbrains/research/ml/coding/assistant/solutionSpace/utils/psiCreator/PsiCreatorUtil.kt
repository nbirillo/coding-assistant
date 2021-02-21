package org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator

import org.jetbrains.research.ml.coding.assistant.util.createFolder

object PsiCreatorUtil {
    val PROJECT_DIR: String = getTmpProjectDir()

    private fun getTmpProjectDir(): String {
        val path = "${System.getProperty("java.io.tmpdir")}/tmpProject"
        createFolder(path)
        return path
    }
}
