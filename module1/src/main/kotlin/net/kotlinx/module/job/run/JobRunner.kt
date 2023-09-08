package net.kotlinx.module.job.run

import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.module.job.*
import net.kotlinx.module.job.JobStatus.*
import net.kotlinx.module.job.run.JobRoot.JobTasklet
import net.kotlinx.module.reflect.newInstance
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.time.LocalDateTime

/**
 * 싱글톤이다.
 * 여기에서는 개발 환경을 구분하지 않는다.
 */
class JobRunner : KoinComponent {

    private val log = KotlinLogging.logger {}

    //==================================================== 주입 ======================================================
    private val jobRepository: JobRepository by inject()
    private val jobFactory: JobFactory by inject()
    private val eventBus: EventBus by inject()
    private val awsInfoLoader: AwsInfoLoader by inject()

    suspend fun apply(job: Job): String {
        val jobDef = jobFactory.find(job.pk)
        val jobService = jobDef.jobClass.newInstance()
        job.awsInfo = awsInfoLoader.load()

        log.info("############ job ${job.pk} / ${job.sk} by ${jobDef.authors} ############")
        job.jobOption?.let { log.info(" -> job option : ${job.jobOption}") }

        try {
            when (jobService) {
                is JobTasklet -> doInnerTasklet(job, jobService)
                //else -> throw IllegalStateException(jobService.javaClass.name + " is not required")
            }
            //실서버 강제호출의 경우 알람 전송
            if (job.awsInfo!!.instanceType == AwsInstanceType.batch) {
                if (job.jobExeFrom == JobExeFrom.ADMIN) {
                    eventBus.post(JobEvent(job, msgs = listOf("강제실행 정상 종료")))
                }
            }
        } catch (e: Throwable) {
            log.error("JOB 에러 => {}", job, e)
            run {
                job.jobStatus = FAILED
                job.jobContext = job.jobContext
                job.jobErrMsg = e.toSimpleString()
                job.endTime = LocalDateTime.now()
                jobRepository.updateItem(job, JobUpdateSet.ERROR)
            }
            eventBus.post(JobEvent(job, err = e))
            throw JobException(e) //예외를 반드시 던져야 한다.
        }
        return JobRoot.OK
    }

    /** 리더 라이터가 없는 태스크릿  */
    @Throws(IOException::class)
    private suspend fun doInnerTasklet(job: Job, jobService: JobTasklet) {
        //==============  RUNNING 마킹 ===================
        job.jobStatus = RUNNING
        job.startTime = LocalDateTime.now()
        jobRepository.updateItem(job, JobUpdateSet.START)
        //==============  싦행  ===================
        jobService.doRun(job)

        //==============  결과 마킹 ===================
        job.jobStatus = SUCCEEDED
        job.endTime = LocalDateTime.now()
        jobRepository.updateItem(job, JobUpdateSet.END)
    }

//    @Throws(IOException::class)
//    private fun doInnerCsvUpload(job: Job, jobService: JobCsvUpload) {
//        val binder = jobService.binder
//        binder.conversionService = conversionService
//
//        //안정적으로 운영하기 위해서 미리 다운받아놓고 진행한다. 스트리밍처리 X
//        val workspace = job.instanceType!!.getWorkspace(this.javaClass.simpleName)
//        val reqFile = File(workspace, job.sk + ".req")
//        s3Client.getObjectAndWrite(AwsS3Data.parse(job.reqFilePath), reqFile)
//        log.info("파일 다운로드 완료 : {} => {}", job.reqFilePath, reqFile.absolutePath)
//
//        //==================================================== itemReader ======================================================
//        val lineDataCsvMapper = CsvMapper { lines: Array<String?>?, lineNumber: Int ->
//            val lineData = JobLineData()
//            val dataBinder = binder.bindWithoutClose(lines, lineNumber)
//            val vo = dataBinder.target
//            lineData.result = vo
//            lineData.job = job
//            val bindingResult = dataBinder.bindingResult
//            bindingResult.allErrors.forEach(Consumer { v: ObjectError ->
//                val e = v as FieldError
//                lineData.addMsg("[{}] : 잘못된 데이터({})가  입력되었습니다.", e.field, e.rejectedValue)
//            })
//
//            //==============  벨리데이션 처리 ===================
//            //필드 미스매핑은 따로 감지하지 않는다.
//            val violations = validator.validate(vo)
//            if (violations.isNotEmpty()) {
//                val results = validationMessageConverter.convert(violations)
//                results.forEach(Consumer { v: ViolationResult -> lineData.addMsg(v.toString()) })
//            }
//            lineData.readSuccess = violations.isEmpty()
//            lineData
//        }
//        val itemReader = CsvItemReader<JobLineData>().linesToSkip(1).resource(FileSystemResource(reqFile))
//            .encoding(JobUtil.OUT_ENCODING).csvMapper(lineDataCsvMapper) // 헤더 부분 제외
//
//
//        //==================================================== itemWriter ======================================================
//        val respFile = File(workspace, job.sk + ".resp")
//        val itemWriter: CsvItemWriter<JobLineData> = object : CsvItemWriter<JobLineData>() {
//            @Throws(Exception::class)
//            override fun write(items: List<JobLineData>) {
//                jobService.doWrite(items)
//                val successCnt =
//                    items.stream().filter { v: JobLineData -> v.readSuccess && v.processSuccess }.count()
//                val failCnt = items.size - successCnt
//                try {
//                    super.write(items)
//                } finally {
//                    //==================================================== 진행상황을 파악하기위한 업데이터 ======================================================
//                    job.rowSuccessCnt = job.rowSuccessCnt ?: 0 + successCnt
//                    job.rowFailCnt = job.rowFailCnt ?: 0 + failCnt
//                    job.updateTime = LocalDateTime.now()
//                    jobRepository.updateItem(Job(job, Job::rowSuccessCnt, Job::rowFailCnt, Job::updateTime))
//                    log.debug("전체 {}건 => 성공/실패 = {}/{}", items.size, successCnt, failCnt)
//                }
//            }
//        }
//        itemWriter.resource(FileSystemResource(respFile)).encoding(JobUtil.OUT_ENCODING)
//        itemWriter.header(ArrayUtils.addAll(binder.headers(), "작업 결과", "실패 코멘트")) //기본 헤더에 추가되는 헤더를 더해준다.
//        itemWriter.csvAggregator { item: JobLineData ->
//            val orgArray = binder.toStringArray(item.result<Any>())
//            val successAll = item.readSuccess && item.processSuccess
//            val success = if (successAll) "성공" else "실패"
//            val msg = if (item.msgs == null) "" else StringUtil.join(item.msgs, ",")
//            ArrayUtils.addAll(orgArray, success, msg)
//        }
//
//        run {
//            //==============  ING 마킹 ===================
//            job.jobStatus = JobStatus.RUNNING
//            job.startTime = LocalDateTime.now()
//            job.rowTotalCnt = itemReader.open().count()
//            job.reqFileSize = StringFormatUtil.toFileSize(reqFile.length())
//            job.rowSuccessCnt = 0L
//            job.rowFailCnt = 0L
//            jobRepository.updateItem(
//                Job(
//                    job,
//                    Job::jobStatus,
//                    Job::startTime,
//                    Job::rowTotalCnt,
//                    Job::reqFileSize,
//                    Job::rowSuccessCnt,
//                    Job::rowFailCnt,
//                    Job::instanceType,
//                    Job::logGroupName,
//                    Job::logStreamName,
//                    Job::awsLambdaFunctionName,
//                    Job::awsBatchJobId,
//                    Job::sfnUuid
//                )
//            )
//        }
//        jobService.open(job)
//        try {
//            //==============  실행 ===================
//            val executor = BatchExecutor.of(this.javaClass.simpleName, jobService.threadCnt)
//            executor.itemReader = itemReader
//            executor.itemWriter = itemWriter
//            executor.commitInterval = jobService.commitInterval
//            executor.execute()
//
//            //업로드
//            val resultFileS3Path = job.reqFilePath + "_result"
//            s3Client.putObject(respFile, AwsS3Data.parse(resultFileS3Path))
//
//            run {
//                //==============  결과 마킹 ===================
//                job.jobStatus = JobStatus.SUCCEEDED
//                job.endTime = LocalDateTime.now()
//                job.resultFilePath = resultFileS3Path
//                jobRepository.updateItem(Job(job, Job::jobStatus, Job::endTime, Job::resultFilePath, Job::jobContext))
//            }
//        } finally {
//            jobService.close(job)
//        }
//    }
}