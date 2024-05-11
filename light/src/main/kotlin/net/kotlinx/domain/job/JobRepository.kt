package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.dynamo.*
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.number.ifTrue
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * DDB 간단접근용 헬퍼
 * 필요할때마다 추가.
 *
 * 기존코드 실사용은 일단 안함
 */
class JobRepository : KoinComponent {

    private val aws1: AwsClient1 by inject()

    //==================================================== 기본 오버라이드 ======================================================

    suspend fun putItem(job: Job) = job.persist.ifTrue { aws1.dynamo.putItem(job) }
    suspend fun updateItem(job: Job, updateKeys: List<String>) = job.persist.ifTrue { aws1.dynamo.updateItem(job, updateKeys) }
    suspend fun getItem(data: Job): Job? = aws1.dynamo.getItem(data)

    //==================================================== 인덱스 쿼리 ======================================================

    /** 최근 잡 확인용 임시 메소드  */
    suspend fun findLastJobs(jobDef: JobDefinition, jobStatus: JobStatus, block: DynamoQuery.() -> Unit = {}): List<Job> {
        val param = Job(jobDef.jobPk) {
            this.jobStatus = jobStatus
        }
        return aws1.dynamo.query(param) {
            indexName = JobIndexUtil.GID_STATUS
            scanIndexForward = false //최근 데이터 우선
            select = Select.AllProjectedAttributes
            limit = 10
            createParamAndQuery = {
                val job = it as Job
                mapOf(
                    ":${DynamoDbBasic.PK}" to AttributeValue.S(job.pk),
                    ":${Job::jobStatus.name}" to AttributeValue.S(job.jobStatus.name)
                )
            }
            block()
        }
    }


//
//    /**
//     * 인덱스에는 키값만 존재한다.
//     */
//    fun findAllByTaskStatus(jobStatus: JobStatus): List<Job> {
//        val query = QueryConditional.keyEqualTo(Key.builder().partitionValue(jobStatus.name).build())
//        val params = dynamoClient!!.queryAll(Job::class.java, JobConfig.INDEX_STATUS, query) //키값만 먼저 가져온다음 처리
//        log.debug("key load ${params.size}")
//        return dynamoClient.getItem(params)
//    }
//
//    fun findAllByTaskStatusPage(jobStatus: JobStatus, pageSize: Int, lastEvaluatedKey: Map<String?, AttributeValue?>?): Page<Job> {
//        val query = QueryConditional.keyEqualTo(Key.builder().partitionValue(jobStatus.name).build())
//        val req = QueryEnhancedRequest.builder().queryConditional(query).limit(pageSize).scanIndexForward(false).exclusiveStartKey(lastEvaluatedKey).build()
//        val pageIt = dynamoClient!!.map(Job::class.java, JobConfig.INDEX_STATUS).query(req)
//        return CollectionUtil.getFirst(pageIt)
//    }
//
//    /** 회원 ID로 가져옴. UI view 용  */
//    fun findByMemberId(
//        jobDef: JobDefinition,
//        memberId: String?,
//        pageSize: Int,
//        lastEvaluatedKey: Map<String?, AttributeValue?>?
//    ): Page<Job> {
//        val param = Job(jobDef.jobPk, null)
//        val query = QueryConditional.sortBeginsWith(Key.builder().partitionValue(param.pk).sortValue(memberId).build())
//        val req = QueryEnhancedRequest.builder().queryConditional(query).limit(pageSize).scanIndexForward(false)
//            .exclusiveStartKey(lastEvaluatedKey).build()
//        val pageIt = dynamoClient!!.map(
//            Job::class.java, JobConfig.INDEX_MEMBER
//        ).query(req)
//        val first = CollectionUtil.getFirst(pageIt)
//        val params = first.items()
//        val jobs = dynamoClient.getItem(params)
//        return Page.create(jobs, first.lastEvaluatedKey())
//    }
//    //==================================================== 응용 ======================================================
//    /** 테스트로 넣은거 정리 -> 확인 한번 후 삭제할것  */
//    fun removeAllInvalidTask() {
//        val all = dynamoClient!!.findAll(Job::class.java)
//        val jobs = Lists.newArrayList(all)
//        log.info("전체 job : {}", jobs.size)
//        val invalidTasks = jobs.stream().filter { v: Job -> v.expireTime != null }.collect(Collectors.toList())
//        dynamoClient.deleteItem(invalidTasks)
//    }


}