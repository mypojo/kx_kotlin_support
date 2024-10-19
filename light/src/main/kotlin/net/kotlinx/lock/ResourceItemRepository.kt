package net.kotlinx.lock

import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.dynamo.DynamoRepository
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.query.DynamoExpressionSet
import net.kotlinx.aws.dynamo.query.DynamoQuery
import net.kotlinx.aws.dynamo.query.queryAll
import net.kotlinx.concurrent.coroutineExecute

/**
 * DDB 간단접근용 헬퍼
 * 필요할때마다 추가.
 *
 * 기존코드 실사용은 일단 안함
 */
class ResourceItemRepository(override val aws: AwsClient) : DynamoRepository<ResourceItem> {

    override val emptyData: ResourceItem = ResourceItem("", "")

    /** 사용중 여부 업데이트 */
    suspend fun updateItemInUse(items: List<ResourceItem>, use: Boolean) {
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
        //xxxx
        val query = DynamoQuery {
            expression = DynamoExpressionSet.PkSkEq {
                this.pk = pk
            }
        }
        return aws.dynamo.queryAll(query, ResourceItem(pk, ""))
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