package net.kotlinx.spring.jpa


/**
 * DB 컬럼 커스텀 사이즈 정리.
 * 내림차순 하세요
 */
object JpaColumnSize {

    //==================================================== 고정 도메인 ======================================================
    const val IP: Int = 16
    const val EMAIL: Int = 64
    const val TEL: Int = 11

    /**
     * YYYYMMDD 형태의 날짜. date 타입을 쓰기에는 불편해서 VCHAR로 사용함
     */
    const val BASIC_DATE: Int = 8
}
