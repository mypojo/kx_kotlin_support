package net.kotlinx.domain.batchTask

import org.koin.core.module.Module
import org.koin.core.qualifier.named

/** BatchTask 등록 (늦은 초기화함) */
fun Module.registBatchTask(id: String, block: BatchTask.() -> Unit) {
    single(named(id)) {
        BatchTask().apply(block).apply {
            this.id = id
        }
    }
}