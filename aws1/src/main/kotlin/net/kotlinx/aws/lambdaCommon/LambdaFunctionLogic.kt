package net.kotlinx.aws.lambdaCommon

/** this를 생략할 수 없게 해줌 -> 1뎁스에서만 DSL 사용 가능 */
@DslMarker
annotation class LambdaFunctionLogicDsl

/** 각 람다 로직을 DSL로 정의 */
@LambdaFunctionLogicDsl
class LambdaFunctionLogic {

    /** 유니크한 ID */
    lateinit var id: String

    /** 설명 */
    var desc: List<String> = emptyList()

    /** 등록 핸들러. null이 리턴되면 스킵으로 간주 */
    lateinit var handler: LambdaLogicHandler

    /** 스탭스타트 핸들러. 이게 있으면 초기화 해줌 */
    var snapStart: (suspend () -> Unit)? = null

}