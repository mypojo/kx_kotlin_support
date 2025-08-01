package net.kotlinx.notion

import mu.KotlinLogging
import net.kotlinx.google.calendar.GoogleCalendar
import net.kotlinx.google.calendar.GoogleCalendarData
import net.kotlinx.string.toLocalDateTime
import net.kotlinx.time.toF01
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime

/**
 * 노션 DB 를 구글 캘린더로 변환해줌
 * ex) 5분에 한번씩 AWS Lambda로 동기화 -> 월비용
 *
 * https://github.com/BoD/klibnotion 쓰세요
 *  */
@Deprecated("klibnotion 쓰세요")
class NotionDatabaseToGoogleCalendar(block: NotionDatabaseToGoogleCalendar.() -> Unit = {}) : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val googleCalendar: GoogleCalendar by inject()
    private val notionDatabaseClient: NotionDatabaseClient by inject()
    private val notionPageBlockClient: NotionPageBlockClient by inject()

    //==================================================== 노션 설정 ======================================================

    /** 노션 DB id */
    lateinit var notionDbId: String

    /** 노션 page id. DB가 있는 페이지 그대로 사용하면됨 */
    lateinit var notionPageId: String

    /** 구글캘린더 event ID가 담길 컬럼 이름  */
    var gceid: String = "gceId"

    /** 구글캘린더 title 담길 컬럼 이름  */
    var title: String = "title"

    /** 구글캘린더 desc 담길 컬럼 이름  */
    var desc: String = "desc"

    /** 구글캘린더 date 담길 컬럼 이름  */
    var date: String = "date"

    /** 구글캘린더 주소를 구분할 컬럼 명. 이 컬럼이 없으면 기본으로 매핑  */
    var type: String = "type"

    /** 블록을 읽어서 최근 동기화 시간으로 */
    var fromBlock: (String) -> LocalDateTime = { it.split("=")[1].trim().toLocalDateTime() }

    /** 최근 동기화 시간을 블록으로 (파싱할 날짜 포맷에 한글이 들어가지 않도로고 주의!) */
    var toBlock: (LocalDateTime) -> String = { "최근동기화시간 = ${it.toF01()}" }

    //==================================================== 구글 캘린더 설정 ======================================================

    /** 구글캘린더 기본주소 */
    lateinit var calendarDefaultId: String

    /** 구글캘린더 주소 매핑 정보 */
    var calendarTypeIdMap: Map<String, String> = emptyMap()

    init {
        block(this)
    }

    /** 스냅스타트용 */
    suspend fun snapstart() {
        log.warn { "스냅스타트.." }
        notionPageBlockClient.blocks(notionPageId, 1)
        googleCalendar.list(calendarDefaultId, 1)
    }

    /**
     * 마지막 동기화 시간 이후부터 지금까지의 변경데이터 스캔
     * */
    fun updateOrInsert() {
        log.warn { "이거 기억이 안난다.." }

//        val start = TimeStart()
//
//        //먼저 마지막 수정 시간을 스캔한다. 페이지의 가장 첫 라인 사용.
//        val synchInfoCell = notionPageBlockClient.blocks(notionPageId, 1).first()
//        val lastSynchTime = fromBlock(synchInfoCell.cell!!.viewText)
//
//        val scanStartTime = lastSynchTime.plusSeconds(1) //1초 이후로 스캔
//        val scanEndTime = LocalDateTime.now()
//
//        val filter = NotionFilterSet.lastEditBetween(scanStartTime to scanEndTime)
        //val filter = NotionFilterSet.lastEditAfter(scanStartTime)
//        val notionRows = notionDatabaseClient.queryAll(notionDbId, filter)
//        notionRows.forEach { notionRow ->
//
//            val dateCell = notionRow.colimns.firstOrNull { it.name == date } ?: throw IllegalStateException("date column is required")
//            val titleCell = notionRow.colimns.firstOrNull { it.name == title } ?: throw IllegalStateException("title column is required")
//            val descCell = notionRow.colimns.firstOrNull { it.name == desc } ?: throw IllegalStateException("desc column is required")
//            val dateValue = when {
//                dateCell.value.contains("~") -> {
//                    val range = dateCell.value.split("~").map { it.trim() }
//                    range[0].toLocalDate() to range[1].toLocalDate()
//                }
//
//                else -> dateCell.value.toLocalDate() to dateCell.value.toLocalDate()
//            }
//
//            val calendarId = notionRow.colimns.firstOrNull { it.name == type }?.let { calendarTypeIdMap[it.value] } ?: calendarDefaultId
//
//            val calendarData = GoogleCalendarData {
//                this.title = titleCell.value
//                this.desc = descCell.value
//                this.date = dateValue
//            }
//
//            val gceId = notionRow.colimns.firstOrNull { it.name == gceid }?.value ?: throw IllegalStateException("gceid column is required")
//            if (gceId.isEmpty()) {
//                synchInsert(calendarId, calendarData, notionRow.id)
//            } else {
//                synchUpdate(calendarId, calendarData, gceId, notionRow.id)
//            }
//        }

        //노션 블럭정보 업데이트
        //notionPageBlockClient.update(NotionCell2(synchInfoCell.cell!!.id, NotionCellType.rich_text, toBlock(scanEndTime)))

//        log.info {
//            val duration = ChronoUnit.MILLIS.between(scanStartTime, scanEndTime).toTimeString()
//            "스캔 [${scanStartTime.toIso()}~${scanEndTime.toIso()}] (${duration} 간의 데이터변경건) : 처리건수 ${notionRows.size} -> $start"
//        }
    }

    private suspend fun synchInsert(calendarId: String, calendarData: GoogleCalendarData, notionPageId: String) {
        googleCalendar.insert(calendarId, calendarData)
        //노션에 구글 캘린더 ID를 업데이트함
        notionDatabaseClient.update(
            notionDbId, notionPageId, listOf(
                NotionCell2(gceid, NotionCellType.rich_text, calendarData.eventId),
            )
        )
        log.debug { " -> [${calendarData.title}] insert" }
    }

    private suspend fun synchUpdate(calendarId: String, calendarData: GoogleCalendarData, gceId: String, notionPageId: String) {
        try {
            calendarData.eventId = gceId
            googleCalendar.update(calendarId, calendarData)
            log.debug { " -> [${calendarData.title}] update" }
        } catch (e: Exception) {
            log.warn { "업데이트 실패 (캘린더ID 변경) -> gceId(${gceId})를 초기화 후 insert" }
            calendarData.eventId = ""
            synchInsert(calendarId, calendarData, notionPageId)
        }
    }


}