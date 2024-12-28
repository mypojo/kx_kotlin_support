package net.kotlinx.aws.athena.table.cloudtrail

import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.athena.table.AthenaTable
import net.kotlinx.core.Kdsl


class CloudtrailTool {

    @Kdsl
    constructor(block: CloudtrailTool.() -> Unit = {}) {
        apply(block)
    }

    /** 테이블명 */
    lateinit var table: AthenaTable

    /** 모듈  */
    lateinit var athenaModule: AthenaModule

    /** 테이블 삭제 */
    suspend fun dropTable() = athenaModule.execute("DROP TABLE IF EXISTS  ${table.tableNameWithDatabase}")

    /** 스키마 생성 & 파티션 생성 */
    suspend fun create() = athenaModule.execute(ddl())

    /**
     * 파티션 전체 or 부분 업데이트
     * 클라우드 트레일 파티션은 key=value 형식이 아니라서 일단 작업 해준다
     * 아이스버그 마렵다..
     *  */
    suspend fun updatePartition(datas: List<Triple<String, String, String>>) {
        val partition = datas.map { data ->
            """
            PARTITION (year='${data.first}', month='${data.second}', day='${data.third}')
            LOCATION 's3://${table.bucket}/${table.s3Key}${data.first}/${data.second}/${data.third}/'
            """.trimIndent()
        }
        val sql = """
            ALTER TABLE ${table.tableNameWithDatabase} ADD IF NOT EXISTS            
            ${partition.joinToString("\n")}
        """.trimIndent()
        athenaModule.execute(sql)
    }

    /** 테이블 생성 스키마 */
    fun ddl(): String = """
CREATE EXTERNAL TABLE ${table.tableNameWithDatabase} (
    eventVersion STRING,
    userIdentity STRUCT<
        type: STRING,
        principalId: STRING,
        arn: STRING,
        accountId: STRING,
        invokedBy: STRING,
        accessKeyId: STRING,
        userName: STRING,
        sessionContext: STRUCT<
            attributes: STRUCT<
                mfaAuthenticated: STRING,
                creationDate: STRING>,
            sessionIssuer: STRUCT<
                type: STRING,
                principalId: STRING,
                arn: STRING,
                accountId: STRING,
                username: STRING>,
            ec2RoleDelivery: STRING,
            webIdFederationData: MAP<STRING,STRING>>>,
    eventTime STRING,
    eventSource STRING,
    eventName STRING,
    awsRegion STRING,
    sourceIpAddress STRING,
    userAgent STRING,
    errorCode STRING,
    errorMessage STRING,
    requestParameters STRING,
    responseElements STRING,
    additionalEventData STRING,
    requestId STRING,
    eventId STRING,
    resources ARRAY<STRUCT<
        arn: STRING,
        accountId: STRING,
        type: STRING>>,
    eventType STRING,
    apiVersion STRING,
    readOnly STRING,
    recipientAccountId STRING,
    serviceEventDetails STRING,
    sharedEventID STRING,
    vpcEndpointId STRING,
    tlsDetails STRUCT<
        tlsVersion: STRING,
        cipherSuite: STRING,
        clientProvidedHostHeader: STRING>
    )
COMMENT '${table.tableName} table for ${table.bucket} bucket'
PARTITIONED BY (year string, month string, day string)
ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe'
STORED AS INPUTFORMAT 'com.amazon.emr.cloudtrail.CloudTrailInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://${table.bucket}/${table.s3Key}'
TBLPROPERTIES ('classification'='cloudtrail');
    """.trimIndent()


}