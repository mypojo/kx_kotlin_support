package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.athena.getQueryExecution
import aws.sdk.kotlin.services.athena.model.QueryExecution
import aws.sdk.kotlin.services.athena.model.QueryExecutionContext
import aws.sdk.kotlin.services.athena.model.QueryExecutionState
import aws.sdk.kotlin.services.athena.model.QueryExecutionState.*
import aws.sdk.kotlin.services.athena.model.TooManyRequestsException
import aws.sdk.kotlin.services.athena.startQueryExecution
import ch.qos.logback.classic.Level
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.s3.getObjectLines
import net.kotlinx.aws.s3.s3
import net.kotlinx.concurrent.CoroutineSleepTool
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.core.Kdsl
import net.kotlinx.logback.LogBackUtil
import net.kotlinx.retry.RetryTemplate
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * 아테나 실행시 도우미
 * 다수의 아테나 쿼리 실행후 기다렸다 결과를 리턴해주는 편의용 모듈
 * checkTimeout 등의 이유료 내장 코루틴을 사용함
 *
 * 본격적으로 사용시 이거말고 SFN을 사용하는 것이 좋음
 *  */
class AthenaModule {

    @Kdsl
    constructor(block: AthenaModule.() -> Unit = {}) {
        apply(block)
    }

    private val log = KotlinLogging.logger {}

    /** 기본 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 데이터베이스 명 (기본스키마) */
    lateinit var database: String

    /** 워크그룹 (쿼리 결과 위치 있어야함) */
    var workGroup: String = "primary"

    /** 쿼리 종료되었는지 체크를 시도하는 간격 */
    var checkInterval: Duration = 1.seconds

    /** 쿼리 체크 타임아웃 */
    var checkTimeout: Duration = 10.minutes

    /** 기본 리트라이 */
    var retry: RetryTemplate = RetryTemplate {
        predicate = RetryTemplate.match(TooManyRequestsException::class.java) //InvalidRequestException 아님!
    }

    /** 고정 실행 컨텍스트 미리 생성. (스키마 미 지정시 디폴트 스키마) */
    private val _queryExecutionContext by lazy { QueryExecutionContext { this.database = this@AthenaModule.database } }

    /** 코루틴 기반 아테나 쿼리 실행 래퍼 */
    private inner class AthenaExecution(private val athenaQuery: AthenaQuery) {

        private val sleepTool = CoroutineSleepTool(checkInterval)

        /** 최초 실행시 서정 */
        private var startExecutionId: String? = null

        /** 상태 체크시마다 설정 */
        private var currentQueryExecution: QueryExecution? = null

        /** 쿼리 실행 */
        suspend fun start() {
            sleepTool.checkAndSleep() //첫 슬립 무시
            try {
                retry.withRetry {
                    startExecutionId = aws.athena.startQueryExecution {
                        this.queryString = athenaQuery.query
                        this.workGroup = this@AthenaModule.workGroup
                        this.queryExecutionContext = _queryExecutionContext
                        this.clientRequestToken = athenaQuery.token
                    }.queryExecutionId!!
                }
            } catch (e: Exception) {
                log.warn { "쿼리 오류 : ${athenaQuery.query}" }
                throw e
            }
        }

        /**
         * 일정 딜레이 후 실행 -> 정상 종료된경우 콜백 호출
         * @return 종료되었는지
         *  */
        suspend fun checkAndExecute(): Boolean {
            if (isCompleted()) return true

            val currentCnt = sleepTool.cnt.get()
            sleepTool.checkAndSleep()
            currentQueryExecution = aws.athena.getQueryExecution { this.queryExecutionId = startExecutionId }.queryExecution!!
            val status = currentQueryExecution!!.status!!
            val state: QueryExecutionState = status.state!!
            log.debug { "  ==> $currentCnt [$startExecutionId] : $state  ... " }

            if (!isCompleted()) return false

            if (!isOk()) {
                throw IllegalStateException("쿼리(${startExecutionId}) 실행 실패 : $status / ${status.stateChangeReason}")
            }

            //편의성 콜백 정의
            when (athenaQuery) {
                is AthenaExecute -> {
                    athenaQuery.outputLocation = currentQueryExecution!!.resultConfiguration!!.outputLocation!!
                    athenaQuery.callback?.invoke(currentQueryExecution!!) //콜백이 없으면 작동 안함.
                }

                is AthenaReadAll -> {
                    val outputLocation = currentQueryExecution!!.resultConfiguration!!.outputLocation!!
                    val s3Data = S3Data.parse(outputLocation)
                    val lines = aws.s3.getObjectLines(s3Data.bucket, s3Data.key)!!
                    athenaQuery.lines = lines
                    athenaQuery.callback.invoke(lines)
                }

                is AthenaDownload -> {
                    val outputLocation = currentQueryExecution!!.resultConfiguration!!.outputLocation!!
                    val s3Data = S3Data.parse(outputLocation)
                    val writeFile = File(AwsInstanceTypeUtil.INSTANCE_TYPE.root, outputLocation.substringAfterLast("/")) //이미 난수가 포함되어있음
                    log.debug { " -> s3 athena 결과 다운로드 : $outputLocation -> $writeFile" }
                    aws.s3.getObjectDownload(s3Data.bucket, s3Data.key, writeFile)
                    athenaQuery.file = writeFile
                    athenaQuery.callback.invoke(writeFile) //임시파일 삭제는 각 로직에서
                }
            }
            return true
        }

        /** wait를 끝내고 데이터를 읽어도 되는 상태인지 */
        @Suppress("BooleanMethodIsAlwaysInverted") //프로파일 버그인듯..
        fun isCompleted(): Boolean = currentQueryExecution?.status?.state in setOf(Succeeded, Cancelled, Failed)

        /** 정상 완료 상태인지 */
        fun isOk(): Boolean = currentQueryExecution?.status?.state in setOf(Succeeded)
    }

    //==================================================== 간단실행 ======================================================

    /**
     * 쿼리 결과 그대로를 다운로드 한다 (UTF-8)
     * ex) Koins.get<AthenaModule>().download(sql).renameTo(rptFile)
     *  */
    fun download(athenaQuery: String): File = runBlocking { startAndWait(AthenaDownload(athenaQuery)).file!! }

    /** 간단 샘플 */
    fun downloadIfEmpty(file: File, block: () -> String): File {
        if (!file.exists()) {
            log.warn { " -> 파일을 다운로드합니다.. $file" }
            download(block()).renameTo(file)
        }
        return file
    }

    /** 단건 처리 */
    suspend fun readAll(athenaQuery: String): List<List<String>> = startAndWait(AthenaReadAll(athenaQuery)).lines!!

    /** 단건 처리 (DSL용) */
    suspend fun readAll(block: () -> String): List<List<String>> = readAll(block())

    /**
     * 단건 처리
     * sqls.map { suspend { athenaModule.execute(it) } }.coroutineExecute()
     *  */
    //fun execute(athenaQuery: String): AthenaExecute = runBlocking { startAndWait(AthenaExecute(athenaQuery)) }
    suspend fun execute(athenaQuery: String): AthenaExecute = startAndWait(AthenaExecute(athenaQuery))

    /**
     * 내부 간단 실행기
     * 개별적으로 타임아웃을 적용한다.
     *  */
    suspend fun <T : AthenaQuery> startAndWait(query: T): T {
        withTimeout(checkTimeout) {
            val execution = AthenaExecution(query)
            execution.start()
            while (true) {
                val ok = execution.checkAndExecute()
                if (ok) break
            }
        }
        return query
    }

    /**
     * 동시 처리하는 간단 샘플
     *  */
    fun <T : AthenaQuery> startAndWait(query: List<T>): List<T> {
        query.map {
            suspend {
                startAndWait(it)
            }
        }.coroutineExecute()
        return query
    }

    //==================================================== 폐기 ======================================================

    /** 모든 쿼리 로직을 동시에 처리 (동시 실행 제한수 주의) */
    @Deprecated("일반 코루틴 startAndWait 사용하세요!!")
    fun startAndWaitAndExecute(querys: List<AthenaQuery>): List<AthenaQuery> {
        runBlocking { startExecute(querys) }
        return querys
    }

    /** 정상 작동하긴 하지만, 코루틴 로직이 여기 있을 필요가 없음. */
    @Deprecated("일반 코루틴 startAndWait 사용하세요!!")
    suspend fun startExecute(querys: List<AthenaQuery>): List<AthenaQuery> {
        val list = querys.map { AthenaExecution(it) }
        withTimeout(checkTimeout) {
            for (execution in list) {
                launch {
                    execution.start()
                    while (true) {
                        val ok = execution.checkAndExecute()
                        if (ok) break
                    }
                }
            }
        }
        return querys
    }


    companion object {

        /** 디버깅 로그 끄고싶을때 사용 */
        fun logLevel(level: Level) {
            LogBackUtil.logLevelTo(this::class.java.packageName!!, level)
        }

    }

}