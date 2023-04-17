package net.kotlinx.aws.lambda

import java.io.File

/** 람다 유틸들  */
object LambdaUtil {

    /** 람다의 로컬디렉토리 디폴트 경로. 여기부터 512mb가 할당된다.  */
    val ROOT = File("/tmp")

    //==================================================== 간단 예약어들  ======================================================

    /** 공용 람다 내부에서 각 실행들의 구분 키값 */
    const val METHOD = "method"

    /** 정상 결과 리턴 문자열 */
    const val OK = "ok"

}