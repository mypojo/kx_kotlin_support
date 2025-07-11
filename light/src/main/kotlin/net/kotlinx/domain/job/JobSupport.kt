package net.kotlinx.domain.job

import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint


/** 간단 출력 */
fun List<Job>.printSimple() {
    listOf("pk", "sk", "상태", "시작시간", "종료시간", "memberId", "instanceType", "option").toTextGridPrint {
        this.map {
            arrayOf(it.pk, it.sk, it.jobStatus, it.startTime, it.endTime, it.memberId, it.instanceMetadata?.instanceType, it.jobOption.toString().abbr(100))
        }
    }
}

/** 잡으로 JobDef 찾기 */
fun Job.toJobDefinition(): JobDefinition = JobDefinitionRepository.findById(pk) ?: throw IllegalStateException("job ${pk} -> JobDefinition is not found")

/**
 * 새로운 잡 생성해서 일부 데이터 복사
 * ex) SQS 전송
 *  */
fun Job.copyNewJob(): Job = Job(this.pk, this.sk).apply {
    jobOption = this.jobOption
    memberId = this.memberId
    jobEnv = this.jobEnv
}
