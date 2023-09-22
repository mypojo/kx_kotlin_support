package net.kotlinx.aws.athena

sealed interface AthenaTableFormat {

    fun toRowFormat(table: AthenaTable): List<String>

    /**
     * AWS 전용 최적화 포맷.
     * ex) DDB 를 S3로 export 해서 Athena로 읽을때
     * 이경우 모든 데이터를 string 으로 할것
     * https://github.com/amazon-ion/ion-java-path-extraction
     * */
    data object IonDdb : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return listOf(
                "ROW FORMAT SERDE 'com.amazon.ionhiveserde.IonHiveSerDe'",
                "WITH SERDEPROPERTIES (",
                table.schema.keys.joinToString(",\n") { "    \"ion.${it}.path_extractor\" = \"(${table.ionFlatPath} ${it})\"" },  //ex) ion.pk.path_extractor" = "(Item pk)
                ")",
                "STORED AS INPUTFORMAT 'com.amazon.ionhiveserde.formats.IonInputFormat'",
                "OUTPUTFORMAT 'com.amazon.ionhiveserde.formats.IonOutputFormat'",
            )
        }
    }

    /** 최종 2차 or 3차 가공 테이블을 만들때 사용 */
    data object Parquet : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return listOf(
                "ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'",
                "STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'",
                "OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'",
            )
        }
    }

    /** 최초 데이터 입력시 주로 사용 ex) 이벤트브릿지  */
    data object Json : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return listOf(
                "ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'",
                "STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'",
                "OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'",
            )
        }
    }


    /** 최초 데이터 입력시 주로 사용 ex) 사용자 정의 데이터 파일 or RDB 데이터 */
    data object Csv : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return listOf(
                "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'",
            )
        }
    }

    /**
     * TSV로 입력할때. (특이사항)
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/lazy-simple-serde.html
     * */
    data object Tsv : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return listOf(
                "ROW FORMAT DELIMITED",
                "FIELDS TERMINATED BY '\\t'",
                "ESCAPED BY '\\\\'",
                "LINES TERMINATED BY '\\n'",
            )
        }
    }

    //==================================================== 벤더 지정 ======================================================

    /**
     * 아래 랭크에서 복붙할것
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/cloudtrail-logs.html
     *  */
    data object CloudTrail : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return listOf(
                "ROW FORMAT SERDE 'com.amazon.emr.hive.serde.CloudTrailSerde'",
                "STORED AS INPUTFORMAT 'com.amazon.emr.cloudtrail.CloudTrailInputFormat'",
                "OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ",
            )
        }
    }

}