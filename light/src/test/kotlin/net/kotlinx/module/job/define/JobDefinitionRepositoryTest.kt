package net.kotlinx.module.job.define

import net.kotlinx.core.test.TestRoot
import net.kotlinx.reflect.Bean
import org.junit.jupiter.api.Test

class JobDefinitionRepositoryTest : TestRoot() {

    @Test
    fun test() {
        val findById = MyJobDef.JOBS.findById("notionDatabaseToGoogleCalendarJob")
        Bean(findById).toTextGrid().print()

    }

}