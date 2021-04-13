package org.jetbrains.research.ml.coding.assistant.utils

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import org.jetbrains.research.ml.ast.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject

object ProjectUtils {
    fun setUpProjectWithSdk(
        directoryPath: String,
        projectToClose: Project? = null,
        forceOpenInNewFrame: Boolean = true
    ): Project {
        val project = ProjectUtil.openOrImport(getTmpProjectDir(), projectToClose, forceOpenInNewFrame)
            ?: error("Internal error: Cannot create project in dir $directoryPath")
        setSdkToProject(project, directoryPath)
        return project
    }
}
