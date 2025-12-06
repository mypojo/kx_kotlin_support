package net.kotlinx.aws.cost

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import java.time.LocalDate

/**
 * CostExplorerSupport2 kotest 템플릿
 * 내용은 비워둡니다. 필요 시 테스트 케이스를 추가하세요.
 */
internal class CostExplorerSupport2_kotest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile48) }
    private val ddd by lazy { koin<AwsClient>(findProfile49) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("costs") {

            Then("costByTagService") {
                val costs = aws.cost.costByTagService(
                    //start = LocalDate.now().minusDays(10),
                    start = LocalDate.of(2025, 12, 1),
                    end = LocalDate.of(2025, 12, 2),
                )
                costs.collect { it.print() }
            }

        }
    }
}
