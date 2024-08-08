package net.kotlinx.aws.athena

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.s3.deleteDir
import net.kotlinx.aws.s3.putObject
import net.kotlinx.collection.toQueryString
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins
import java.io.File
import kotlin.time.Duration.Companion.days


/**
 * 아테나 테이블 스키마 생성기
 *
 * Ktorm 이나 exposed 써보려고 했으나 불가능해서 포기
 *
 * 느리고 불안한 CDK보다 이게 더 나은 판단이라고 생각함
 *
 * 네이밍 컨벤션 = mysql 표준인 소문자 언더스코어로 통일한다.
 *  */
class AthenaTable {

    @Kdsl
    constructor(block: AthenaTable.() -> Unit = {}) {
        apply(block)
    }

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
    var athenaTablePartitionType: AthenaTablePartitionType = AthenaTablePartitionType.NONE

    /** 테이블 타입 */
    var athenaTableType: AthenaTableType = AthenaTableType.EXTERNAL

    /** 포맷 */
    var athenaTableFormat: AthenaTableFormat = AthenaTableFormat.Parquet

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

    /**
     * 파티션 정보
     * ice버그의 경우 value에 함수형을 입력할 수 있음
     *  -> https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html 참고
     *  */
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

    /** ION에서 강제로 falt 하게 지정할 path */
    var ionFlatPath: String? = "Item"

    /** 테이블 생성시에는 대부분 필요없음 (기본 스키마) */
    var database: String = ""

    /** 데이터베이스를 포함한 테이블명  */
    val tableNameWithDatabase: String
        get() = if (database.isEmpty()) tableName else "${database}.${tableName}"

    //==================================================== 간단설정 ======================================================
    fun icebugTable() {
        athenaTableFormat = AthenaTableFormat.Iceberg
        athenaTableType = AthenaTableType.INTERNAL
        athenaTablePartitionType = AthenaTablePartitionType.INDEX
    }

    //==================================================== 아이스버그 전용 ======================================================
    /**
     * 보존 기한 이후의 삭제처리된 데이터를 실제로 삭제한다.
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-data-optimization.html
     * */
    fun icebugDoVacuum() {
        check(athenaTableFormat == AthenaTableFormat.Iceberg)
        val athenaModule = Koins.koin<AthenaModule>()
        athenaModule.execute("VACUUM $tableName")
    }

    /** 레이아웃을 최적화해서 재구성한다 */
    fun icebugDoOptimize() {
        check(athenaTableFormat == AthenaTableFormat.Iceberg)
        val athenaModule = Koins.koin<AthenaModule>()
        athenaModule.execute("OPTIMIZE $tableName REWRITE DATA USING BIN_PACK  where 1=1;")
    }


    //==================================================== 데이터 편집 ======================================================

    /** 해당 파티션 경로의 디렉토리를 삭제한다. */
    suspend fun deleteaData(vararg partitionValue: String) {
        val dataPath = getDataPath(partitionValue)
        val aws = Koins.koin<AwsClient1>()
        log.debug { " -> 테이블 $tableName 데이터 삭제 : $bucket $dataPath" }
        aws.s3.deleteDir(bucket, dataPath)
    }

    /** 데이터 경로 가져오기 */
    private fun getDataPath(partitionValue: Array<out String>): String {
        check(athenaTableType == AthenaTableType.EXTERNAL) { "athenaTableType EXTERNAL 테이블만 지원합니다." }
        val dataPath = when (athenaTablePartitionType) {
            AthenaTablePartitionType.PROJECTION -> {
                check(partitionValue.isNotEmpty())
                "${s3Key}${partitionValue.joinToString("/")}/"
            }

            AthenaTablePartitionType.INDEX -> {
                check(partitionValue.isNotEmpty())
                val pathKeys = partition.keys.take(partitionValue.size)
                val pathMap = pathKeys.mapIndexed { index, s -> s to partitionValue[index] }.toMap()
                "${s3Key}${pathMap.toQueryString("/")}/"
            }

            AthenaTablePartitionType.NONE -> {
                check(partitionValue.isEmpty())
                s3Key
            }
        }
        return dataPath
    }

    suspend fun insertData(file: File, vararg partitionValue: String) {
        val dataPath = getDataPath(partitionValue)

        val aws = Koins.koin<AwsClient1>()
        log.debug { " -> 테이블 $tableName 데이터 업로드 : $bucket $dataPath" }
        aws.s3.putObject(bucket, "${dataPath}${file.name}", file)
    }

    /** 삭제 후 입력 */
    suspend fun deleteAndinsertData(file: File, vararg partitionValue: String) {
        deleteaData(*partitionValue)
        insertData(file, *partitionValue)
    }


    //==================================================== 스키마 ======================================================

    fun drop(): String {
        check(athenaTableType == AthenaTableType.EXTERNAL) { "EXTERNAL 테이블이 아니라면 직접 drop 해주세요 (위험합니다)" }
        return dropForce()
    }

    fun dropForce(): String = "DROP TABLE IF EXISTS ${tableNameWithDatabase};"

    fun create(): String {

        val location = "s3://${bucket}/${s3Key}"

        if (skipHeader) {
            props = props + mapOf("skip.header.line.count" to "1") //헤더 스킵 정보
        }

        //속성 추가
        when (athenaTablePartitionType) {
            // https://docs.aws.amazon.com/ko_kr/athena/latest/ug/partition-projection-supported-types.html
            // 프로젝션에 date 사용하는거 추가해야함
            // 프로젝션의 경우 basic_date=20210101  이런식으로 하지 않음 ( firehose 예제 참고)
            AthenaTablePartitionType.PROJECTION -> {
                props = props + mapOf(
                    "projection.enabled" to "true",
                    "storage.location.template" to "${location}${partition.keys.joinToString("/") { "\${${it}}" }}/",
                    *partition.keys.map { "projection.${it}.type" to "injected" }.toTypedArray(),
                )

            }

            AthenaTablePartitionType.INDEX -> {}
            AthenaTablePartitionType.NONE -> {}
        }

        /**
         * 아이스버그인경우라도, 수동으로 스키마 입력해야함 -> 컬럼 정의와 PK 정의(버킷팅 등)가 틀릴 수 있음
         * */
        val schemaText = schema.map { "    `${it.key}` ${toSchema(it.value)}" }.joinToString(",\n")

        val partitionText = when (athenaTableFormat) {
            /**
             * 아이스버그의 경우 일반 컬럼에 파티션 데이터가 있어야 한다.
             * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html
             * */
            AthenaTableFormat.Iceberg -> if (partition.isEmpty()) "" else "PARTITIONED BY (${partition.map { it.value }.joinToString(",")})"

            else -> if (partition.isEmpty()) "" else "PARTITIONED BY (${partition.map { "${it.key} ${it.value}" }.joinToString(",")})"
        }

//        val partitionText = when (athenaTablePartitionType) {
//            AthenaTablePartitionType.INDEX -> {
//                when (athenaTableFormat) {
//                    /** 아이스버그의 경우 일반 컬럼에 파티션 데이터가 있어야 한다. */
//                    AthenaTableFormat.Iceberg -> if (partition.isEmpty()) "" else "PARTITIONED BY (${partition.map { "${it.value}" }.joinToString(",")})"
//
//                    else -> if (partition.isEmpty()) "" else "PARTITIONED BY (${partition.map { "${it.key} ${it.value}" }.joinToString(",")})"
//                }
//            }
//
//            else -> ""
//        }

        //==================================================== 포맷정보 ======================================================
        val formatText = athenaTableFormat.toRowFormat(this).joinToString("\n")
        when (athenaTableFormat) {
            is AthenaTableFormat.Iceberg -> {
                props = props + mapOf(
                    //https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html
                    "table_type" to "ICEBERG",
                    "optimize_rewrite_delete_file_threshold" to "5", //임계값보다 적으면 파일이 재작성되지 않음
                    "vacuum_max_snapshot_age_seconds" to "${14.days.inWholeSeconds}", //vacuum 명령으로 몇일치 삭제데이터의 마커만 남기고 다 삭제할지? 기본값은 5일 -> 2주로 수정
                    //이하 설정은 일단 기본값 사용함.
                )
            }

            else -> {} //아무것도 안함
        }

        val propsText = when {
            props.isEmpty() -> ""
            else -> "TBLPROPERTIES ( ${props.map { "   '${it.key}' = '${it.value}'" }.joinToString(",")} )"
        }

        return listOf(
            "CREATE ${athenaTableType.schema} TABLE ${tableNameWithDatabase}(",
            schemaText,
            ")",
            partitionText,
            formatText,
            "LOCATION  '${location}'",
            propsText,
            ";",
        ).joinToString("\n")
    }

    private fun toSchema(value: Any?): String = when (value) {

        null -> throw NullPointerException()

        /** 주로 JSON 매핑에 사용 */
        is Map<*, *> -> "STRUCT< ${value.entries.joinToString(",") { toSchema(it) }} >"

        /** 주로 JSON 매핑에 사용 */
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

    /** icebug는 지원안함!! 주의!!  */
    val tinyint = "tinyint"
    val int = "int"
    val bigint = "bigint"

    val float = "float"
    val double = "double"

    /**
     * 가장 기본적인 json 대체 내장타입
     * 외부 데이터는 json으로 파싱해서 사용하면 되고
     * 내부 1뎁스 데이터는 이거로 사용할것
     *  */
    val mapString = "map<string, string>"

    companion object {
        private val log = KotlinLogging.logger {}
    }

}

