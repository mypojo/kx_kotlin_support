package net.kotlinx.collection

import kotlinx.coroutines.withTimeout
import net.kotlinx.concurrent.CoroutineSleepTool
import net.kotlinx.exception.KnownException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


//==================================================== 횟수 제한이 아니라 타임아웃 제한 ======================================================


/**
 * 횟수 제한이 아니라 시간으로 제한을 거는데 사용
 * ex) 데이터 갱신 요청, athena 쿼리 등을 요청 후, 완료를 기다림
 * @param action 리턴이 null이 아니면 종료
 * */
suspend fun <T> doUntilTimeout(checkInterval: Duration = 10.seconds, checkTimeout: Duration = 10.minutes, action: suspend () -> T?): T {
    val sleeper = CoroutineSleepTool(checkInterval)
    return withTimeout(checkTimeout) {
        var theResult: T? = null
        while (true) {
            sleeper.checkAndSleep()
            val result = action()
            theResult = result ?: continue
            break
        }
        theResult!!
    }
}

//==================================================== repeat 시리즈가 별로임 & suspend 문제로 새로 제작함 ======================================================

/**
 * maxTimes 만큼 최대로 반복해서 결과를 리턴한다. (무한반복 금지) -> maxTimes 도달시 에러로 간주
 * ex) 조회 데이터가 더 없을때까지 데이터 조회 ex) 페이징 API
 * @param action 리소스가 들어가는 작업,  100회 입력시 0~99 까지 작동. false 이면 다음 작업 중지
 * @return flatten() 해서 쓰세요~
 * */
suspend fun <T> doUntil(maxTimes: Int = 100, action: suspend (Int) -> Pair<List<T>, Boolean>): List<List<T>> {
    val results = mutableListOf<List<T>>()

    for (index in 0 until maxTimes) {
        val curentResult = action(index)
        results.add(curentResult.first)
        if (!curentResult.second) return results
    }

    throw KnownException.StopException("최대 작동횟수 ${maxTimes}를 초과했습니다.")
}

/**
 * 단축 메소드.
 * 결과가 빈값이 아닐경우
 * */
suspend fun <T> doUntilNotEmpty(maxTimes: Int = 100, action: suspend (Int) -> List<T>): List<List<T>> {
    return doUntil(maxTimes) {
        val list = action.invoke(it)
        list to list.isNotEmpty()
    }
}

/**
 * 단축 메소드.
 * 결과가 특정 숫자와 같거나 이상인경우
 * ex) 토큰이 없고 페이징 번호를 호출하는 페이징 API 전체 호출  (AWS 등에는 사용안함)
 * */
suspend fun <T> doUntilMax(resultMax: Int, maxTimes: Int = 100, action: suspend (Int) -> List<T>): List<List<T>> {
    return doUntil(maxTimes) {
        val list = action.invoke(it)
        list to (list.size >= resultMax)
    }
}

/**
 * 단축 메소드.
 * 최초 이후 토큰이 null일때까지
 * ex) 중단 토큰을 주는 AWS API
 * 경고!!! Flow 지원되는거에는 안써도 됩니다!! Flow 쓰세요
 * ex) S3Client.listAllDirs
 * */
suspend fun <T> doUntilTokenNull(firstToken: Any? = null, maxTimes: Int = 100, action: suspend (Int, Any?) -> Pair<List<T>, Any?>): List<List<T>> {
    var token: Any? = firstToken
    return doUntil(maxTimes) {
        val (list, currentToken) = action.invoke(it, token)
        token = currentToken
        list to (currentToken != null)
    }
}