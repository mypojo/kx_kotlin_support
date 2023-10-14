package net.kotlinx.core.calculator

import net.kotlinx.core.number.minWith

/**
 * 배치 작업시 청크 단위에 대한 계산을 도와줌
 * */
class BatchChunk(
    /** 전체 수 */
    val totalCount: Long,
    /** 페이지 하나가 차지하는 크기 */
    val pageSize: Long,
) {

    /** 가장 큰 페이지 번호 */
    val maxPageNo: Long = ( (totalCount-1) / pageSize) + 1

    /** 이 페이지 번호의 시작/종료 */
    fun range(pageNo: Long): Pair<Long, Long> {
        check(pageNo <= maxPageNo) { "페이지 번호는 $maxPageNo 보다 작거나 같아야 합니다." }
        return pageSize * (pageNo-1) + 1 to (pageSize * pageNo).minWith(totalCount)
    }

}