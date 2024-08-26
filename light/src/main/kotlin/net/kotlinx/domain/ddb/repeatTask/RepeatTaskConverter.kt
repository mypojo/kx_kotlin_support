package net.kotlinx.domain.ddb.repeatTask

import com.lectra.koson.obj
import net.kotlinx.aws.dynamo.ddbJoin
import net.kotlinx.aws.dynamo.ddbSplit
import net.kotlinx.domain.ddb.DdbBasic
import net.kotlinx.domain.ddb.DdbBasicConverter
import net.kotlinx.json.koson.toGsonData


class RepeatTaskConverter : DdbBasicConverter<DdbBasic, RepeatTask> {

    override val pkPrefix: String = "repeat"
    override val skPrefix: String = "m"

    override fun createBasic(data: RepeatTask) = DdbBasic(
        pk = arrayOf(pkPrefix, data.group, data.div).ddbJoin(),
        sk = arrayOf(skPrefix, data.memberId, data.id).ddbJoin(3),
    )

    override fun convertTo(data: DdbBasic): RepeatTask = RepeatTask().apply {
        //==================================================== PK ======================================================
        val pks = data.pk.ddbSplit()
        check(pks.size == 3)
        group = pks[1]
        div = pks[2]

        val sks = data.sk.ddbSplit()
        check(sks.size == 3)
        memberId = sks[1]
        id = sks[2]

        //==================================================== 기본값 ======================================================
        ttl = data.ttl
        time = data.body[this::time.name].str!!
    }

    override fun convertFrom(task: RepeatTask): DdbBasic {

        return createBasic(task).apply {

            //==================================================== 기본값 ======================================================

            ttl = task.ttl

            body = obj {
                RepeatTask::time.name to task.time
            }.toGsonData()

            //==================================================== 인덱스 구성 ======================================================

            /** 시간대별로 조회 */
            gsi01 = arrayOf(pkPrefix, task.time).ddbJoin() to arrayOf(pkPrefix, task.group, task.div, task.memberId, task.id).ddbJoin()
        }

    }


}