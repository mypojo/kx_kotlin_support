package net.kotlinx.string

/**
 * 주어진 값이 비어있으면 블록 리턴 else 그대로 사용
 *  -> 자주 사용되는건데 null & empty 둘다 체크하는 버전이 없어서 만들었음
 * ex) 인라인 결과로 주어진 값이 비어있는경우 디폴트값을 세팅
 *
 * 아래 2개가 원래 있는 기본기능. 널 가능버전이라서 이름 그대로 사용함!
 * @see ifEmpty
 * @see ifBlank
 *  */
inline fun String?.ifEmpty(block: () -> String): String = if (this.isNullOrEmpty()) block() else this.toString()


/**
 * let의 변형판.
 * 문자열에만 있는 empty 상태를 체크해서, empty가 아닌 경우에만 let 블록 실행
 * ex) CSV 입력값이 있으면 객체 리턴. 아니라면 ?: 로 디폴트 객체 리턴
 * ex) 요청값이 있을때만 블록 실행
 * @return null인경우 ?: 사용 가능하도록 null이 리턴되어야 한다
 * */
inline fun <T> String?.lett(block: (String) -> T): T? {
    if (this.isNullOrEmpty()) return null
    return block(this)
}

/**
 * 널 & empty 세이프한  enum 로드
 *  */
inline fun <reified T : Enum<T>> enumValueOf(str: String?, nullValue: T): T = str.lett { enumValueOf<T>(it) } ?: nullValue
