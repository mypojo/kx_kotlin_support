package net.kotlinx.aws.lambda

import java.io.File

/** 람다 유틸들  */
object LambdaUtil {

    /** 람다의 로컬디렉토리 디폴트 경로. 여기부터 512mb가 할당된다.  */
    val ROOT = File("/tmp")

}