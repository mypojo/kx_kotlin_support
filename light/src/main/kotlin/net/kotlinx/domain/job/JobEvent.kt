package net.kotlinx.domain.job

/**
 * job에서 발생하는 이벤트
 */
data class JobEvent(

    var job: Job,

    /** 전달 메세지들  */
    val msgs: List<String> = emptyList(),

    /** 예외가 발생했을 경우  */
    val err: Throwable? = null,

    )