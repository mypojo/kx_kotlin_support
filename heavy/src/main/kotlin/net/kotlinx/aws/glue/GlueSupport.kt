package net.kotlinx.aws.glue

import aws.sdk.kotlin.services.glue.GlueClient
import aws.sdk.kotlin.services.glue.createDatabase
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.glue: GlueClient
    get() = getOrCreateClient { GlueClient { awsConfig.build(this) }.regist(awsConfig) }

/** 일반 데이터베이스 생성 */
suspend fun GlueClient.createDatabase(databaseName: String, desc: String? = null) {
    this.createDatabase {
        this.databaseInput {
            this.name = databaseName
            this.description = desc
        }
    }
}

/**
 * 데이터베이스 링크 생성
 * 다른데서 내 계정으로 쉐어(RAM)한 데이터베이스를 내가 athena 등에서 읽을수 있게 DB링크로 만들어줄때 사용
 * 이렇게 링크 생성하면, athena 에서 database 로 보임
 * @param fromdatabaseName to_${projectName}
 *  */
suspend fun GlueClient.createDatabaseLink(databaseName: String, fromAwsId: String, fromdatabaseName: String) {
    this.createDatabase {
        this.databaseInput {
            this.name = databaseName
            //링크에는 desc를 넣을 수 없음
            this.targetDatabase {
                this.catalogId = fromAwsId
                this.databaseName = fromdatabaseName
            }
        }
    }
}