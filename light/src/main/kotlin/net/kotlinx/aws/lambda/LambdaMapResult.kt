package net.kotlinx.aws.lambda

/** 람다결과(map) 형식 지원하는 객체 */
interface LambdaMapResult {

    fun toLambdaMap(): Map<String, Any>

}