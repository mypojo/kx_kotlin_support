package net.kotlinx.exception


/**
 * 비지니스 로직 처리시 던지는 알려진 예외.
 * 예외가 시키는대로 코딩하면 됨
 */
sealed class KnownException(message: String) : RuntimeException(message) {
    
    //==================================================== 계속 진행 ======================================================

    /** 배치 처리 등에서 현재 상품을 스킵하고 지나가야 할때 사용  */
    class ItemSkipException(message: String) : KnownException(message)

    /** 배치 처리 등에서 현재 상품을 스킵하고 다음부터 비슷한게 반복 안되도록 조치(제거)하고 지나가야 할때 사용 (프록시 제거 등)  */
    class ItemRemoveException(message: String) : KnownException(message)

    /** IO등의 이유로 에러가난것들. 재시도하면 가능성이 있음. 리트라이 설정이 추가되어야함  */
    class ItemRetryException(message: String) : KnownException(message)

    //==================================================== 중단 ======================================================

    /** 진행을 중지시켜야함. ex) 데이터에 오류가 있음. */
    class StopException(message: String) : KnownException(message)
    
    /** 세션이 만료되었음. 로그인 등의 추가 작업 필요.  ex) 리프레시 토큰 오류. 재로그인을 유도해야함 */
    class SessionExpiredException(message: String) : KnownException(message)
}