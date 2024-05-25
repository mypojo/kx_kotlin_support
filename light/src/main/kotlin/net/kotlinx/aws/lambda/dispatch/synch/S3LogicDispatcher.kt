package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicHandler
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy

/**
 * S3 경로 입력에 반응하는 핸들러. (커스텀 입력 or SFN 자동실행)
 *  */
class S3LogicDispatcher : LambdaDispatch {

    companion object {
        /**
         * AWS가 S3 객체를 넣어줄때 사용하는 기본 키값을 동일하게 사용
         * 주의! 두문자가 대문자임
         * */
        const val KEY = "Key"

    }

    private val s3LogicHandler by koinLazy<S3LogicHandler>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val s3InputDataKey = input[KEY].str ?: return null //S3 형식이 아니라면 스킵. 여기 full path S3 경로가 전달된다.
        s3LogicHandler.execute(s3InputDataKey)
        /** 고정값 리턴 -> 개별 SFN 결과로 기록됨 */
        return obj {
            "ok" to s3InputDataKey
        }
    }


}


