package net.kotlinx.aws.athena.table

import mu.KotlinLogging
import net.kotlinx.collection.toQueryString
import net.kotlinx.core.Kdsl
import net.kotlinx.string.toLocalDate
import net.kotlinx.time.toYmd
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
     * ex) collect/${테이블명}/
     *  */
    lateinit var s3Key: String

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

    /**
     * 파티션 정보
     * 일반적으로 boolean 을 사용하지만 ice버그의 경우 함수형을 입력할 수 있음
     *  -> https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html 참고
     *  */
    private val partitions: MutableList<String> = mutableListOf()

    /** NK(자연키) 설정 */
    private val nks: MutableList<String> = mutableListOf()

    /** 프로젝션 정보 설정 */
    private val projectionMap: MutableMap<String, Map<String, String>> = mutableMapOf()

    //==================================================== 테이블 컬럼 설정정보 ======================================================

    /**
     * 스키마
     * 기본적으로 언더스코어 구조이지만,
     * 예외적으로 json 입력 기본 스키마는 카멜케이스로 구성한다 (어차피 AWS가 시작을 잘못잡아서 엉망임)
     * Any 로 입력받아서 , 각 로직별로 처리함
     * AthenaUtil.toSchema(columnType) 참고
     *
     * 주의!! 아이스버그의경우 카멜인식 못함. 스네이크로 할것!
     * */
    lateinit var schema: Map<String, AthenaType>


    /** 기본 설정 */
    infix fun String.AS(columnType: Map<String, AthenaType>): Pair<String, AthenaType> = this to AthenaType(columnType)

    /** 기본 설정 */
    infix fun String.AS(columnType: List<Pair<String, AthenaType>>): Pair<String, AthenaType> = this to AthenaType(columnType)

    /** 자연키 + 파티션키 */
    infix fun String.PARTITION(columnType: AthenaType): Pair<String, AthenaType> {
        nks += this
        partitions += this
        return this to columnType
    }

    /** 자연키 */
    infix fun String.NK(columnType: AthenaType): Pair<String, AthenaType> {
        nks += this
        return this to columnType
    }

    /** 프로젝션 옵션 입력 */
    infix fun Pair<String, AthenaType>.PROJECTION(option: Map<String, String>): Pair<String, AthenaType> {
        projectionMap[this.first] = option
        return this
    }

    /**
     * 프로젝션 옵션  간단 입력
     * 시작일로부터 X년
     *  */
    infix fun Pair<String, AthenaType>.PROJECTION(range: Pair<String, Long>): Pair<String, AthenaType> {
        projectionMap[this.first] = mapOf(
            "type" to "date",
            "range" to "${range.first},${range.first.toLocalDate().plusYears(range.second).toYmd()}",
            "format" to "yyyyMMdd",
        )
        return this
    }

    /** 옵션 커스텀 */
    infix fun Pair<String, AthenaType>.OPTION(block: AthenaType.() -> Unit): Pair<String, AthenaType> {
        this.second.apply(block)
        return this
    }

    //====================================================  ======================================================

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
                val basicProps = mapOf(
                    "projection.enabled" to "true",
                    "storage.location.template" to "${location}${partitions.joinToString("/") { "\${${it}}" }}/",
                )
                val columnProps = partitions.flatMap { colimnName ->
                    val projectionConfig = projectionMap[colimnName] ?: mapOf("type" to "injected") //아무 설정 없으면 injected로 설정
                    projectionConfig.entries.map { "projection.${colimnName}.${it.key}" to it.value }
                }.toMap()
                props = props + basicProps + columnProps
            }

            AthenaTablePartitionType.INDEX -> {}
            AthenaTablePartitionType.NONE -> {}
        }

        /** 파티션 설정과 스키마가 중복되는거도 있고 아닌거도 있다.. */
        val schemaText = run {
            val targetSchemas = when (athenaTableFormat) {

                /** 파티션 정의와 스키마 정의가 중복되면 안되는거 */
                //Parquet 추가..
                in setOf(AthenaTableFormat.Json, AthenaTableFormat.Csv, AthenaTableFormat.Parquet) -> schema.filter { !nks.contains(it.key) }

                /** 파티션 정의와 스키마 정의가 중복되도 되는가 */
                else -> schema
            }
            targetSchemas.entries.joinToString(",\n") { "`${it.key}` ${it.value}" } //특문 가능하도록 `` 로 감싸준다
        }

        val partitionText = when (athenaTableFormat) {
            /**
             * 아이스버그의 경우 일반 컬럼에 파티션 데이터가 있어야 한다.
             * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html
             * */
            AthenaTableFormat.Iceberg -> if (partitions.isEmpty()) "" else "PARTITIONED BY (${partitions.joinToString(",")})"

            else -> if (partitions.isEmpty()) "" else "PARTITIONED BY (${partitions.joinToString(",") { "$it ${schema[it]!!.type}" }})"
        }

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

    /** NK 벨리데이션 쿼리 */
    fun validateNkDetail(block: () -> String = { "" }): String = """
            ${validateNkQuery(block)} 
            LIMIT 100
            """.trimIndent()

    /** 내부 벨리데이션 쿼리 */
    private fun validateNkQuery(block: () -> String): String {
        val nkText = nks.joinToString(",")
        return """SELECT ${nkText},COUNT(*) CNT 
                FROM $tableNameWithDatabase
                ${block()}
                GROUP BY $nkText
                HAVING COUNT(*) > 1"""
    }


    /** NK 벨리데이션 상세 조회 */
    fun validateNk(block: () -> String = { "" }): String {
        val partitionText = partitions.joinToString(",")
        return """
        select ${partitionText},COUNT(1) CNT
        from (
            ${validateNkQuery(block)}
        )
        GROUP BY $partitionText
        order by $partitionText
            """.trimIndent()
    }

    /** 데이터 경로 가져오기 */
    fun s3Path(partitionValue: Array<out String>): String {
        check(athenaTableType == AthenaTableType.EXTERNAL) { "athenaTableType EXTERNAL 테이블만 지원합니다." }
        val dataPath = when (athenaTablePartitionType) {
            AthenaTablePartitionType.PROJECTION -> {
                check(partitionValue.isNotEmpty())
                "${s3Key}${partitionValue.joinToString("/")}/"
            }

            AthenaTablePartitionType.INDEX -> {
                check(partitionValue.isNotEmpty())
                val pathKeys = partitions.take(partitionValue.size)
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

    //==================================================== 간편기능 ======================================================

    /** 간단하게 insert 구문 제작*/
    fun insertSql(datas: List<List<String>>): String {
        return """
            INSERT INTO ${this.tableNameWithDatabase} ( ${this.schema.keys.joinToString(",")}) 
            VALUES ${datas.joinToString(",") { data -> data.joinToString(",", "(", ")") { "'${it}'" } }}
        """.trimIndent()
    }


    //==================================================== 간단설정 ======================================================
    fun icebugTable() {
        athenaTableFormat = AthenaTableFormat.Iceberg
        athenaTableType = AthenaTableType.INTERNAL
        athenaTablePartitionType = AthenaTablePartitionType.INDEX
    }


    //==================================================== athena type 입력용 (하드코딩 방지) ======================================================
    //데이터 형식은 다음 참고 -> https://docs.aws.amazon.com/ko_kr/athena/latest/ug/data-types.html

    val boolean = AthenaType("boolean")
    val string = AthenaType("string")

    /** YYYY-MM-DD HH:MM:SS.SSS */
    val timestamp = AthenaType("timestamp")

    /** icebug는 지원안함!! 주의!!  */
    val tinyint = AthenaType("tinyint")
    val int = AthenaType("int")
    val bigint = AthenaType("bigint")

    val float = AthenaType("float")
    val double = AthenaType("double")

    /**
     * 가장 기본적인 json 대체 내장타입
     * 외부 데이터는 json으로 파싱해서 사용하면 되고
     * 내부 1뎁스 데이터는 이거로 사용할것
     *  */
    val mapString = AthenaType("map<string, string>")

    /** 기본 어레이 스트링 */
    val arrayString = AthenaType("array<string>")

    companion object {
        private val log = KotlinLogging.logger {}
    }

}

