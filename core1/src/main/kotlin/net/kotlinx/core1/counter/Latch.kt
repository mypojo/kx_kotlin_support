package net.kotlinx.core1.counter

/**
 * 딱 한번만 false로 바뀌는 Latch이다.
 * 클래스 이름으로 의도를 표현하기 위해서 만듬
 * 가능하면 늦은 초기화를 사용할것
 */
class Latch {

    private var isFirst = true

    /** @return  첫 호출인지 여부
     */
    @Synchronized
    fun check(block: () -> Unit = {}): Boolean {
        if (isFirst) {
            isFirst = false
            block()
            return true
        }
        return false
    }
}