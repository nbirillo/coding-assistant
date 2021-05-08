package org.jetbrains.research.ml.coding.assistant.dataset.model

import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo.ProgramExperience
import org.junit.Test
import kotlin.test.assertEquals

class MetaInfoSelectionTest {
    @Test
    fun testPEFromMonths() {
        assertEquals(ProgramExperience.LESS_THAN_HALF_YEAR, ProgramExperience.createFromMonths(2))
        assertEquals(ProgramExperience.MORE_THAN_SIX, ProgramExperience.createFromMonths(80))
        assertEquals(ProgramExperience.FROM_TWO_TO_FOUR_YEARS, ProgramExperience.createFromMonths(25))
    }

    @Test
    fun testMetaInfoSelectionFindPE() {
        val metaInfos = listOf(
            MetaInfo(12.0f, MetaInfo.ProgramExperience.FROM_TWO_TO_FOUR_YEARS, 0.0, DatasetTask.BRACKETS),
            MetaInfo(65.0f, MetaInfo.ProgramExperience.MORE_THAN_SIX, 1.0, DatasetTask.BRACKETS),
        )

        val metaInfo = MetaInfo(33.0f, MetaInfo.ProgramExperience.MORE_THAN_SIX, 0.0, DatasetTask.BRACKETS)

        assertEquals(1, metaInfos.indexOfPreferredFor(metaInfo))
    }

    @Test
    fun testMetaInfoSelectionClosestPE() {
        val metaInfos = listOf(
            MetaInfo(22.0f, MetaInfo.ProgramExperience.MORE_THAN_SIX, 0.0, DatasetTask.BRACKETS),
            MetaInfo(65.0f, MetaInfo.ProgramExperience.FROM_ONE_TO_TWO_YEARS, 1.0, DatasetTask.BRACKETS),
        )

        val metaInfo = MetaInfo(33.0f, MetaInfo.ProgramExperience.FROM_FOUR_TO_SIX_YEARS, 0.0, DatasetTask.BRACKETS)

        assertEquals(0, metaInfos.indexOfPreferredFor(metaInfo))
    }

    @Test
    fun testMetaInfoSelectionEqPE() {
        val metaInfos = listOf(
            MetaInfo(40.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS),
            MetaInfo(20.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 1.0, DatasetTask.BRACKETS),
            MetaInfo(19.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS),
        )

        val metaInfo = MetaInfo(18.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS)

        assertEquals(2, metaInfos.indexOfPreferredFor(metaInfo))
    }

    @Test
    fun `test meta info eq pe and age`() {
        // search for closets test result
        val metaInfos = listOf(
            MetaInfo(40.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS),
            MetaInfo(20.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 1.0, DatasetTask.BRACKETS),
            MetaInfo(20.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS),
        )

        val metaInfo = MetaInfo(18.0f, MetaInfo.ProgramExperience.LESS_THAN_HALF_YEAR, 0.0, DatasetTask.BRACKETS)

        assertEquals(2, metaInfos.indexOfPreferredFor(metaInfo))
    }
}
