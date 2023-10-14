package net.kotlinx.aws.athena

import aws.sdk.kotlin.services.s3.S3Client
import mu.KotlinLogging
import net.kotlinx.aws.s3.listDirs

/**
 * Athena s3 - 파티션 생성 도우미
 */
class AthenaS3PartitionModule(
    private val s3Client: S3Client,
    private val athenaModule: AthenaModule,
    private val bucketName: String,
    private val prefix: String,
    private val tableName: String,
    /** 인덱스 키값 */
    private val partitionKeys: List<String>,
) {

    private val log = KotlinLogging.logger {}

    private val builder = AthenaPartitionSqlBuilder(bucketName, prefix)

    suspend fun listDir(prefixs: List<String>, upkeep: MutableList<Map<String, String>>) {

        val base = prefixs.mapIndexed { i, it -> partitionKeys[i] to it }.toMap()
        if (prefixs.size == partitionKeys.size) {
            upkeep += base
            return
        }

        val prefixList = listOf(prefix) + tableName + prefixs.mapIndexed { i, v -> "${partitionKeys[i]}=${v}" }
        val dirPrefix = "${prefixList.joinToString("/")}/"
        log.debug { "버킷[$bucketName] 조회 : $dirPrefix" }
        s3Client.listDirs(bucketName, dirPrefix).forEach { dir ->
            val suffixs = dir.key.substring(dirPrefix.length).split("/").filter { it.isNotEmpty() }
            check(suffixs.isNotEmpty()) { "데이터가 반드시 있어야함" }
            val (k, v) = suffixs.first().split("=")
            val isLastNode = prefixs.size + 1 == partitionKeys.size
            if (isLastNode) {
                check(k == partitionKeys.last()) { "마지막 노드인경우 남은 키가 마지막 파티션의 키 여야함" }
                val theMap = base + (partitionKeys.last() to v)
                if (theMap.size > 1) {
                    check(theMap is LinkedHashMap) { "사이즈가 1 이상인경우 코틀린 디폴트 확인!!" }
                }
                upkeep += theMap
            } else {
                log.trace { " -> 재귀 호출됨 $v" }
                listDir(prefixs + v, upkeep)
            }
        }

    }

    suspend fun listAndUpdate(vararg prefixs: String) {
        check(prefixs.size <= partitionKeys.size) { "입력값은 인덱스 길이보다 클 수 없음" }

        val upkeep: MutableList<Map<String, String>> = mutableListOf()
        listDir(prefixs.toList(), upkeep)
        log.debug { " -> 파티션 ${upkeep.size}건 조회됨" }
        val sqls = builder.generateAddSqlBatch(tableName, upkeep)
        if (log.isTraceEnabled) {
            sqls.forEachIndexed { i, it -> log.trace { " -> sql[$i]\n$it" } }
        }
        athenaModule.startAndWaitAndExecute(sqls.map { AthenaExecute(it) })
    }

}

