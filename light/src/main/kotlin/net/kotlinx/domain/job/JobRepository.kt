package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.ddb.DbRepository
import net.kotlinx.aws.ddb.DbTable
import net.kotlinx.aws.ddb.exp.*
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.domain.job.define.JobDefinition
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name

/**
 * DDB 간단접근용 헬퍼
 */
class JobRepository(profile: String? = null) : DbRepository<Job>(profile) {

    override val dbTable by koinLazy<DbTable>(Job::class.name())

    //==================================================== 기본 쿼리 ======================================================

    /** 페이징 조회 */
    suspend fun findByPk(jobDef: JobDefinition, block: DbExpression.() -> Unit = {}): DbResult {
        return aws.dynamo.query { findByPkInner(jobDef, block) }
    }

    /** 전체 조회 */
    suspend fun findAllByPk(jobDef: JobDefinition, block: DbExpression.() -> Unit = {}): List<Job> {
        return aws.dynamo.queryAll { findByPkInner(jobDef, block) }
    }

    private fun findByPkInner(jobDef: JobDefinition, block: DbExpression.() -> Unit) = DbExpressionSet.PkSkEq {
        init(Job(jobDef.jobPk))
        block(this)
    }

    //==================================================== GID_STATUS 쿼리 ======================================================

    /**
     * 상태 / PK 기준으로 조회
     * ex) 실패잡 x건
     * 보통 모니터링에 사용됨
     *  */
    suspend fun findByStatusPk(jobStatus: JobStatus, jobDef: JobDefinition? = null, block: DbExpression.() -> Unit = {}): DbResult {
        return aws.dynamo.query { findByStatusPkInner(jobStatus, jobDef, block) }
    }

    /** findByStatusPk 전체 버전 */
    suspend fun findAllByStatusPk(jobStatus: JobStatus, jobDef: JobDefinition? = null, block: DbExpression.() -> Unit = {}): List<Job> {
        return aws.dynamo.queryAll { findByStatusPkInner(jobStatus, jobDef, block) }
    }

    private fun findByStatusPkInner(jobStatus: JobStatus, jobDef: JobDefinition?, block: DbExpression.() -> Unit) = DbExpressionSet.PkSkEq {
        init(dbTable, JobIndexUtil.GID_STATUS)
        pk(Job::jobStatus.name to jobStatus.name)
        jobDef?.let {
            sk(DbTable.PK_NAME to it.jobPk)
        }
        select = Select.AllProjectedAttributes
        block(this)
    }


    //==================================================== LID_MEMBER 쿼리 ======================================================

    /** 특정 사용자의 요청을 요청 최신순으로 조회*/
    suspend fun findByMemberId(jobDef: JobDefinition, memberId: String, block: DbExpression.() -> Unit = {}): DbResult {
        return aws.dynamo.query { findByMemberIdInner(jobDef, memberId, block) }
    }

    /** 특정 사용자의 요청을 요청 최신순으로 조회 -> 전체 */
    suspend fun findAllByMemberId(jobDef: JobDefinition, memberId: String, block: DbExpression.() -> Unit = {}): List<Job> {
        return aws.dynamo.queryAll { findByMemberIdInner(jobDef, memberId, block) }
    }

    private fun findByMemberIdInner(jobDef: JobDefinition, memberId: String, block: DbExpression.() -> Unit) = DbExpressionSet.SkPrefix {
        init(dbTable, JobIndexUtil.LID_MEMBER)
        pk(DbTable.PK_NAME to jobDef.jobPk)
        sk(JobIndexUtil.LID_MEMBER_NAME to "${memberId}#")
        select = Select.AllProjectedAttributes
        block(this)
    }

}