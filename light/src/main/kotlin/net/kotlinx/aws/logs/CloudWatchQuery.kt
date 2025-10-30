package net.kotlinx.aws.logs

import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CloudWatchQuery {

    /** 검색할 로그 디렉토리 */
    lateinit var logGroupNames: List<String>

    /** 조회 쿼리 */
    lateinit var query: String

    /** 몇번까지 시도할지? */
    var repeat: Int = 100

    /** 페이지 리미트. 디폴트로 max */
    var limit: Int = 10000

    /** 쿼리 종료되었는지 체크를 시도하는 간격 */
    var checkInterval: Duration = 1.seconds

    /** 쿼리 체크 타임아웃 */
    var checkTimeout: Duration = 1.minutes

    var startTime: LocalDateTime = LocalDateTime.now().minusDays(1)
    var endTime: LocalDateTime = LocalDateTime.now()

    /** CloudWatch regex는 슬래시(/), $, {, }, [, ], (, ), ?, +, *, ., ^ 같은 정규식 메타문자 주의 */
    val queryEncoded: String
        get() {
            val regexMeta = Regex("([/\\\\^$*+?.()|\\[\\]{}])")
            return query.replace(regexMeta, "\\\\$1")
        }

}