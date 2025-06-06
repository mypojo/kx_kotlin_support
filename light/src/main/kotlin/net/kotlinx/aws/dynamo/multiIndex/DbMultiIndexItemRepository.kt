package net.kotlinx.aws.dynamo.multiIndex

import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhanced.DbRepository
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.enhancedExp.*
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name


class DbMultiIndexItemRepository : DbRepository<DbMultiIndexItem>() {

    override val dbTable by koinLazy<DbTable>(DbMultiIndexItem::class.name())

    /** 페이징 조회 */
    suspend fun findBySkPrefix(item: DbMultiIndexItem, index: DbMultiIndex? = null, block: DbExpression.() -> Unit = {}): DbResult {
        return aws.dynamo.query { findBySkPrefixInner(item, index, block) }
    }

    /** 전체 조회 */
    suspend fun findAllBySkPrefix(item: DbMultiIndexItem, index: DbMultiIndex? = null, block: DbExpression.() -> Unit = {}): List<DbMultiIndexItem> {
        return aws.dynamo.queryAll { findBySkPrefixInner(item, index, block) }
    }

    /** 카운트만 조회 */
    suspend fun findCntBySkPrefix(item: DbMultiIndexItem, index: DbMultiIndex? = null, block: DbExpression.() -> Unit = {}): Int {
        return aws.dynamo.queryCnt { findBySkPrefixInner(item, index, block) }
    }

    private fun findBySkPrefixInner(item: DbMultiIndexItem, index: DbMultiIndex?, block: DbExpression.() -> Unit) = DbExpressionSet.SkPrefix {
        when (index) {
            null -> {
                init(item)
            }

            else -> {
                init(item.table(), index.indexName)
                pkName = index.pkName
                skName = index.skName
                val indexValue = item.indexValue(index)!!
                pk = indexValue.first
                sk = indexValue.second
                select = Select.AllProjectedAttributes  //기본키값만 조회
            }
        }
        block(this)
    }


}