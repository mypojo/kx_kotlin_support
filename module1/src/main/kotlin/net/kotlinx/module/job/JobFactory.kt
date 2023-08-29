package net.kotlinx.module.job

import net.kotlinx.module.job.define.JobDefinition


interface JobFactory {
    fun create(pk: String): Job
    fun find(pk: String): JobDefinition
}
