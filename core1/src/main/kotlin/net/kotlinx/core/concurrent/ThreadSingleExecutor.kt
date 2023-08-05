package net.kotlinx.core.concurrent

/**
 * 싱글 스래드로, 작업을 실행 후 예외를 무시한다.
 * 최종 체크로 예외가 있다면 한번만 예외를 던진다.
 * 여러 작업을 각각 처리하지만, 오류가 나더라도 나머지는 정상 처리하고싶을때 사용
 */
class ThreadSingleExecutor {

    private val runs = mutableListOf<() -> Unit>()

    fun regist(run: () -> Unit) {
        runs.add(run)
    }

    fun exe() {

        val failCnt = runs.map {
            try {
                it()
                return@map true
            } catch (e: Exception) {
                e.printStackTrace()
                return@map false
            }
        }.count { it.not() }

        if (failCnt > 0) throw RuntimeException("전체 ${failCnt}/${runs.size} 실패!")

    }
}