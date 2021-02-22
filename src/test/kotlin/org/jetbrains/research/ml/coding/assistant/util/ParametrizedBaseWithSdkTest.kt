package org.jetbrains.research.ml.coding.assistant.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.TestApplicationManager
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.PsiCreator
import org.jetbrains.research.ml.coding.assistant.solutionSpace.utils.psiCreator.impl.TestPsiCreator
import org.junit.Ignore

@Ignore
open class ParametrizedBaseWithSdkTest(testDataRoot: String) : ParametrizedBaseTest(testDataRoot) {

    /**
     * @return fixture to be used as temporary dir.
     */
    protected open fun createTempDirFixture(): TempDirTestFixture {
        return LightTempDirTestFixtureImpl(true) // "tmp://" dir by default
    }

    override fun mySetUp() {
        TestApplicationManager.getInstance()
        super.setUp()
        setupSdk()
        (project.service<PsiCreator>() as? TestPsiCreator)?.fixture = myFixture // for anonymization
    }

    private fun setupSdk() {
        val project = myFixture.project
        val projectManager = ProjectRootManager.getInstance(project)
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(createMockSdk())
    }

    private fun createMockSdk(): Sdk {
        val sdk = PythonMockSdk(testDataPath).create("3.8")
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                ProjectJdkTable.getInstance().addJdk(
                    sdk,
                    CodeInsightTestFixtureImpl(myFixture, myFixture.tempDirFixture).projectDisposable
                )
            }
        }
        return sdk
    }
}
