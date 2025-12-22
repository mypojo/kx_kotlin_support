package net.kotlinx.notion

import com.lectra.koson.obj
import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.bedrock.bra
import net.kotlinx.aws.bedrock.syncKnowledgeBase
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.putObject
import net.kotlinx.aws.s3.s3
import net.kotlinx.domain.item.tempData.TempDataRepository
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01
import java.time.LocalDateTime

internal class NotionDatabaseClient_실무테스트 : BeSpecHeavy() {

    companion object {
        const val DB_ID = "2b6effe4827d80fe8b08f8a1419cbc5a"
    }

    val tempDataRepository by lazy {
        TempDataRepository().apply {
            aws = koin<AwsClient>(findProfile49)
        }
    }

    val aws by lazy { koin<AwsClient>(findProfile49) }
    val key by lazyLoadStringSsm("/secret/api/notion", findProfile49)
    val database by lazy { NotionDatabaseClient(key) }
    val page by lazy { NotionPageClient(key) }
    val block by lazy { NotionBlockClient(key) }
    val pageLoader by lazy { NotionPageLoader(key) }

    init {
        initTest(KotestUtil.SLOW)

        Given("사전작업") {

            Then("노션데이터 변경분 S3올리고 날리지베이스 동기화") {

                //디렉토리 구조   {root}/{dbId}/index.md   (사실 page 전체가 인덱싱 되기 때문에 불필요)
                //디렉토리 구조   {root}/{dbId}/page_{pageId}.md
                val now = LocalDateTime.now()
                val lastUpdateTime = tempDataRepository.findLastUpdate() //변경시점은 DDB에 저장
                val filter = NotionFilterSet.lastEditAfter(lastUpdateTime.minusMinutes(10)) //10분의 버퍼를 줘서 필터링
                log.info { "최근 업데이트시간(${lastUpdateTime.toKr01()})기준 필터링.." }

                // 3) 로컬 저장 위치 준비
                val localRoot = AwsInstanceTypeUtil.INSTANCE_TYPE.tmpDir().slash("notion").slash("export")
                val uploadPath = S3Data.parse("s3://dmp-data-prod/knowledgeBase/notion-PRD")

                // 5) 조회 -> 파일 저장 -> 업로드
                database.query(DB_ID, filter).collect { list ->
                    list.forEach { row ->
                        val file = localRoot.slash("${row.id}.txt")
                        val allBlocks = pageLoader.load(row.id)
                        val allText = listOf(row.body.toString()) + allBlocks.map { it.markdown }
                        file.writeText(allText.joinToString("\n"))

                        val upload = uploadPath.slash(file.name)
                        aws.s3.putObject(upload, file)
                    }
                    log.info { " -> ${list.size}  건 처리 완료 (로컬 저장 및 S3 업로드)" }
                }
                tempDataRepository.putLastUpdate(now)
                aws.bra.syncKnowledgeBase("CNP5M931HU", "8C2U3AVV8G")
            }

        }

        Given("step01") {

            Then("생성된 PRD를 노션DB에 추가") {
                val data = obj {
                    "업무명" to NotionCell.Title.toNotion("신규프로젝트01")
                    "프로젝트" to NotionCell.Select.toNotion("기타")
                    "기획" to NotionCell.Select.toNotion("홍길동")
                    "개발" to NotionCell.Select.toNotion("김영삼")
                }
                val row = database.insert(DB_ID, data)
//                val contents = prdInitialContents() //LLM에게 json으로 만들어달라고 한고 그대로 입력
//                page.insert(row.id, contents)
            }
        }

        Given("step02") {

            Then("노션DB 리스트 조회") {
                val pages = database.query(DB_ID).first()
                pages.size shouldBeGreaterThan 0
                listOf("id", "프로젝트", "기획", "개발", "업무명", "edited").toTextGridPrint {
                    pages.map {
                        val elements = it.properties
                        arrayOf(
                            it.id, elements["프로젝트"]?.viewText,
                            elements["기획"]?.viewText, elements["개발"]?.viewText, elements["업무명"]?.viewText, it.lastEditedTime
                        )
                    }
                }
            }

            Then("생성된 측정공수로 표 업데이트") {
                val pageId = "2bfeffe4-827d-81ec-bc71-c133e836bf45"
                val pages = page.list(pageId).first()
                val table = pages.find { it.type == "table" }!!
                log.info { "테이블 ${table.id}" }

                //헤더 날리고 작업수행
                block.list(table.id).flatMapConcat { it.asFlow() }.drop(1).collect { row ->
                    block.updateTableRow(row.id) { list ->
                        list.toMutableList().apply {
                            this[0] = this[0] + "."
                            this[1] = "${this[1].toLong() + 1}"
                        }
                    }
                }
            }

        }

    }
}