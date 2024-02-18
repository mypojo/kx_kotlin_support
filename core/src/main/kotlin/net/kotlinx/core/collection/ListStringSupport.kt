package net.kotlinx.core.collection

/**
 * 오른쪽 끝에 패드를 더한다
 * 주로 csv 결과를 맞춤 할때 사용함
 * */
fun List<String>.padEnd(size: Int, pad: String = ""): List<String> {
    val padList = (0 until size - this.size).map { pad }
    return this + padList
}