package net.kotlinx.domain.batchStep

import net.kotlinx.aws.athena.table.AthenaTable
import net.kotlinx.aws.athena.table.AthenaTableFormat
import net.kotlinx.aws.athena.table.AthenaTablePartitionType


/**
 * 아테나 테이블 정의
 * */
object BatchStepTable {

    /**
     * 결과 파일 저장
     * 버킷 등은 개별 입력 해야함
     *  */
    val BATCH_STEP = AthenaTable {
        tableName = "batch_step"
        s3Key = "upload/sfnBatchModuleOutput/"
        schema = mapOf(
            "sfn_id" PARTITION string,
            "file_name" to string,
            "total_interval" to bigint,
            "total_size" to int,
            "input" to string,
            "output" to string,
        )
        athenaTableFormat = AthenaTableFormat.Json
        athenaTablePartitionType = AthenaTablePartitionType.PROJECTION
    }


}


