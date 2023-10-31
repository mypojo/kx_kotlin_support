package net.kotlinx.module.job.define

import net.kotlinx.reflect.Bean
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class JobDefinitionRepositoryTest : TestLight() {

    @Test
    fun test() {
        val findById = JobDefinitionUtil.findById("notionDatabaseToGoogleCalendarJob")
        Bean(findById).toTextGrid().print()

    }

}