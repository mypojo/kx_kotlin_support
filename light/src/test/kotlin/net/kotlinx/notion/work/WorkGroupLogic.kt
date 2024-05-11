package net.kotlinx.notion.work

import mu.KotlinLogging
import net.kotlinx.notion.NotionCell2
import net.kotlinx.notion.NotionCellType
import net.kotlinx.notion.NotionDatabaseClient
import net.kotlinx.notion.NotionFilterSet
import net.kotlinx.string.CharSets
import net.kotlinx.string.toLocalTime
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

    lateinit var groups: List<WorkGroup>

    fun read(file: File): List<WorkOutput> {
        log.info { "파일 처리 [$file ] ..." }
        val worksOrg = file.readLines(CharSets.MS949).drop(2).map { line ->
            val split = line.split(",")
            try {
                WorkInput(split[0], split[1], split[4])
            } catch (e: Exception) {
                log.error { "실패 라인 : $line" }
                throw e
            }
        }
        val basicDate = worksOrg.minOf { it.date } //가장 작은 날을 기준 날짜로 함. (익일이 올 수 있나?)

        val workOutputs = worksOrg.filter { it.date == basicDate }.groupBy { it.name }.values.map { v ->
            val first = v.first()
            val startTime = v.minOf { it.time }.padStart(6,'0')
            val endTime = v.maxOf { it.time }.padStart(6,'0')
            val totlaSec = endTime.toLocalTime().toSecondOfDay() - startTime.toLocalTime().toSecondOfDay()
            val workSec = totlaSec - (if (totlaSec >= 7.hours.inWholeSeconds) 1.hours.inWholeSeconds else 0)  //7시간 초과인경우 점심시간 1시간 고려
            WorkOutput(basicDate, first.name, startTime, endTime, workSec)
        }

        log.trace { "팀 등록" }
        val workerNames = workOutputs.map { it.name }
        val workGroup = groups.first { g -> g.names.any { it in workerNames } }
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
                NotionCell2("날짜", NotionCellType.date, basicDate),
            )
        )
        val notionLines = database.queryAll(dbId, filter)
        if (notionLines.isNotEmpty()) {
            log.warn { "기존파일 ${notionLines.size}건 삭제!" }
            notionLines.forEach { database.delete(it.id) }
        }

        log.info { "${workOutputs.size}건 입력됨.." }
        workOutputs.forEach { work ->
            database.insert(
                dbId, listOf(
                    NotionCell2("팀", NotionCellType.select, work.workGroup.groupName),
                    NotionCell2("날짜", NotionCellType.date, work.date),
                    NotionCell2("이름", NotionCellType.rich_text, work.name),
                    NotionCell2("출근", NotionCellType.rich_text, work.startTime),
                    NotionCell2("퇴근", NotionCellType.rich_text, work.endTime),
                    NotionCell2("체류시간", NotionCellType.rich_text, work.workTimeString),
                    NotionCell2("정상근무", NotionCellType.checkbox, "${work.valid}"),
                    NotionCell2("근무타입", NotionCellType.select, work.workType),
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