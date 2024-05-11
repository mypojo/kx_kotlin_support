package net.kotlinx.domain.job


/**
 * job 업무로직이 담긴 작업
 * 만약 spring-batch 가 필요한 경우라면 SFN으로 분산처리하게 할것
 */
interface JobTasklet {
    fun doRun(job: Job)
}