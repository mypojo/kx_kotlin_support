package net.kotlinx.domain.job.define

import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint
import org.koin.core.module.Module
import org.koin.core.qualifier.named

/** 잡 정의 등록 (초기화시 모두 생성해서 등록함) */
fun Module.registJob(block: JobDefinition.() -> Unit) {
    val defForPk = JobDefinition(block) //pk 확인을 위해서 그냥 생성한다.
    single(named(defForPk.jobPk)) { JobDefinition(block) }  //named 지정하면서 생성은 안되네.. ㅠㅠ
}

/** 간단 출력 */
fun List<JobDefinition>.printSimple() {
    listOf("jobPk", "이름", "등록자", "설명").toTextGridPrint {
        this.map { arrayOf(it.jobPk, it.name, it.authors.firstOrNull()?.name, it.descs.joinToString(",").abbr(100)) }
    }
}