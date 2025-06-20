package net.kotlinx.domain.item.repeatTask

import net.kotlinx.aws.dynamo.ddbJoin
import net.kotlinx.aws.dynamo.ddbSplit
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexEachConverter
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexItem
import net.kotlinx.json.gson.GsonData

@Deprecated("AWS 스케쥴러로 대체")
class RepeatTaskConverter : DbMultiIndexEachConverter<DbMultiIndexItem, RepeatTask> {

    override val pkPrefix: String = "repeat"

    /** member */
    override val skPrefix: String = "m"

    override fun convertTo(ddb: DbMultiIndexItem): RepeatTask = RepeatTask().apply {
        //==================================================== PK ======================================================
        val pks = ddb.pk.ddbSplit()
        check(pks.size == 3)
        group = pks[1]
        div = pks[2]

        val sks = ddb.sk.ddbSplit()
        check(sks.size == 3)
        memberId = sks[1]
        id = sks[2]

        //==================================================== 기본값 ======================================================
        ttl = ddb.ttl
        time = ddb.body.remove(this::time.name)!!.str!!
        body = ddb.body
    }

    override fun convertFrom(item: RepeatTask): DbMultiIndexItem {
        return DbMultiIndexItem(
            pk = arrayOf(pkPrefix, item.group, item.div).ddbJoin(),
            sk = arrayOf(skPrefix, item.memberId, item.id).ddbJoin(3),
        ).apply {

            //==================================================== 기본값 ======================================================
            ttl = item.ttl
            body = GsonData.obj().apply {
                putAll(item.body)
                put(RepeatTask::time.name, item.time)
            }

            //==================================================== 인덱스 구성 (ddb에 write 할때만 작업하면됨) ======================================================

            /** 시간대별로 조회 */
            gsi01 = arrayOf(pkPrefix, item.time).ddbJoin() to arrayOf(skPrefix, item.group, item.div, item.memberId, item.id).ddbJoin(5)
        }

    }


}