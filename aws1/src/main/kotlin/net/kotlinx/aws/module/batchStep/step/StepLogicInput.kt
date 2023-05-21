package net.kotlinx.aws.module.batchStep.step

/**
 * S3에 JSON 덩어리로 저장되는 입력 데이터
 * */
data class StepLogicInput(
    /** 커스텀 로직 이름 */
    val logicName: String,
    /** 데이터들 */
    val datas: List<String>,
    /** 로직 옵션 */
    val logicOption: String = "{}",
) {
    lateinit var inputKey: String
    lateinit var fileName: String
    lateinit var sfnId: String
}