package net.kotlinx.aws.batch


/** 배치잡 설정 오버라이드 */
data class BatchOverride(
    /** CPU */
    val vcpu: String = "0.25",
    /** 메모리 GB */
    val memory: Int = 1,
)