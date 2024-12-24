package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.CostExplorerException
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.iam.IamCredential
import net.kotlinx.aws.toAwsClient
import net.kotlinx.exception.toSimpleString
import net.kotlinx.file.slashDir
import net.kotlinx.guava.fromJsonList
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.toYmdF01
import java.io.File
import java.time.LocalDate

private val log = KotlinLogging.logger {}

fun main() {

    Koins.startupOnlyOnce(BeSpecHeavy.MODULES)

    val workspace = ResourceHolder.WORKSPACE.slashDir("AWS비용")

    log.trace { "먼저 비용정보를 AWS에서 받아온 후 캐싱한다. (API 호출당 비용 발생함)" }
    val dataFile = File(workspace, "AWS비용캐시_.${LocalDate.now().toYmdF01()}.json")

    /** 태그별로 비용을 보고싶은경우 */
    val tagRequired = setOf("catson")

    if (!dataFile.exists()) {
        log.info { "cost 데이터를 로드합니다.." }
        runBlocking {
            val profileNames = IamCredential().profileDatas.map { it.profileName }.filter { it !in setOf("sin", "default", "md") } - listOf("kx")
            log.debug { " -> 로드된 프로파일 : $profileNames" }
            val datas = profileNames.flatMap { profileName ->
                val client = AwsConfig(profileName = profileName).toAwsClient()
                val byService = try {
                    client.cost.monthService().onEach { it.projectName = profileName }
                } catch (e: CostExplorerException) {
                    log.warn { " -> 프로파일 [${profileName}] 무시 : ${e.toSimpleString()}" }
                    emptyList()
                }
                val byTag = when (profileName in tagRequired) {
                    true -> {
                        try {
                            client.cost.monthTag(listOf("ProjectName")).onEach { it.projectName = profileName }
                        } catch (e: CostExplorerException) {
                            log.warn { " -> 프로파일 [${profileName}] 무시" }
                            emptyList()
                        }
                    }

                    false -> emptyList()
                }

                byService + byTag
            }
            dataFile.writeText(GsonSet.GSON.toJson(datas)) //일단 json을 기록
        }
    } else {
        log.warn { "cost 데이터를 재사용합니다.." }
    }

    val lines = GsonSet.GSON.fromJsonList<CostExplorerLine>(dataFile.readText())

//    val wonDoller = runBlocking {
//        val secret by lazyLoadStringSsm("/api/koreaexim/key")
//        try {
//            val client = KoreaeximClient(secret)
//            client.dollarWon()
//        } catch (e: Exception) {
//            log.warn { "달러원 오류!! -> 강제 매핑  ${e.toSimpleString()}" }
//            1380
//        }
//    }

    CostExplorerExcel {
        //won = wonDoller.toInt()
        costDatas = lines
        groupByProject()
        eachProject()
        excel.wrap()
        val outFile = File(workspace, "AWS비용_${LocalDate.now().toYmdF01()}.xlsx")
        excel.write(outFile)
        log.info { "결과파일 : $outFile" }
    }
}