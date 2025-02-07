package net.kotlinx.math


object MathUtil {

    /**
     * 간단한 재귀함수 예제
     *  */
    fun fact(n: Int) = _fact(n, 1)

    private tailrec fun _fact(n: Int, a: Int): Int = if (n == 0) a else _fact(n - 1, n * a)

}