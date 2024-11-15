package net.kotlinx.domain.job

import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.dynamo.enhanced.DbTable
import java.util.concurrent.TimeUnit

object JobTableUtil {

    fun createDefault(block: DbTable.() -> Unit = {}) = DbTable {
        beforePut = {
            val job = it as Job
            job.ttl = when (AwsInstanceTypeUtil.INSTANCE_TYPE) {
                AwsInstanceType.LOCAL -> DynamoUtil.ttlFromNow(TimeUnit.HOURS, 1)  //로컬은 테스트로 간주하고 1시간 보관
                else -> DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            }
        }
        persist = {
            val job = it as Job
            job.persist
        }
        converter = JobConverter(this)
        block()
    }


}