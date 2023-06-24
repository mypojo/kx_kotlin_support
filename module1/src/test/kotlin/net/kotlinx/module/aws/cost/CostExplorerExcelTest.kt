package net.kotlinx.module.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.CostExplorerException
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.cost.CostExplorerLine
import net.kotlinx.aws.cost.monthService
import net.kotlinx.aws.iam.IamCredential
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.threadlocal.ResourceHolder
import net.kotlinx.core.time.toYmdF01
import net.kotlinx.guava.fromJsonList
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class CostExplorerExcelTest : TestRoot() {

    @Test
    fun test() {

        val workspace = File(ResourceHolder.getWorkspace(), this::class.simpleName).apply { mkdirs() }
        val dataFile = File(workspace, "data.json")

        if (!dataFile.exists()) {
            runBlocking {
                val profileNames = IamCredential().profileNames.filter { it !in setOf("sin") }
                val datas = profileNames.flatMap { profileName ->
                    try {
                        AwsConfig(profileName = profileName).toAwsClient().cost.monthService().onEach { it.projectName = profileName }
                    } catch (e: CostExplorerException) {
                        log.warn { " -> 프로파일 [${profileName}] 무시" }
                        emptyList()
                    }
                }
                dataFile.writeText(GsonSet.GSON.toJson(datas))
            }
        }

        val lines = GsonSet.GSON.fromJsonList<CostExplorerLine>(dataFile.readText())

        CostExplorerExcel {
            this.costDatas = lines
            groupByProject()
            eachProjectByService()
            excel.wrap()
            excel.write(File(workspace, "AWS비용_${LocalDate.now().toYmdF01()}.xlsx"))
        }

    }

}