package net.kotlinx.aws.athena.table

import net.kotlinx.aws.athena.AthenaModule

/**
 * 보존 기한 이후의 삭제처리된 데이터를 실제로 삭제한다.
 * https://docs.aws.amazon.com/ko_kr/athena/latest/ug/querying-iceberg-data-optimization.html
 * */
fun AthenaModule.icebugDoVacuum(tableName: String) {
    this.execute("VACUUM $tableName")
}

/** 레이아웃을 최적화해서 재구성한다 */
fun AthenaModule.icebugDoOptimize(tableName: String) {
    this.execute("OPTIMIZE $tableName REWRITE DATA USING BIN_PACK  where 1=1;")
}