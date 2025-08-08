package net.kotlinx.domain.job


/**
 * 잡 컨텍스트의 네이밍
 * */
object JobContextNaming {

    //==================================================== context ======================================================

    /**
     * 전체 수 (진행율 계산에 사용)
     * 성공수 + 실패수 = 진행된 수
     *  -> 이걸로 진행율을 구할 수 있음
     *  */
    const val TOTAL_CNT = "totalCnt"

    /** 성공 수 */
    const val SUCCESS_CNT = "successCnt"

    /** 실패 수 */
    const val FAIL_CNT = "failCnt"


}