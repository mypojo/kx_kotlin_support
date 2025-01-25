package net.kotlinx.domain.batchTask.sfn

import com.lectra.koson.Koson
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.domain.batchTask.BatchTaskExecutor
import net.kotlinx.domain.batchTask.BatchTaskOptionUtil
import net.kotlinx.reflect.name

/**
 * SFN 입력데이터 (인메모리)
 * */
data class BatchTaskInputInmemery(
    val datas: List<List<String>>,
    val batchTaskIds: List<String>,
    var inputOptionBlock: Koson.() -> Unit = {}
) {

    /**
     * 로컬 테스트시 S3LogicInput 이 필요함
     * */
    fun toS3LogicInputs(): List<S3LogicInput> {
        val inputOption = BatchTaskOptionUtil.inputOption(batchTaskIds, inputOptionBlock)
        return datas.map { S3LogicInput(BatchTaskExecutor::class.name(), it, inputOption) }
    }

}