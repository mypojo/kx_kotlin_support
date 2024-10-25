package net.kotlinx.domain.batchTask

import net.kotlinx.domain.batchStep.BatchStepExecutor
import net.kotlinx.domain.batchStep.BatchStepOption
import net.kotlinx.reflect.name
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

/**
 * BatchStep 에서 BatchTask 간단 실행
 * @param sk  각 작업간의 구분자로 사용됨. ex) xxxJob-2022-11-step05
 *  */
suspend fun BatchStepExecutor.startExecutionBatchTask(sk: String, batchTaskIds: List<String>, datas: List<List<String>>, block: BatchStepOption.() -> Unit = {}) {
    val logicOption = BatchTaskOptionUtil.createOption(batchTaskIds) {}
    this.startExecution(
        pk = BatchTaskExecutor::class.name(),
        sk = sk,
        splitedDatas = datas,
        inputOption = logicOption,
        block,
    )
}