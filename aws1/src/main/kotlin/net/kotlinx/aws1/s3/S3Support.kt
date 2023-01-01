package net.kotlinx.aws1.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.Object
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.content.writeToFile
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

/** 간단 다운로드. 스트리밍 처리시 다운받아서 하세여 (inputStream 제공이 없는거 같음.) */
suspend inline fun S3Client.getObjectDownload(bucket: String, key: String, file: File) = this.getObject(
    GetObjectRequest {
        this.bucket = bucket
        this.key = key
    }
) {
    it.body?.writeToFile(file)
}

/** 간단 읽기 (소용량) */
suspend inline fun S3Client.getObjectLines(bucket: String, key: String): List<List<String>> = this.getObject(
    GetObjectRequest {
        this.bucket = bucket
        this.key = key
    }
) {
    csvReader().readAll(it.body!!.decodeToString())
}

/**
 * 디렉토리를 가져온다.
 * @param prefix  디렉토리 표시인 /로 끝나야 한다.  ex) main/data/
 * */
suspend inline fun S3Client.listDirs(bucket: String, prefix: String): List<String> = this.listObjectsV2 {
    this.bucket = bucket
    delimiter = "/"
    this.prefix = prefix
}.commonPrefixes?.map { it.prefix!! } ?: emptyList() //파일이 있더라도 디렉토리가 없다면 빈값이 올 수 있음

/** 파일(객체)들을 가져온다. */
suspend inline fun S3Client.listFiles(bucket: String, prefix: String): List<Object> = this.listObjectsV2 {
    this.bucket = bucket
    this.prefix = prefix
}.contents ?: emptyList()

