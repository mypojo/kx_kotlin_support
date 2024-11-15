package net.kotlinx.domain.item.errorLog

import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndexItem
import net.kotlinx.domain.job.Job
import net.kotlinx.koin.Koins.koin
import net.kotlinx.reflect.name

/** 에러 로그 파라메터로 변환 */
val Job.toErrorLogParam: ErrorLog
    get() = ErrorLog {
        group = Job::class.name()
        div = pk
        divId = sk
    }


/** 에러 로그를 쿼리하는 DDB 링크 */
val Job.errorLogQueryLink: String
    get() {
        val pk = "${ErrorLogConverter.PK_PREFIX}#job#${pk}"
        val sk = "${ErrorLogConverter.SK_PREFIX}#${sk}#"
        val table = koin<DbTable>(DbMultiIndexItem::class.name())
        return DynamoUtil.toItemQuery(table.tableName, pk, sk, table.region)
    }