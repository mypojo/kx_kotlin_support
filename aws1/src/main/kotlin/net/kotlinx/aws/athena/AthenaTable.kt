package net.kotlinx.aws.athena


/**
 * 아테나 테이블 스키마 생성기
 * 느리고 불안한 CDK보다 이게 더 나은 판단인거 같다.
 * 후회하지 않는다... ㅠㅠ
 *
 * 네이밍 컨벤션 = mysql 표준인 소문자 언더스코어로 통일한다.
 *  */
class AthenaTable(block: AthenaTable.() -> Unit = {}) {

    /**
     * 스키마
     * 기본적으로 언더스코어 구조이지만,
     * 예외적으로 json 입력 기본 스키마는 카멜케이스로 구성한다 (어차피 AWS가 시작을 잘못잡아서 엉망임)
     *
     * string
     * int / bigint
     * timestamp
     * */
    lateinit var schema: Map<String, Any>

    /** 파티션 타입 */
    var athenaTablePartitionType: AthenaTablePartitionType = AthenaTablePartitionType.None

    /** 포맷 */
    var athenaTableFormat: AthenaTableFormat = AthenaTableFormat.Json

    /** 테이블명  */
    lateinit var tableName: String


    /** 버킷 명 */
    lateinit var bucket: String

    /**
     * 테이블 시작점의 S3 key
     * 폴더형식 이여야 한다 ( / 로 끝나야함)
     * ex) collect/member/
     *  */
    lateinit var s3Key: String

    /** 파티션 정보 */
    var partition: Map<String, String> = emptyMap()

    /** 헤더가 있으면 true로 지정할것 */
    var skipHeader: Boolean = false

    /**
     * 테이블 프로퍼티
     *    'classification'='parquet'
     *    'has_encrypted_data'='false'
     *    이런것들
     *  */
    var props: Map<String, String> = emptyMap()

    /** 테이블 생성시에는 필요없음. 권한 부여용 */
    var database: String = ""

    init {
        block(this)
    }

    fun drop(): String = "DROP TABLE IF EXISTS ${tableName};"

    fun create(): String {

        val location = "s3://${bucket}/${s3Key}"
        //속성 추가
        when (athenaTablePartitionType) {
            // https://docs.aws.amazon.com/ko_kr/athena/latest/ug/partition-projection-supported-types.html
            // 프로젝션에 date 사용하는거 추가해야함
            // 프로젝션의 경우 basic_date=20210101  이런식으로 하지 않음 ( firehose 예제 참고)
            AthenaTablePartitionType.Projection -> {
                props = props + mapOf(
                    "projection.enabled" to "true",
                    "storage.location.template" to "${location}${partition.keys.joinToString("/") { "\${${it}}" }}/",
                    *partition.keys.map { "projection.${it}.type" to "injected" }.toTypedArray(),
                )

            }

            else -> {}
        }

        if (skipHeader) {
            props = props + mapOf("skip.header.line.count" to "1") //헤더 스킵 정보
        }

        val partitionText = when {
            athenaTablePartitionType == AthenaTablePartitionType.Index && partition.isNotEmpty() -> {
                "PARTITIONED BY (${partition.map { "${it.key} ${it.value}" }.joinToString(",")})"
            }

            else -> ""
        }

        val propsText = when {
            props.isEmpty() -> ""
            else -> "TBLPROPERTIES ( ${
                props.map { "'${it.key}' = '${it.value}'" }.joinToString(",")
            } )" //'storage.location.template' = 's3://sin-work-dev/upload/sfnBatchModuleOutput/$\\{sfnId}'
        }

        val schemaText = schema.map { "`${it.key}` ${toSchema(it.value)}" }.joinToString(",") //뎁스에 따라 표현 방식이 틀려진다.
        return """            
CREATE EXTERNAL TABLE ${tableName}( $schemaText  )
$partitionText
${athenaTableFormat.define}
LOCATION  '${location}'
$propsText
            ;
        """.trimIndent()
    }

    private fun toSchema(value: Any?): String = when (value) {

        null -> throw NullPointerException()

        is Map<*, *> -> "STRUCT< ${value.entries.joinToString(",") { toSchema(it) }} >"

        is List<*> -> "ARRAY<STRUCT< ${value.joinToString(",") { toSchema(it) }} >>"

        //is Map.Entry<*, *> -> "`${value.key}` ${toSchema(value.value!!)}"
        is Map.Entry<*, *> -> "${value.key}:${toSchema(value.value!!)}"
        is Pair<*, *> -> "${value.first}:${toSchema(value.second!!)}"

        is CharSequence -> value.toString()

        else -> throw IllegalArgumentException("${value::class} is not required!")
    }

    //==================================================== athena type 입력용 (하드코딩 방지) ======================================================
    //https://docs.aws.amazon.com/ko_kr/athena/latest/ug/data-types.html

    val boolean = "boolean"
    val string = "string"

    /** YYYY-MM-DD HH:MM:SS.SSS */
    val timestamp = "timestamp"

    val tinyint = "tinyint"
    val int = "int"
    val bigint = "bigint"

    val float = "float"
    val double = "double"

}

