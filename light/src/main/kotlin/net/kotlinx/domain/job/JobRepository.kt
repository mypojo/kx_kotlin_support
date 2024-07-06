package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.dynamo.*
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.number.ifTrue

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

    //==================================================== 기본 오버라이드 ======================================================

    override suspend fun putItem(job: Job) {
        job.persist.ifTrue { aws.dynamo.putItem(job) }
    }

    override suspend fun updateItem(job: Job, updateKeys: List<String>) {
        job.persist.ifTrue { aws.dynamo.updateItem(job, updateKeys) }
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
            createParamAndQuery = {
                buildMap {
                    put(":${Job::jobStatus.name}", AttributeValue.S(jobStatus.name))
                    jobDef?.let { put(":${DynamoDbBasic.PK}", AttributeValue.S(jobDef.jobPk)) }
                }
            }
            exclusiveStartKey = last
            block()
        }
    }

    /** 페이징 조회 */
    suspend fun findByPk(jobDef: JobDefinition, last: Map<String, AttributeValue>? = null, block: DynamoQuery.() -> Unit = {}): DynamoResult<Job> {
        val param = Job(jobDef.jobPk)
        return aws.dynamo.query(param) {
            createParamAndQuery = {
                buildMap {
                    put(":${DynamoDbBasic.PK}", AttributeValue.S(jobDef.jobPk))
                }
            }
            exclusiveStartKey = last
            block()
        }
    }

    /** 전체 조회 */
    suspend fun findAllByPk(jobDef: JobDefinition, block: DynamoQuery.() -> Unit = {}): List<Job> {
        val dynamoQuery = DynamoQuery {
            createParamAndQuery = {
                buildMap {
                    put(":${DynamoDbBasic.PK}", AttributeValue.S(jobDef.jobPk))
                }
            }
            block()
        }
        return aws.dynamo.queryAll(dynamoQuery, EMPTY)
    }

    /** 특정 사용자의 요청을 요청 최신순으로 조회*/
    suspend fun findByMemberId(jobDef: JobDefinition, memberId: String, last: Map<String, AttributeValue>? = null, block: DynamoQuery.() -> Unit = {}): DynamoResult<Job> {

        val queryExpression = DynamoExpressSet.Query.DynamoExpressSkPrefix(JobIndexUtil.LID_MEMBER, JobIndexUtil.LID_MEMBER_NAME, jobDef.jobPk,"${memberId}#")

        return aws.dynamo.query(EMPTY) {
            indexName = queryExpression.indexName
            select = Select.AllProjectedAttributes
            param = queryExpression.expressionAttributeValues()
            query = queryExpression.expression()
            exclusiveStartKey = last
            block()
        }
    }

}