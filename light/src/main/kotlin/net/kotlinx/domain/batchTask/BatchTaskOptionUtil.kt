package net.kotlinx.domain.batchTask

import com.lectra.koson.Koson
import com.lectra.koson.arr
import com.lectra.koson.obj


/**
 * 하드코딩 방지용 유틸
 * */
object BatchTaskOptionUtil {

    /** 적용할 task id들 */
    const val BATCH_TASK_IDS: String = "BATCH_TASK_IDS"

    fun inputOption(batchTaskIds: List<String>, inputOptionBlock: Koson.() -> Unit = {}): String {
        return obj {
            BATCH_TASK_IDS to arr[batchTaskIds]
            inputOptionBlock(this)  //키값 말고 기타 옵션 넣기
        }.toString()
    }

}