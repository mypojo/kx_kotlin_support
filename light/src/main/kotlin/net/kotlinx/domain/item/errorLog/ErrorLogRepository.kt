package net.kotlinx.domain.item.errorLog

import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhanced.DbRepository
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.enhancedExp.*
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name

/**
 * DDB 간단접근용 헬퍼
 */
class ErrorLogRepository : DbRepository<ErrorLog>() {

    override val dbTable by koinLazy<DbTable>(ErrorLog::class.name())

    /** 일반 조회 */
    suspend fun find(group: String, div: String, divId: String, block: DbExpression.() -> Unit = {}): DbResult = aws.dynamo.query { findInner(group, div, divId, block) }

    /** 전체 조회 */
    suspend fun findAll(group: String, div: String, divId: String? = null, block: DbExpression.() -> Unit = {}): List<ErrorLog> =
        aws.dynamo.queryAll { findInner(group, div, divId, block) }

    /** 카운트만 조회 */
    suspend fun findCnt(group: String, div: String, divId: String? = null, block: DbExpression.() -> Unit = {}): Int = aws.dynamo.queryCnt { findInner(group, div, divId, block) }

    /** 내부 템플릿 */
    private fun findInner(group: String, div: String, divId: String? = null, block: DbExpression.() -> Unit): DbExpressionSet.SkPrefix = DbExpressionSet.SkPrefix {
        table = dbTable
        pk = ErrorLogConverter.toPk(group, div)
        sk = ErrorLogConverter.toSk(divId)
        block(this)
    }

}