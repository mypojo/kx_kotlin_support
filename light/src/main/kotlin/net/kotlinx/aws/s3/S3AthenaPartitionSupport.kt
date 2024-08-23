package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.S3Client
import mu.KotlinLogging
import net.kotlinx.aws.athena.table.AthenaTable
import java.io.File


private val log = KotlinLogging.logger {}

/** 데이터 경로 삭제 */
suspend fun S3Client.athenaTableFileDelete(table: AthenaTable, vararg partitionValue: String) {
    val dataPath = table.s3Path(partitionValue)
    log.debug { " -> 테이블 ${table.tableName} 데이터 삭제 : ${table.bucket} $dataPath" }
    this.deleteDir(table.bucket, dataPath)
}

/** 데이터 경로 업로드 */
suspend fun S3Client.athenaTableFileUpload(table: AthenaTable, file: File, vararg partitionValue: String) {
    val dataPath = table.s3Path(partitionValue)
    log.debug { " -> 테이블 ${table.tableName} 데이터 업로드 : ${table.bucket} $dataPath" }
    this.putObject(table.bucket, "${dataPath}${file.name}", file)
}

/** 삭제 후 입력 */
suspend fun S3Client.athenaTableFileDeleteAndUpload(table: AthenaTable, file: File, vararg partitionValue: String) {
    athenaTableFileDelete(table, *partitionValue)
    athenaTableFileUpload(table, file, *partitionValue)
}