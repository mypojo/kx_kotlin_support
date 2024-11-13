package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.ddb.DbTable
import net.kotlinx.domain.ddb.DbMultiIndexItem
import net.kotlinx.domain.ddb.errorLog.ErrorLogConverter
import net.kotlinx.domain.job.Job
import net.kotlinx.koin.Koins.koin
import net.kotlinx.reflect.name
import net.kotlinx.string.encodeUrl


/**
 * 에러 링크
 * */
fun Job.toErrorLogLink(): String {
    val config = koin<AwsConfig>()
    val pk = "${ErrorLogConverter.PK_PREFIX}#job#${pk}"
    val sk = "${ErrorLogConverter.SK_PREFIX}#${sk}#"

    val table = koin<DbTable>(DbMultiIndexItem::class.name())
    return "https://${config.region}.console.aws.amazon.com/dynamodbv2/home?region=${config.region}#item-explorer?maximize=true&operation=QUERY&pk=${pk.encodeUrl()}&sk=${sk.encodeUrl()}&skComparator=BEGINS_WITH&table=${table.tableName}"
}