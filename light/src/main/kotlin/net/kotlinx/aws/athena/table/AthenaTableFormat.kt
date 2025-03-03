package net.kotlinx.aws.athena.table

import kotlin.time.Duration.Companion.days

sealed interface AthenaTableFormatIcebug {
    val defaultOption: Map<String, String>
}

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
                table.schema.entries.joinToString(",\n") { "    \"ion.${it.key}.path_extractor\" = \"(${table.ionFlatPath} ${it})\"" },  //ex) ion.pk.path_extractor" = "(Item pk)
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

    /**
     * 최초 데이터 입력시 주로 사용
     * ex) 이벤트브릿지 or 커스텀한 구조의 작업 결과물 (csv 대체)
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/openx-json-serde.html
     *  */
    data object Json : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {

            /**
             * 추가 옵션들
             * 'ignore.malformed.json' = 'false',
            'dots.in.keys' = 'false',
             */
            val options = mapOf(
                /** 이걸 false 로 해야 대소문자 구분함 */
                "case.insensitive" to "false",
            )

            return listOf(
                "ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'",
                "WITH SERDEPROPERTIES ( ${options.entries.joinToString(",") { "\"${it.key}\" = \"${it.value}\"" }}  )",
                "STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'",
                "OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'",
            )
        }
    }


    /**
     * 최초 데이터 입력시 주로 사용 ex) 사용자 정의 데이터 파일 or RDB 데이터
     * "1" 이런식으로 " 로 감싸지는 데이터 형식임 (csv기본)
     * 이 CSV 안에 JSON을 문자열로 인식하면 " 인식에 문제가 생긴다.. 방법 못찾음. 일단 자체 이스케이핑
     * */
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


    /**
     * 아이스버그!! 트랜잭션 필요하면 이거
     * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html
     * athena 는 기본적으로 v2 버전의 아이스버그 테이블을 생성한다 (따로 명시 x)
     * */
    data object Iceberg : AthenaTableFormat, AthenaTableFormatIcebug {
        override fun toRowFormat(table: AthenaTable): List<String> = emptyList() /*별도 필요 없으음*/
        override val defaultOption = ICEBUG_DEFAULT_FORMAT
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

    companion object {
        private val ICEBUG_DEFAULT_FORMAT = mapOf(
            //https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-creating-tables.html
            "table_type" to "ICEBERG",
            "optimize_rewrite_delete_file_threshold" to "5", //임계값보다 적으면 파일이 재작성되지 않음
            "vacuum_max_snapshot_age_seconds" to "${14.days.inWholeSeconds}", //vacuum 명령으로 몇일치 삭제데이터의 마커만 남기고 다 삭제할지? 기본값은 5일 -> 2주로 수정
            //이하 설정은 일단 기본값 사용함.
        )
    }

}