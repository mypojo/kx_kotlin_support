package net.kotlinx.domain.item.errorLog

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.ddbJoin
import net.kotlinx.aws.dynamo.ddbSplit
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.aws.dynamo.put

/**
 * DDB에 입력되는 메타데이터
 */
class ErrorLogConverter(private val table: DbTable) : DbConverter<ErrorLog> {

    companion object {
        const val PK_PREFIX: String = "errorLog"
        const val SK_PREFIX: String = "id"

        fun toPk(group: String, div: String): String = arrayOf(PK_PREFIX, group, div).ddbJoin()
        fun toSk(divId: String?, id: String? = null): String = arrayOf(SK_PREFIX, divId, id).ddbJoin(3)

    }

    override fun toAttribute(item: ErrorLog): Map<String, AttributeValue> {
        return buildMap {
            put(table.pkName, item.pk)
            put(table.skName, item.sk)
            put(ErrorLog::ttl.name, item.ttl)
            put(ErrorLog::time.name, item.time)
            put(ErrorLog::cause.name, item.cause)
            put(ErrorLog::stackTrace.name, item.stackTrace)

        }
    }

    override fun fromAttributeMap(map: Map<String, AttributeValue>): ErrorLog {
        val (_, group, div) = map[table.pkName]!!.asS().ddbSplit()
        val (_, divId, id) = map[table.skName]!!.asS().ddbSplit()
        return ErrorLog(
            group = group,
            div = div,
            divId = divId,
            id = id,
            ttl = map.findOrThrow(ErrorLog::ttl),
            time = map.findOrThrow(ErrorLog::time),
            cause = map.findOrThrow(ErrorLog::cause),
            stackTrace = map.findOrThrow(ErrorLog::stackTrace),
        )
    }


}