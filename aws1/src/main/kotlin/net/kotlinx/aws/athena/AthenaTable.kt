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
    lateinit var athenaTablePartitionType: AthenaTablePartitionType

    /** 포맷 */
    var athenaTableFormat: AthenaTableFormat = AthenaTableFormat.Json

    /** 테이블명  */
    lateinit var tableName: String

    /** 위치 */
    lateinit var location: String

    /** 파티션 정보 */
    var partition: Map<String, String> = emptyMap()

    /** 테이블 프로퍼티 */
    var props: MutableMap<String, String> = mutableMapOf()

    init {
        block(this)
    }

    fun drop(): String = "DROP TABLE IF EXISTS ${tableName};"

    fun create(): String {

        //속성 추가
        when (athenaTablePartitionType) {
            // https://docs.aws.amazon.com/ko_kr/athena/latest/ug/partition-projection-supported-types.html
            // 프로젝션에 date 사용하는거 추가해야함
            AthenaTablePartitionType.Projection -> {
                props["projection.enabled"] = "true"
                partition.keys.forEach { //S3 경로임으로 타입은 무시..
                    props["projection.${it}.type"] = "injected"
                }
                props["storage.location.template"] = "${location}${partition.keys.joinToString("/") { "\${${it}}" }}/"
            }

            else -> {}
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


}

