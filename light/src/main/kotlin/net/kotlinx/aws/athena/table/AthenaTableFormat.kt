package net.kotlinx.aws.athena.table

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
            return listOf(
                "ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'",
                "WITH SERDEPROPERTIES (\"case.insensitive\" = \"FALSE\")", //이게 있어야 대소문자 구분함
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
     * */
    data object Iceberg : AthenaTableFormat {
        override fun toRowFormat(table: AthenaTable): List<String> {
            return emptyList() //별도 필요 없으음
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