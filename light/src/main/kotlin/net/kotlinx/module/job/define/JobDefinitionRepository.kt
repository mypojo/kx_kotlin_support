package net.kotlinx.module.job.define

import net.kotlinx.core.collection.addAndGet


/** 잡 정의한 내용을 보관 */
class JobDefinitionRepository() {

    private val jobDefs: MutableList<JobDefinition> = mutableListOf()

    /** 최초 호출되는순간 jobMap 이 정의됨 */
    private val jobMap: Map<String, JobDefinition> by lazy { jobDefs.associateBy { it.jobPk } }

    /** 잡 등록 */
    fun reg(block: JobDefinition.() -> Unit): JobDefinition = jobDefs.addAndGet { JobDefinition(block) }

    /** 잡 조회 */
    fun findById(pk: String): JobDefinition = jobMap[pk] ?: throw IllegalStateException("JobDefinition $pk 를 찾을 수 없습니다.")


}
