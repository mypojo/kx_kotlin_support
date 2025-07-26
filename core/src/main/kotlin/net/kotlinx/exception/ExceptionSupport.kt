package net.kotlinx.exception

/**
 * 전체 causes를 리턴함.
 * ex) 전체 스택에 IoException이 있는지 체크 = cause -> cause.causes().any { it is IOException }
 * */
fun Throwable.causes(): List<Throwable> {
    val list: MutableList<Throwable> = mutableListOf()
    var current: Throwable? = this
    while (current != null) {
        list.add(current)
        current = current.cause
    }
    return list
}

/** 로깅용 간단 메세지 리턴 */
fun Throwable.toSimpleString(): String {
    return if (this.message == null) {
        "[${this.javaClass.simpleName}]"
    } else {
        "[${this.javaClass.simpleName}] ${this.message}"
    }
}