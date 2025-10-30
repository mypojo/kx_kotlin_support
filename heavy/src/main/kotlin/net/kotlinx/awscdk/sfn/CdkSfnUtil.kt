package net.kotlinx.awscdk.sfn

/**
 * Step Functions 관련 공통 유틸
 */
object CdkSfnUtil {

    /**
     * Lambda 태스크에서 기본적으로 리트라이할 오류 목록
     */
    val DEFAULT_RETRY_ERRORS: List<String> = listOf(

        /** AWS Lambda 서비스 자체 문제로 요청이 실패했을 때 발생. 타임아웃도 여기 걸치는것으로 예상됨 */
        "Lambda.ServiceException",

        /** Lambda 내부보다는 “Lambda API 호출 레벨”에서의 오류 */
        "Lambda.AWSLambdaException",

        /** Step Functions이 Lambda 호출 시 SDK 레벨에서 발생하는 오류 */
        "Lambda.SdkClientException",

        /** Lambda의 동시 실행 제한(concurrency limit)에 걸려 호출이 거부될 때 발생 -> 당연히 리트라이 해야함 */
        "Lambda.TooManyRequestsException",

        ///** 코드 내부 throw 하는것. 코딩으로 조절 가능하니 리트라이를 안해야 하는 경우가 더 많음  */
        //"States.TaskFailed",
    )
}
