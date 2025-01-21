package net.kotlinx.domain.batchTask

import com.lectra.koson.Koson
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.reflect.name


/**
 * 하드코딩 방지용 유틸
 * */
object BatchTaskOptionUtil {

    /** 적용할 task id들 */
    const val BATCH_TASK_IDS: String = "BATCH_TASK_IDS"

    /**
     * 로컬 테스트시 S3LogicInput 이 필요함
     * */
    fun toS3LogicInputs(taskIds: List<String>, datas: List<List<String>>, block: Koson.() -> Unit = {}): List<S3LogicInput> {
        val inputOption = obj {
            BATCH_TASK_IDS to arr[taskIds]
            block(this)  //키값 말고 기타 옵션 넣기
        }.toString()
        val inputs = datas.map {
            S3LogicInput(BatchTaskExecutor::class.name(), it, inputOption)
        }
        return inputs
    }

}