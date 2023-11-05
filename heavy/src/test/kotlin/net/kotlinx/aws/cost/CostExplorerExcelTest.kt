package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.CostExplorerException
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.iam.IamCredential
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.core.time.toYmdF01
import net.kotlinx.guava.fromJsonList
import net.kotlinx.okhttp.OkHttpSamples
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class CostExplorerExcelTest : TestRoot() {


    @Test
    fun test() {

        val workspace = File(ResourceHolder.getWorkspace(), this::class.simpleName).apply { mkdirs() }
        val dataFile = File(workspace, "AWS비용캐시_.${LocalDate.now().toYmdF01()}.json")

        val tagRequired = setOf("catson")

        if (!dataFile.exists()) {
            runBlocking {
                val profileNames = IamCredential().profileNames.filter { it !in setOf("sin") }
                val datas = profileNames.flatMap { profileName ->
                    val client = AwsConfig(profileName = profileName).toAwsClient()
                    val byService = try {
                        client.cost.monthService().onEach { it.projectName = profileName }
                    } catch (e: CostExplorerException) {
                        log.warn { " -> 프로파일 [${profileName}] 무시" }
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
        }

        val lines = GsonSet.GSON.fromJsonList<CostExplorerLine>(dataFile.readText())

        CostExplorerExcel {
            won = runBlocking { OkHttpSamples.dollarWon() }
            costDatas = lines
            groupByProject()
            eachProject()
            excel.wrap()
            val outFile = File(workspace, "AWS비용_${LocalDate.now().toYmdF01()}.xlsx")
            excel.write(outFile)
            log.info { "결과파일 : $outFile" }
        }

    }

}