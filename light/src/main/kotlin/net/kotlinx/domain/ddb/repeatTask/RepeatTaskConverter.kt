package net.kotlinx.domain.ddb.repeatTask

import net.kotlinx.aws.dynamo.ddbJoin
import net.kotlinx.aws.dynamo.ddbSplit
import net.kotlinx.domain.ddb.DdbBasic
import net.kotlinx.domain.ddb.DdbBasicConverter
import net.kotlinx.json.gson.GsonData


class RepeatTaskConverter : DdbBasicConverter<DdbBasic, RepeatTask> {

    override val pkPrefix: String = "repeat"
    override val skPrefix: String = "m"

    override fun convertTo(ddb: DdbBasic): RepeatTask = RepeatTask().apply {
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

    override fun convertFrom(item: RepeatTask): DdbBasic {
        return DdbBasic(
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