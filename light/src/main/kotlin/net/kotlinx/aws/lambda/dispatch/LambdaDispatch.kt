package net.kotlinx.aws.lambda.dispatch

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.json.gson.GsonData

/** 람다 실행 설정 */
interface LambdaDispatch {

    /**
     * 체크 후 처리 가능 -> 이벤트 post 후  null이 아닌 객체를 리턴
     * @return  Map<String, Any> 으로 최종 변환되어서 람다 응답됨.
     * */
    suspend fun postOrSkip(input: GsonData, context: Context?): Any?
}