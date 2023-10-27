package net.kotlinx.aws.lambdaUrl


/** 라우터 정보 */
class FunctionRouteInfo {

    /**
     * 공백문자인경우 디폴트 실행
     * 접미어가 일치해야함
     *  */
    lateinit var pathPrefix: String

    /** 상세 설명 */
    var desc: List<String> = emptyList()

    /** 처리 */
    lateinit var process: (net.kotlinx.aws.lambdaUrl.LambdaUrlInput) -> net.kotlinx.aws.lambdaUrl.LambdaUrlOutput


}
