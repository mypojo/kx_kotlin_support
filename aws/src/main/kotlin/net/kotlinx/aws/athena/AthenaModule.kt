package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.athena.getQueryExecution
import aws.sdk.kotlin.services.athena.model.QueryExecution
import aws.sdk.kotlin.services.athena.model.QueryExecutionState
import aws.sdk.kotlin.services.athena.model.QueryExecutionState.*
import aws.sdk.kotlin.services.athena.startQueryExecution
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws1.AwsInstanceTypeUtil
import net.kotlinx.aws1.s3.S3Data
import net.kotlinx.aws1.s3.getObjectDownload
import net.kotlinx.aws1.s3.getObjectLines
import net.kotlinx.core1.thread.CoroutineSleepTool
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * 아테나 실행시 도우미
 * 아테나 쿼리 실행후 기다렸다 결과를 리턴해주는 편의용 모듈
 *  */
class AthenaModule(
    private val aws: AwsClient,
    /** 워크그룹 (쿼리 결과 위치 있어야함) */
    private val workGroup: String = "primary",
    /** 쿼리 종료되었는지 체크를 시도하는 간격 */
    private val checkIntervalMills: Long = TimeUnit.SECONDS.toMillis(1),
    /** 쿼리 체크 타임아웃 */
    private val checkTimeout: Long = TimeUnit.MINUTES.toMillis(10),
) {

    private val log = KotlinLogging.logger {}

    /** 코루틴 기반 아테나 쿼리 실행 래퍼 */
    inner class AthenaExecution(private val athenaQuery: AthenaQuery) {

        private val sleepTool = CoroutineSleepTool(checkIntervalMills)

        /** 최초 실행시 서정 */
        private var startExecutionId: String? = null

        /** 상태 체크시마다 설정 */
        private var currentQueryExecution: QueryExecution? = null

        /** 쿼리 실행 */
        suspend fun start() {
            sleepTool.checkAndSleep() //첫 슬립 무시
            startExecutionId = aws.athena.startQueryExecution {
                this.queryString = athenaQuery.query
                this.workGroup = workGroup
            }.queryExecutionId!!
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
                    athenaQuery?.callback?.invoke(currentQueryExecution!!) //콜백이 없으면 작동 안함.
                }

                is AthenaReadAll -> {
                    val outputLocation = currentQueryExecution!!.resultConfiguration!!.outputLocation!!
                    val s3Data = S3Data.parse(outputLocation)
                    val lines = aws.s3.getObjectLines(s3Data.bucket, s3Data.key)
                    athenaQuery.lines = lines
                    athenaQuery.callback.invoke(lines)
                }

                is AthenaDownload -> {
                    val outputLocation = currentQueryExecution!!.resultConfiguration!!.outputLocation!!
                    val s3Data = S3Data.parse(outputLocation)
                    val writeFile = File(AwsInstanceTypeUtil.instanceType.root, outputLocation.substringAfterLast("/"))
                    aws.s3.getObjectDownload(s3Data.bucket, s3Data.key, writeFile)
                    athenaQuery.file = writeFile
                    athenaQuery.callback.invoke(writeFile) //임시파일 삭제는 각 로직에서
                }
            }
            return true
        }

        /** wait를 끝내고 데이터를 읽어도 되는 상태인지 */
        fun isCompleted(): Boolean = currentQueryExecution?.status?.state in setOf(Succeeded, Cancelled, Failed)

        /** 정상 완료 상태인지 */
        fun isOk(): Boolean = currentQueryExecution?.status?.state in setOf(Succeeded)
    }

    /** 단건 처리 */
    fun download(athenaQuery: String): File = (startAndWaitAndExecute(listOf(AthenaDownload(athenaQuery) {})).first() as AthenaDownload).file!!

    /** 단건 처리 */
    fun readAll(athenaQuery: String): List<List<String>> = (startAndWaitAndExecute(listOf(AthenaReadAll(athenaQuery) {})).first() as AthenaReadAll).lines!!

    /** 단건 처리 */
    fun execute(athenaQuery: String) = startAndWaitAndExecute(listOf(AthenaExecute(athenaQuery)))

    /** 모든 쿼리 로직을 동시에 처리 (동시 실행 제한수 주의) */
    fun startAndWaitAndExecute(querys: List<AthenaQuery>): List<AthenaQuery> {
        val list = querys.map { AthenaExecution(it) }
        runBlocking {
            withTimeout(checkTimeout){
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
        }
        return querys
    }
}