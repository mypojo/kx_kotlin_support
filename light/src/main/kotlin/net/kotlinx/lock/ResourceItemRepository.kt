package net.kotlinx.lock

import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhanced.DbRepository
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.enhancedExp.DbExpressionSet
import net.kotlinx.aws.dynamo.enhancedExp.queryAll
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name

/**
 * DDB 간단접근용 헬퍼
 * 필요할때마다 추가.
 *
 * 기존코드 실사용은 일단 안함
 */

class ResourceItemRepository : DbRepository<ResourceItem>() {

    /** 여기서는 안씀 */
    override val dbTable by koinLazy<DbTable>(ResourceItem::class.name())

    /** 사용중 여부 업데이트 */
    fun updateItemInUse(items: List<ResourceItem>, use: Boolean) {
        items.map {
            suspend {
                check(it.inUse != use)
                it.inUse = use
                updateItem(it, UPDATE)
            }
        }.coroutineExecute(20)
    }

    /** 리소스 전체 리스팅  */
    suspend fun findAllByPk(pk: String): List<ResourceItem> {
        return aws.dynamo.queryAll {
            DbExpressionSet.PkSkEq {
                init(ResourceItem(pk, ""))
            }
        }
    }

    companion object {
        /** 업데이트할거 */
        private val UPDATE = listOf(
            ResourceItem::inUse,
            ResourceItem::div,
            ResourceItem::cause,
        ).map { it.name }
    }

}