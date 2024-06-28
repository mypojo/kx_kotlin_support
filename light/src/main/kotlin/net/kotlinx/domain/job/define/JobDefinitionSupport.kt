package net.kotlinx.domain.job.define

import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint


/** 간단 출력 */
fun List<JobDefinition>.printSimple() {
    listOf("jobPk", "이름", "등록자", "설명").toTextGridPrint {
        this.map { arrayOf(it.jobPk, it.name, it.authors.firstOrNull()?.name, it.descs.joinToString(",").abbr(100)) }
    }
}