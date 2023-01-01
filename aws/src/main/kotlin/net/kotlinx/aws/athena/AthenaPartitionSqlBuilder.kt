package net.kotlinx.aws.athena

import java.time.LocalDate

/**
 * Athena 파티션 sql 생성 도우미
 * 미리 파티션에 입력될 값을 알고 있어야 한다.
 * 파티션 정보를 모르는경우 S3 스캔해서 확인
 */
class AthenaPartitionSqlBuilder(

    /** ex) sin-work-dev */
    private val bucketName: String,
    /**
     * ex) collect
     * ex) cloudtrail/AWSLogs/653734769926/CloudTrail/ap-northeast-2
     *  */
    private val prefix: String = "collect",
) {

    //==================================================== 쿼리 생성 ======================================================

    /** 파티셔닝 추가 SQL 생성  -> 3일치 생성 가능 */
    fun generateAddSql(tableName: String, datas: List<LinkedHashMap<String, String>>): String {
        val tablePath = tableName.replace(Regex(".*\\."), "") //스키마 제거
        val s3Path = "s3://${bucketName}/${prefix}/${tablePath}"
        val append = datas.joinToString("\n") { dataMap ->
            val pData = dataMap.entries.joinToString(",") { "${it.key}='${it.value}'" }
            val pPath = dataMap.entries.joinToString("/") { "${it.key}=${it.value}" }
            "PARTITION (${pData}) LOCATION '${s3Path}/${pPath}/'"
        }
        return "ALTER TABLE $tableName ADD IF NOT EXISTS\n${append}"
    }

    /** 파티셔닝 삭제 SQL 생성 -> 2일치 삭제 불가능. 나눠서 할것 */
    fun generateDropSql(tableName: String, datas: List<LinkedHashMap<String, String>>): String {
        val append = datas.joinToString(",\n") { dataMap ->
            val pData = dataMap.entries.joinToString(",") { "${it.key}='${it.value}'" }
            "PARTITION (${pData})"
        }
        return "ALTER TABLE $tableName DROP IF EXISTS\n${append}"
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
        return "ALTER TABLE $tableName ADD IF NOT EXISTS\n${append}"
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