package net.kotlinx.aws.rds

import aws.sdk.kotlin.services.rdsdata.executeStatement
import aws.sdk.kotlin.services.rdsdata.model.ExecuteStatementResponse
import aws.sdk.kotlin.services.rdsdata.model.Field
import aws.sdk.kotlin.services.rdsdata.model.SqlParameter
import net.kotlinx.aws.AwsClient
import net.kotlinx.core.Kdsl

/**
 *
 * 아래 깃 참고
 * https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/kotlin/usecases/serverless_rds
 * */
class AwsDbClient {

    @Kdsl
    constructor(block: AwsDbClient.() -> Unit = {}) {
        apply(block)
    }

    lateinit var aws: AwsClient

    /**
     * DB 리소스 명
     * ex) main-prod
     *  */
    lateinit var resourceName: String

    /**
     * DB 리소스의 DB 이름
     * ex) ${projectName}-dev
     *  */
    lateinit var databaseName: String

    /**
     * 시크릿 매니저 명
     * IAM은 아직 안되는듯..
     *  */
    lateinit var secretManagerName: String

    //==================================================== 내부 사용 ======================================================

    private val resourceArn: String by lazy { "arn:aws:rds:${aws.awsConfig.region}:${aws.awsConfig.awsId}:cluster:${resourceName}" }

    private val secretArn: String by lazy { "arn:aws:secretsmanager:${aws.awsConfig.region}:${aws.awsConfig.awsId}:secret:${secretManagerName}" }

    /**
     * 쿼리, 업데이트 등등 이거 하나로 다 같이 사용
     * 트랜잭션은 별도임
     * */
    suspend fun executeStatement(sql: String, param: Map<String, *> = emptyMap<String, Any>()): ExecuteStatementResponse {
        return aws.rdsData.executeStatement {
            this.secretArn = this@AwsDbClient.secretArn
            this.sql = sql
            this.database = databaseName
            this.resourceArn = this@AwsDbClient.resourceArn
            this.includeResultMetadata = false //디폴트로 메타데이터 안줌
            //formatRecordsAs =RecordsFormatType.Json
            this.parameters = param.map {
                SqlParameter {
                    name = it.key
                    value = when (it.value) {
                        is Long -> Field.LongValue(it.value as Long)
                        is Double -> Field.DoubleValue(it.value as Double)
                        is Boolean -> Field.BooleanValue(it.value as Boolean)
                        else -> Field.StringValue(it.value.toString())
                    }
                }
            }
        }
    }


}