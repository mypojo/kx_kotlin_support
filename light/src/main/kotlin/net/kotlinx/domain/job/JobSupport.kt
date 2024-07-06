package net.kotlinx.domain.job

import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint


/** 간단 출력 */
fun List<Job>.printSimple() {
    listOf("pk", "sk", "상태", "시작시간", "종료시간", "memberId","instanceType","option").toTextGridPrint {
        this.map {
            arrayOf(it.pk, it.sk, it.jobStatus, it.startTime, it.endTime, it.memberId,it.instanceMetadata?.instanceType,it.jobOption?.abbr(100))
        }
    }
}
