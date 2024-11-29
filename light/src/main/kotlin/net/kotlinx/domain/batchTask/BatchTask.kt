package net.kotlinx.domain.batchTask


/** 
 * 배치 작업
 * 
 * 람다 256mb 기준 => 로직 2개 & 키워드 약 400개 정도 처리가능
 *  */
class BatchTask {

    /** 유니크한 ID */
    lateinit var id: String

    /** 간단 이름 */
    lateinit var name: String

    /** 설명 */
    var desc: List<String> = emptyList()

    /** 실행기 */
    lateinit var runtime: BatchTaskRuntime

}