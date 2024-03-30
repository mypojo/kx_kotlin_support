package net.kotlinx.aws.lambdaUrl

import net.kotlinx.aws.lambda.LambdaMapResult


/** 라우터 정보 */
class FunctionRouteInfo {

    /**
     * "/" 입력시 디폴트 실행으로 간주
     * 프리픽스 매칭이며, 먼저 등록한 순서대로 작동한다.
     *  */
    lateinit var pathPrefix: String

    /** 상세 설명 */
    var desc: List<String> = emptyList()

    /** 처리 */
    lateinit var process: suspend (LambdaUrlInput) -> LambdaMapResult


}
