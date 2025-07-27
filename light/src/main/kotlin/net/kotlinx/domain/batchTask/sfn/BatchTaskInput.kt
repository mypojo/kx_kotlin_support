package net.kotlinx.domain.batchTask.sfn

import com.lectra.koson.Koson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.domain.batchTask.BatchTaskExecutor
import net.kotlinx.domain.batchTask.BatchTaskOptionUtil
import net.kotlinx.reflect.name

/** SFN 입력데이터 설정 */
data class BatchTaskInput(
    val flow: Flow<List<String>>,
    val batchTaskIds: List<String>,
    var inputOptionBlock: Koson.() -> Unit = {},
) {

    /**
     * 로컬 테스트시 S3LogicInput 이 필요함
     * 소량의 데이터를 테스트할때 사용
     * */
    suspend fun toAllS3LogicInputs(): List<S3LogicInput> {
        val inputOption = BatchTaskOptionUtil.inputOption(batchTaskIds, inputOptionBlock)
        return flow.map { S3LogicInput(BatchTaskExecutor::class.name(), it, inputOption) }.toList()
    }


}