package net.kotlinx.aws.athena

object AthenaUtil {

    /** 한번에 50개 까지만 호출 가능  */
    const val API_LIMIT_SIZE = 50

//    /**
//     * 성공이 아니면 예외를 던지기
//     * 간이 배치 형태에만 사용할것
//     */
//    fun checkAndThrow(queryExecution: QueryExecution) {
//        val status = queryExecution.status
//        if (status!!.state!!.isOk()) return
//        throw IllegalStateException("쿼리(${queryExecution.queryExecutionId}) 실행 실패 : $status / ${status.stateChangeReason}")
//    }

//
//    @JvmStatic
//    fun checkAndThrow(queryExecution: Collection<QueryExecution>) {
//        val falis: List<QueryExecution> = queryExecution.stream().filter(Predicate<QueryExecution> { v: QueryExecution -> !isOk(v.status().state()) }).collect(Collectors.toList<Any>())
//        if (falis.isEmpty()) return
//        val msg: String = StringFormatUtil.format("쿼리 {}/{} 실행 실패 : {}", falis.size, queryExecution.size, falis)
//        throw AwsAthenaQueryFailException(msg)
//    }
//
//    /**
//     * 성공이어도 읽은 데이터가 없다면 예외를 던진다. (where 절 실수일 확율이 높음)
//     * @see .checkAndThrow
//     */
//    fun checkAndThrowIfEmpty(queryExecution: QueryExecution) {
//        checkAndThrow(queryExecution)
//        val aLong: Long = queryExecution.statistics().dataScannedInBytes()
//        if (aLong > 0) return
//        val msg: String = StringFormatUtil.format("쿼리({}) 실행 실패 : 쿼리는 정상 종료되었으나 읽은 데이터가 0입니다", queryExecution.queryExecutionId())
//        throw RuntimeException(msg)
//    }
//    //==================================================== SQL 생성기 ======================================================
//    /** 파티션 생성용 쿼리  */
//    fun buildPartitionAddSql(tableName: String?, datas: List<Map<String, Any>>): String {
//        val partitions: List<String> = toPartition(datas)
//        return StringFormatUtil.format("ALTER TABLE {} ADD {}", tableName, partitions.stream().collect(Collectors.joining(" ")))
//    }
//
//    /** 파티션 드랍용 쿼리  */
//    fun buildPartitionDropSql(tableName: String?, datas: List<Map<String, Any>>): String {
//        val partitions: List<String> = toPartition(datas)
//        return StringFormatUtil.format("ALTER TABLE {} DROP {}", tableName, partitions.stream().collect(Collectors.joining(" ,"))) //조이너가 틀림
//    }
//
//    private fun toPartition(datas: List<Map<String, Any>>): List<String> {
//        val partitions: List<String> = datas.stream().map(Function<Map<String, Any>, Any> { map: Map<String, Any> ->
//            val partitionJoin: String = map.entries.stream().map(Function { e: Map.Entry<String, Any> ->
//                val value: Any = e.value
//                val valueStr: String = if (value is Number) value.toString() else "'" + value + "'"
//                e.key + " = " + valueStr
//            }).collect(Collectors.joining(","))
//            StringFormatUtil.format("PARTITION ( {} )", partitionJoin)
//        }).collect(Collectors.toList<Any>())
//        return partitions
//    }
//    //==================================================== 간단 컨슈머 ======================================================
//    /**
//     * 전체 수를 알고 양이 많은 간단 로드용 컨슈머
//     * EX) select limit 로 불러오는 키워드 로드
//     */
//    fun buildEntryConsumer(limit: Long, kwds: MutableSet<String?>): Consumer<Map.Entry<List<String?>?, List<List<String>>>> {
//        val logger: ProgressLogger = ProgressLogger.of(limit)
//        return Consumer { e: Map.Entry<List<String?>?, List<List<String>>> ->
//            val lines: List<List<String>> = e.value
//            lines.forEach(Consumer { v: List<String> -> kwds.add(v.get(0)) })
//            logger.log(lines.size)
//        }
//    }
//    //==================================================== json 변환기 ======================================================
//    /** 간단한 결과 래핑  */
//    fun toGson(queryExecution: QueryExecution): GsonData {
//        val result: GsonData = GsonData.`object`()
//        result.put("queryExecutionId", queryExecution.queryExecutionId())
//        result.put("outputLocation", queryExecution.resultConfiguration().outputLocation())
//        val status: QueryExecutionStatus = queryExecution.status()
//        result.put("state", status.state().name())
//        result.put("stateChangeReason", status.stateChangeReason())
//        result.put("state", isOk(status.state()))
//        val statistics: QueryExecutionStatistics = queryExecution.statistics()
//        val dataScannedInBytes: Long = statistics.dataScannedInBytes()
//        val unit: Long = dataScannedInBytes / FileUtils.ONE_MB / 10 + 1 //과금 단위 구함
//        val cost: Double = COST_PER_UNIT.multiply(BigDecimal(unit * 1000)).toDouble() //천회 기준으로 구함 (1회당 비용이면 너무 적음)
//        result.put("totalExecutionTimeInMillis", statistics.totalExecutionTimeInMillis())
//        var queryPlanningTimeInMillis: Long? = statistics.queryPlanningTimeInMillis()
//        if (queryPlanningTimeInMillis == null) queryPlanningTimeInMillis = 0L //널 가능한 데이터..
//        result.put("queryPlanningTimeInMillis", queryPlanningTimeInMillis)
//        result.put("totalExecutionTimeInMillis", statistics.totalExecutionTimeInMillis())
//        result.put("dataScannedInBytes", dataScannedInBytes)
//        result.put("cost", cost)
//        return result
//    }
//    //==================================================== Grid 변환기 ======================================================
//    /**
//     * 통계 결과치를 간단히 볼때
//     * ex) AwsAthenaUtil.toTextGrid(result.getQueryExecution().statistics()).print();
//     */
//    fun toTextGrid(st: QueryExecutionStatistics): TextGrid {
//        val grid: TextGrid = TextGrid.of("전체실행시간", "쿼리플래닝", "엔진실행", "스캐닝데이터", "비용수", "천회당비용(원)")
//        val asByte: Long = st.dataScannedInBytes()
//        val unit: Long = asByte / FileUtils.ONE_MB / 10 + 1 //과금 단위 구함
//        val cost: Double = COST_PER_UNIT.multiply(BigDecimal(unit * 1000)).toDouble() //천회 기준으로 구함 (너무 적음)
//        val planningTime: Long? = st.queryPlanningTimeInMillis() //null 가능함.. ㅅㅂ
//        val planningTimeStr: String = if (planningTime == null) "-" else TimeString(planningTime).toString()
//        return grid.add(TimeString(st.totalExecutionTimeInMillis()), planningTimeStr, TimeString(st.engineExecutionTimeInMillis()), StringFormatUtil.toFileSize(asByte), unit, cost)
//    }
//
//    /**
//     * 결과 간단히 보기
//     * AwsAthenaUtil.toTextGrid(result).print();
//     */
//    fun toTextGrid(result: AwsAthenaResult): TextGrid {
//        val datas: List<Array<Any>> = result.getLines().stream().map(java.util.List::toArray).collect(Collectors.toList())
//        return TextGrid.of(result.getColumnNames()).datas(datas)
//    }
}