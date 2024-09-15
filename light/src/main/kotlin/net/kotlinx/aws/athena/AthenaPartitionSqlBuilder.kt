package net.kotlinx.aws.athena

import net.kotlinx.core.Kdsl
import java.time.LocalDate

/**
 * 문법상 limit는 없는듯 하지만 쿼리가 너무 길어지면 오류가 나기때문에 1000개 이하로 추천
 * 샘플 : 파티션 7500개 생성에 1분 56초 걸림
 * 경고!! 기본map 이어야함
 *  */
private const val PARTITION_ADD_LIMIT = 1000

/**
 * Athena 파티션 sql 생성 도우미
 * 미리 파티션에 입력될 값을 알고 있어야 한다.
 * 파티션 정보를 모르는경우 S3 스캔해서 확인
 *
 * 주의!! 가능하면 파티션 대신 projection 이나 아이스버그를 사용하세요
 */
class AthenaPartitionSqlBuilder {

    @Kdsl
    constructor(block: AthenaPartitionSqlBuilder.() -> Unit = {}) {
        apply(block)
    }


    /** ex) sin-work-dev */
    lateinit var bucketName: String

    /**
     * ex) collect
     * ex) cloudtrail/AWSLogs/xxxxxxxxxxx/CloudTrail/ap-northeast-2
     *  */
    lateinit var prefix: String

    /**
     * 다른 데이터베이스에 적용할경우 사용
     * ex) dev
     *  */
    var database: String? = null

    /**
     * SQL에 사용할 .이 포함된 데이터베이스 이름
     *  */
    private val tablePrefix: String by lazy { database?.let { "${it}." } ?: "" }

    /**
     * 파티션이 A=B/C=D 이런식의 키밸류인지?
     * cloudtrail 같은거는 키벨류가 아니라 false 로 해줘야함
     * */
    var keyValue: Boolean = true


    //==================================================== 쿼리 생성 ======================================================

    /** 파티셔닝 추가 SQL 생성 */
    fun generateAddSql(tableName: String, datas: List<Map<String, String>>): String {
        val tablePath = tableName.replace(Regex(".*\\."), "") //스키마 제거
        val s3Path = "s3://${bucketName}/${prefix}/${tablePath}"
        val append = datas.joinToString("\n") { dataMap ->
            if (dataMap.size >= 2) {
                check(dataMap is LinkedHashMap) { "순서가 있는 맵 이어야함" }
            }
            val pData = dataMap.entries.joinToString(",") { "${it.key}='${it.value}'" }
            val pPath = when (keyValue) {
                true -> dataMap.entries.joinToString("/") { "${it.key}=${it.value}" }
                false -> dataMap.values.joinToString("/") { it }
            }
            "PARTITION (${pData}) LOCATION '${s3Path}/${pPath}/'"
        }

        return "ALTER TABLE `${tablePrefix}${tableName}` ADD IF NOT EXISTS\n${append}"
    }

    /** 대량의 경우 limit로 분리해서 만들어줌 */
    fun generateAddSqlBatch(tableName: String, datas: List<Map<String, String>>): List<String> = datas.chunked(PARTITION_ADD_LIMIT).map { generateAddSql(tableName, it) }

    /** 파티셔닝 삭제 SQL 생성 -> 2일치 삭제 불가능. 나눠서 할것 */
    fun generateDropSql(tableName: String, datas: List<Map<String, String>>): String {
        val append = datas.joinToString(",\n") { dataMap ->
            val pData = dataMap.entries.joinToString(",") { "${it.key}='${it.value}'" }
            "PARTITION (${pData})"
        }
        return "ALTER TABLE `${tablePrefix}${tableName}` DROP IF EXISTS\n${append}"
    }

    /**
     * 클라우드트레일 디폴트 (맘에 안드는데 수정이 안된다)
     * https://docs.aws.amazon.com/athena/latest/ug/cloudtrail-logs.html#create-cloudtrail-table
     *  */
    fun generateAddSqlForcloudtrail(tableName: String, vararg days: LocalDate): String {
        val s3Path = "s3://${bucketName}/${prefix}"
        val append = days.joinToString("\n") { day ->
            val param = mapOf(
                "year" to day.year,
                "month" to day.monthValue.toString().padStart(2, '0'),
                "day" to day.dayOfMonth.toString().padStart(2, '0'),
            )
            val pData = param.entries.joinToString(",") { "${it.key}='${it.value}'" }
            val pPath = param.values.joinToString("/")
            "PARTITION (${pData}) LOCATION '${s3Path}/${pPath}/'"
        }
        return "ALTER TABLE `${tablePrefix}${tableName}` ADD IF NOT EXISTS\n${append}"
    }

    //==================================================== 데이터 생성 ======================================================

    /**
     * 시간+날짜 베이스 파티션 데이터 생성
     * 시간단위 로그 등에 사용
     * ex) basicDate=20220927/hh=00/xx.csv
     *  */
    fun generateBasicDateHh(vararg basicDates: String): List<LinkedHashMap<String, String>> {
        return basicDates.flatMap { basicDate ->
            (0..23).map { i ->
                LinkedHashMap<String, String>().apply {
                    put("basicDate", basicDate)
                    put("hh", i.toString().padStart(2, '0'))
                }
            }
        }.toList()
    }


}