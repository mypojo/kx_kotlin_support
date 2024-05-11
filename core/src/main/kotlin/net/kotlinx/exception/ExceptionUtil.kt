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

/**
 * 별 필요없다..  아래 클래스들 참조해서 없는거 추가.
 *
 * Throwables.propagate(e); 요런것
 * @see ..ExceptionUtils
 * @see ..com.google.common.base.Throwables
 * @see ..ThrowableAnalyzer
 */
object ExceptionUtil {

    /** 로깅용 간단 메세지 리턴  */
    @Deprecated("확장함수 사용", replaceWith = ReplaceWith("Bean"))
    fun toString(e: Throwable): String {
        return if (e.message == null) {
            "[${e.javaClass.simpleName}]"
        } else {
            "[${e.javaClass.simpleName}] ${e.message}"
        }
    }

//    /**
//     * Throwables.getRootCause 가 잘 안되서 걍 만듬
//     * ROOT 까지 해당 예외가 존재한다면 찾아서 리턴해준다.
//     * @return null이면 루트까지 해당 예외가 포함되어있지 않음
//     * ex) AnalysisException sqlEx = ExceptionUtil.getMatchCause(e,AnalysisException.class);
//     */
//    fun <T : Throwable> findMatchCause(ex: Throwable?, clazzs: Class<T>): T? {
//        if (ex == null) return null
//        if (ex.javaClass.isAssignableFrom(clazzs)) return ex as T
//        val cause = ex.cause
//        return findMatchCause(cause, clazzs)
//    }

//    /** 지정된 스택트레이스만 가져온다.  */
//    fun findStackTraces(throwable: Throwable, startsWith: String?): List<StackTraceElement> {
//        val list: MutableList<StackTraceElement> = mutableListOf()
//        if (throwable.stackTrace == null) return list
//        for (st in throwable.stackTrace) {
//            if (st.className.startsWith(startsWith!!)) {
//                list.add(st)
//            }
//        }
//        return list
//    }
}