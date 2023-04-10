package net.kotlinx.core1.collection

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