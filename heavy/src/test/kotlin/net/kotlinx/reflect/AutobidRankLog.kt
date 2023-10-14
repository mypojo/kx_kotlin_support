package net.kotlinx.reflect

import java.time.LocalDateTime

/**
 * 임시 로그 객체
 */
class AutobidRankLog {

    /** 기준날짜 */
    var basicDate: String = ""

    /** 광고주명(크롤링시 타이틀) */
    var advName: String = ""

    /** 키워드명 */
    var kwdName: String = ""

    /** 도메인(경쟁업체 도메인) */
    var domain: String = ""

    /** 디바이스 */
    var device: String = ""

    /** 시간(크롤링 시간) */
    var eventTime: LocalDateTime = LocalDateTime.now()

    /** 현재 순위 */
    var nowRank: Long = 0L


}
