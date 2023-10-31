package net.kotlinx.notion.work

import net.kotlinx.core.time.TimeString
import kotlin.time.Duration.Companion.hours

/** 지문인식기 프로그램에서 csv로 받은 데이터 */
data class WorkInput(val date: String, val time: String, val name: String)

data class WorkOutput(val date: String, val name: String, val startTime: String, val endTime: String, val workSec: Long) {

    lateinit var workGroup: WorkGroup

    val workType: String
        get() = when (workSec) {
            0L -> "연차"
            in 0..4.hours.inWholeSeconds -> "확인필요"
            in 4..8.hours.inWholeSeconds -> "반차"
            else -> "정상"
        }

    val workTimeString: String
        get() = TimeString(workSec * 1000).toString()

    val valid: Boolean
        get() = workSec >= 8.hours.inWholeSeconds

}

class WorkGroup(block: WorkGroup.() -> Unit = {}) {

    /** 그룹명 */
    lateinit var groupName: String

    lateinit var leaderName: String

    lateinit var workers: List<String>

    init {
        block(this)
    }

    val names: List<String> = workers + leaderName
}