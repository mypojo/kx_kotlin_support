package net.kotlinx.concurrent


object ThreadUtil {

    /**
     * 코드 확인용 간이 유틸
     * 편의상 예외 핸들러는 무시함
     * */
    fun fire(block: () -> Unit) = Thread { block() }.start()

}
