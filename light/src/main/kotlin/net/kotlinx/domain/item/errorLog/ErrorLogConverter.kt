package net.kotlinx.domain.item.errorLog

import net.kotlinx.aws.dynamo.ddbJoin
import net.kotlinx.aws.dynamo.ddbSplit
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexEachConverter
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexItem
import net.kotlinx.json.gson.GsonData
import net.kotlinx.string.toLocalDateTime
import net.kotlinx.time.TimeFormat


class ErrorLogConverter : DbMultiIndexEachConverter<DbMultiIndexItem, ErrorLog> {

    companion object {
        const val PK_PREFIX: String = "errorLog"
        const val SK_PREFIX: String = "id"
    }

    override val pkPrefix: String = PK_PREFIX

    override val skPrefix: String = SK_PREFIX

    override fun convertTo(ddb: DbMultiIndexItem): ErrorLog = ErrorLog().apply {
        //==================================================== PK ======================================================
        val pks = ddb.pk.ddbSplit()
        check(pks.size == 3)
        group = pks[1]
        div = pks[2]

        val sks = ddb.sk.ddbSplit()
        check(sks.size == 3)
        divId = sks[1]
        id = sks[2]

        //==================================================== 기본값 ======================================================
        ttl = ddb.ttl
        time = ddb.body.remove(this::time.name)!!.str!!.toLocalDateTime()
        cause = ddb.body.remove(this::cause.name)!!.str
        stackTrace = ddb.body.remove(this::stackTrace.name)!!.str
    }

    override fun convertFrom(item: ErrorLog): DbMultiIndexItem {
        return DbMultiIndexItem(
            pk = arrayOf(pkPrefix, item.group, item.div).ddbJoin(),
            sk = arrayOf(skPrefix, item.divId, item.id).ddbJoin(3),
        ).apply {

            //==================================================== 기본값 ======================================================
            ttl = item.ttl
            body = GsonData.obj().apply {
                put(ErrorLog::time.name, item.time?.let { TimeFormat.YMDHMS[it] })
                put(ErrorLog::cause.name, item.cause)
                put(ErrorLog::stackTrace.name, item.stackTrace)
            }
        }

    }


}