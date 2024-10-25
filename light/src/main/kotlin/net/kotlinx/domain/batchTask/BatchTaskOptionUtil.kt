package net.kotlinx.domain.batchTask

import com.lectra.koson.Koson
import com.lectra.koson.ObjectType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.reflect.name


/**
 * 배치 작업 입력
 * */
object BatchTaskOptionUtil {

    /** 적용할 task id들 */
    const val BATCH_TASK_IDS: String = "BATCH_TASK_IDS"

    /**
     * 간간한 입력 구조 생성
     * 추가 옵션이 필요할 경우 오버라이드 할것
     *  */
    fun createS3LogicInput(batchTaskIds: List<String>, datas: List<String>, block: Koson.() -> Unit = {}): S3LogicInput {
        val obj = createOption(batchTaskIds, block)
        return S3LogicInput(
            logicId = BatchTaskExecutor::class.name(),
            datas,
            obj.toString()
        )
    }

    /**
     * 배치 태스크용 입력옵션 간단 생성
     *  */
    fun createOption(batchTaskIds: List<String>, block: Koson.() -> Unit = {}): ObjectType {
        val obj = obj {
            BATCH_TASK_IDS to arr[batchTaskIds]
            block()
        }
        return obj
    }

}