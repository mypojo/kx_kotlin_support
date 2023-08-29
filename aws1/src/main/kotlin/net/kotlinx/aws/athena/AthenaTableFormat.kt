package net.kotlinx.aws.athena


enum class AthenaTableFormat(val define: String) {

    /** 최초 데이터 입력시 주로 사용 ex) 이벤트브릿지  */
    Json(
        """
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
    """.trimIndent()
    ),
    /** 최초 데이터 입력시 주로 사용 ex) 사용자 정의 데이터 파일 or RDB 데이터 */
    Csv(
        """
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        """.trimIndent()
    ),
    /**
     * TSV로 입력할때. (특이사항)
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/lazy-simple-serde.html
     * */
    Tsv(
        """
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t'
ESCAPED BY '\\'
LINES TERMINATED BY '\n'
        """.trimIndent()
    ),
    /** 최종 2차 or 3차 가공 테이블을 만들때 사용 */
    Parquet(
        """
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
    """.trimIndent()
    ),

    //==================================================== 벤더 지정 ======================================================

    /**
     * 아래 랭크에서 복붙할것
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/cloudtrail-logs.html
     *  */
    CloudTrail(
        """
ROW FORMAT SERDE 'com.amazon.emr.hive.serde.CloudTrailSerde'
STORED AS INPUTFORMAT 'com.amazon.emr.cloudtrail.CloudTrailInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' 
        """.trimIndent()
    ),


    ;


}
