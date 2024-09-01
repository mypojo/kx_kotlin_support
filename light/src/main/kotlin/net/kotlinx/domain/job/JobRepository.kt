package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.dynamo.*
import net.kotlinx.aws.dynamo.query.*
import net.kotlinx.collection.doUntilTokenNull
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.number.ifTrue
import java.util.concurrent.TimeUnit

/**
 * DDB 간단접근용 헬퍼
 * 필요할때마다 추가.
 *
 * 기존코드 실사용은 일단 안함
 */
class JobRepository(val profile: String? = null) : DynamoRepository<Job> {

    override val aws by koinLazy<AwsClient1>(profile)
    override val emptyData: Job = EMPTY

    companion object {
        /** 쿼리용 빈 객체 */
        val EMPTY: Job = Job("", "")
    }

    /** 디폴트 put 후크 */
    var beforePutHook: (Job) -> Unit = { job ->
        job.ttl = when (AwsInstanceTypeUtil.INSTANCE_TYPE) {
            AwsInstanceType.LOCAL -> DynamoUtil.ttlFromNow(TimeUnit.HOURS, 1)  //로컬은 테스트로 간주하고 1시간 보관
            else -> DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
        }
    }

    //==================================================== 기본 오버라이드 ======================================================

    override suspend fun putItem(job: Job) {
        beforePutHook(job)
        job.persist.ifTrue { aws.dynamo.putItem(job) }
    }

    override suspend fun updateItem(job: Job, updateKeys: List<String>) {
        job.persist.ifTrue { aws.dynamo.updateItem(job, updateKeys) }
    }

    //==================================================== 기본 쿼리 ======================================================

    /** 페이징 조회 */
    suspend fun findByPk(jobDef: JobDefinition, last: Map<String, AttributeValue>? = null, block: DynamoQuery.() -> Unit = {}): DynamoResult<Job> {
        val param = Job(jobDef.jobPk)
        return aws.dynamo.query(param) {
            expression = DynamoExpressionSet.PkSkEq {
                pk = jobDef.jobPk
            }
            exclusiveStartKey = last
            block()
        }
    }

    /** 전체 조회 */
    suspend fun findAllByPk(jobDef: JobDefinition, block: DynamoQuery.() -> Unit = {}): List<Job> {
        val dynamoQuery = DynamoQuery {
            expression = DynamoExpressionSet.PkSkEq {
                pk = jobDef.jobPk
            }
            block()
        }
        return aws.dynamo.queryAll(dynamoQuery, EMPTY)
    }

    //==================================================== 인덱스 쿼리 ======================================================

    /**
     * 상태 / PK 기준으로 조회
     * ex) 실패잡 x건
     * 보통 모니터링에 사용됨
     *  */
    suspend fun findByStatusPk(
        jobStatus: JobStatus,
        jobDef: JobDefinition? = null,
        last: Map<String, AttributeValue>? = null,
        block: DynamoQuery.() -> Unit = {}
    ): DynamoResult<Job> {
        return aws.dynamo.query(EMPTY) {
            indexName = JobIndexUtil.GID_STATUS
            scanIndexForward = false //최근 데이터 우선
            select = Select.AllProjectedAttributes
            expression = DynamoExpressionSet.PkSkEq {
                pkName = Job::jobStatus.name
                pk = jobStatus.name
                jobDef?.let {
                    skName = DynamoBasic.PK
                    sk = it.jobPk
                }
            }
            exclusiveStartKey = last
            block()
        }
    }

    /** findByStatusPk 전체 버전 */
    suspend fun findAllByStatusPk(jobStatus: JobStatus, jobDef: JobDefinition? = null, block: DynamoQuery.() -> Unit = {}): List<Job> {
        return doUntilTokenNull { _, last ->
            val jobs = findByStatusPk(jobStatus, jobDef, last as Map<String, AttributeValue>?) {
                block()
            }
            jobs.datas to jobs.lastEvaluatedKey
        }.flatten()
    }

    /** 특정 사용자의 요청을 요청 최신순으로 조회*/
    suspend fun findByMemberId(jobDef: JobDefinition, memberId: String, last: Map<String, AttributeValue>? = null, block: DynamoQuery.() -> Unit = {}): DynamoResult<Job> {
        return aws.dynamo.query(EMPTY) {
            indexName = JobIndexUtil.LID_MEMBER
            select = Select.AllProjectedAttributes
            expression = DynamoExpressionSet.SkPrefix {
                skName = JobIndexUtil.LID_MEMBER_NAME
                pk = jobDef.jobPk
                sk = "${memberId}#"
            }
            exclusiveStartKey = last
            block()
        }
    }

    /** 특정 사용자의 요청을 요청 최신순으로 조회 -> 전체 */
    suspend fun findAllByMemberId(jobDef: JobDefinition, memberId: String, block: DynamoQuery.() -> Unit = {}): List<Job> {
        return doUntilTokenNull { _, last ->
            val jobs = findByMemberId(jobDef, memberId, last as Map<String, AttributeValue>?) {
                block()
            }
            jobs.datas to jobs.lastEvaluatedKey
        }.flatten()
    }

}