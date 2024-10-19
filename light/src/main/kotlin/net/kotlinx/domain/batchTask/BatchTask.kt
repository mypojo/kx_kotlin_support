package net.kotlinx.domain.batchTask

import net.kotlinx.core.Kdsl
import org.koin.core.module.Module
import org.koin.core.qualifier.named


/** 배치 작업 */
class BatchTask {

    @Kdsl
    constructor(block: BatchTask.() -> Unit = {}) {
        apply(block)
    }

    /** 유니크한 ID */
    lateinit var id: String

    /** 간단 이름 */
    lateinit var name: String

    /** 설명 */
    var desc: List<String> = emptyList()

    /** 실행기 */
    lateinit var runtime: BatchTaskRuntime

}

/** BatchTask 등록 */
fun Module.registBatchTask(id: String, block: BatchTask.() -> Unit) {
    single(named(id)) {
        val batchTask = BatchTask(block) //block을 늦은 초기화함
        batchTask.id = id
        batchTask
    }
}