package net.kotlinx.notion.work

import mu.KotlinLogging
import net.kotlinx.core.string.CharSets
import net.kotlinx.core.string.toLocalTime
import net.kotlinx.notion.NotionCell
import net.kotlinx.notion.NotionCellType
import net.kotlinx.notion.NotionDatabaseClient
import net.kotlinx.notion.NotionFilterSet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.time.Duration.Companion.hours

class WorkGroupLogic(block: WorkGroupLogic.() -> Unit = {}) : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val database: NotionDatabaseClient by inject()

    lateinit var dbId: String

    lateinit var inputDir: File

    lateinit var backupDir: File

    fun read(file: File): List<WorkOutput> {
        log.info { "파일 $file 처리..." }
        val worksOrg = file.readLines(CharSets.MS949).drop(2).map { it.split(",") }.map { WorkInput(it[0], it[1], it[4]) }
        val basicDate = worksOrg.minOf { it.date } //가장 작은 날을 기준 날짜로 함. (익일이 올 수 있나?)

        val workOutputs = worksOrg.filter { it.date == basicDate }.groupBy { it.name }.values.map { v ->
            val first = v.first()
            val startTime = v.minOf { it.time }
            val endTime = v.maxOf { it.time }
            val totlaSec = endTime.toLocalTime().toSecondOfDay() - startTime.toLocalTime().toSecondOfDay()
            val workSec = totlaSec - (if (totlaSec >= 7.hours.inWholeSeconds) 1.hours.inWholeSeconds else 0)  //7시간 초과인경우 점심시간 1시간 고려
            WorkOutput(basicDate, first.name, startTime, endTime, workSec)
        }

        log.trace { "팀 등록" }
        val workerNames = workOutputs.map { it.name }
        val workGroup = GROUPS.first { g -> g.names.any { it in workerNames } }
        workOutputs.onEach { it.workGroup = workGroup }

        log.trace { "연차 사용한사람 추가" }
        val empty = workGroup.names - workerNames.toSet()
        val emptyWorker = empty.map { WorkOutput(basicDate, it, "-", "-", 0) }.onEach { it.workGroup = workGroup }

        log.info { "대상파일 ${file.absolutePath} : 팀 ${workGroup.groupName} / 데이터수 ${workOutputs.size}" }
        return workOutputs + emptyWorker
    }

    suspend fun updateAll() {
        inputDir.listFiles().forEach { update(it) }
    }

    /** 파일을 읽어서 WorkOutput로  */
    suspend fun update(file: File) {

        val workOutputs = read(file)
        check(workOutputs.size >= 2) { "너무 적은 데이터!!" }

        val basicDate = workOutputs.minOf { it.date }!!
        val filter = NotionFilterSet.eq(
            listOf(
                NotionCell("날짜", NotionCellType.date, basicDate),
            )
        )
        val notionLines = database.queryAll(dbId, filter)
        if (notionLines.isNotEmpty()) {
            log.warn { "기존파일 ${notionLines.size}건 삭제!" }
            notionLines.forEach { database.delete(dbId, it.id) }
        }

        log.info { "${workOutputs.size}건 입력됨.." }
        workOutputs.forEach { work ->
            database.insert(
                dbId, listOf(
                    NotionCell("팀", NotionCellType.select, work.workGroup.groupName),
                    NotionCell("날짜", NotionCellType.date, work.date),
                    NotionCell("이름", NotionCellType.rich_text, work.name),
                    NotionCell("출근", NotionCellType.rich_text, work.startTime),
                    NotionCell("퇴근", NotionCellType.rich_text, work.endTime),
                    NotionCell("체류시간", NotionCellType.rich_text, work.workTimeString),
                    NotionCell("정상근무", NotionCellType.checkbox, "${work.valid}"),
                    NotionCell("근무타입", NotionCellType.select, work.workType),
                )
            )
        }

        log.trace { "파일 백업" }
        backupDir.mkdir()
        file.renameTo(File(backupDir, file.name))
    }

    init {
        block(this)
    }

}