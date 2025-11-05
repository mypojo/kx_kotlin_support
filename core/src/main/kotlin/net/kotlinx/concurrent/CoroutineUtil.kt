package net.kotlinx.concurrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


object CoroutineUtil {

    /**
     * 코드 확인용 간이 유틸
     * 편의상 예외 핸들러는 무시함
     * ex) CoroutineExceptionHandler
     *
     * 스래드로컬 사용시 ApiHolder 등에 코드를 넣을것
     * */
    fun fire(block: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) { block() }
    }

}
