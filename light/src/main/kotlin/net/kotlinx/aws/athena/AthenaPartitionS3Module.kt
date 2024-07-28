package net.kotlinx.aws.athena

import mu.KotlinLogging
import net.kotlinx.aws.s3.listDirs
import net.kotlinx.core.Kdsl

/**
 * Athena s3 - 파티션 생성 도우미
 * S3를 리스팅해서 자동으로 파티션을 생성한다.
 */
class AthenaPartitionS3Module {

    private val log = KotlinLogging.logger {}

    @Kdsl
    constructor(block: AthenaPartitionS3Module.() -> Unit = {}) {
        apply(block)
    }

    /** 아테나 모듈  */
    lateinit var athenaModule: AthenaModule

    /** 테이블명 */
    lateinit var tableName: String

    /** 인덱스 키값 */
    lateinit var partitionKeys: List<String>

    /** 파티션 빌더 (버킷 정보 같이 사용) */
    lateinit var partitionSqlBuilder: AthenaPartitionSqlBuilder

    suspend fun listDir(prefixs: List<String>, upkeep: MutableList<Map<String, String>>) {

        val base = prefixs.mapIndexed { i, it -> partitionKeys[i] to it }.toMap()
        if (prefixs.size == partitionKeys.size) {
            upkeep += base
            log.trace { "prefixs가 전체 인덱스와 일치함 -> S3 리스트 호출 중지" }
            return
        }

        val prefixList = listOf(partitionSqlBuilder.prefix) + tableName + prefixs.mapIndexed { i, v -> "${partitionKeys[i]}=${v}" }
        val dirPrefix = "${prefixList.joinToString("/")}/"
        log.debug { "버킷[${partitionSqlBuilder.bucketName}] 조회 : $dirPrefix" }
        athenaModule.aws.s3.listDirs(partitionSqlBuilder.bucketName, dirPrefix).forEach { dir ->
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

    /**
     * 최초 1회는 전체로 돌리고,
     * 데이터가 많아지는경우 당일, 전일 등을 선택해서 돌리면됨 (재시도 안전함)
     *
     * @param prefixs 특정 프리픽스로 제한해서 디렉토링. ex) 파티션이 날짜/ID 인경우 날짜입력.  입력이 없으면 전체 순회.
     * */
    suspend fun listAndUpdate(vararg prefixs: String) {
        check(prefixs.size <= partitionKeys.size) { "입력값은 인덱스 길이보다 클 수 없음" }

        val upkeep: MutableList<Map<String, String>> = mutableListOf()
        listDir(prefixs.toList(), upkeep)
        log.debug { " -> 파티션 ${upkeep.size}건 조회됨" }

        val sqls = partitionSqlBuilder.generateAddSqlBatch(tableName, upkeep)
        if (log.isTraceEnabled) {
            sqls.forEachIndexed { i, it -> log.debug { " -> sql[$i]\n$it" } }
        }
        athenaModule.startAndWait(sqls.map { AthenaExecute(it) })
    }

}

