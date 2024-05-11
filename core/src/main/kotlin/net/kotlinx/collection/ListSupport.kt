package net.kotlinx.collection


/**
 * 객체를 추가후 그대로 리턴해줌
 * 주로 static을 초기화 할때 사용
 * operator는 사용하지 않음
 *  */
fun <T> MutableList<T>.addAndGet(block: () -> T): T {
    val data = block()
    this += data
    return data
}

/**
 * 리스트를 조건에 따라 분리해준다.
 * 순서에 따라 나뉨으로 그룹바이하고는 다름.
 * */
@Deprecated("쓸모없어보인다")
inline fun <reified T> List<T>.chunkedBy(isStart: (value: T) -> Boolean): List<List<T>> {

    val chunkedList = mutableListOf<List<T>>()

    var current = mutableListOf<T>()
    for (any in this) {
        if (isStart.invoke(any)) {
            chunkedList += current.toList()
            current = mutableListOf()
            current += any
        } else {
            current += any
        }
    }
    return chunkedList.filter { it.isNotEmpty() }.toList()
}

/**
 * 체크 후 작업을 실행하는 인터페이스
 * 여러개 등록후, 순서대로 작동해서, 최초 null이 아닌게 리턴되면 전체 로직을 중단하고 해당 값을 사용한다.
 * 람다 수신 등에 사용됨
 * @return null -> 처리 안되었음
 */
fun <INPUT, RESULT> List<(INPUT) -> RESULT?>.invokeUntilNotNull(input: INPUT): RESULT? {
    for (exe in this) {
        val result = exe.invoke(input)
        if (result != null) return result
    }
    return null
}


/**
 * map을 flat 하게 해준다.
 *  */
fun <K, V> List<Map<K, V>>.flatten(): Map<K, V> = this.flatMap { it.entries }.associate { it.key to it.value }