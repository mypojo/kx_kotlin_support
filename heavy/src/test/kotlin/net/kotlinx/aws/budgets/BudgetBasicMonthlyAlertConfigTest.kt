package net.kotlinx.aws.budgets

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class BudgetBasicMonthlyAlertConfigTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("지난달 대비 xx% 예상 or xx% 도달시 알람 발송") {

            val alertConfig = BudgetBasicMonthlyAlertTemplate {
                emails = listOf()
            }

            val profiles = listOf(
                findProfile28(),
                findProfile46(),
                findProfile48(),
                findProfile97(),
                findProfile99(),
            )

            profiles.forEach { profile ->
                val aws = koin<AwsClient>(profile)
                Then("profile $profile") {
                    alertConfig.awsId = aws.awsConfig.awsId
                    alertConfig.budgetName = "${alertConfig.budgetName}-${profile}" //프로파일이 이름에 있어야, 이메일만으로 확인이 쉬움
                    aws.budget.deleteAndCreateBudget(alertConfig)
                }
            }

        }
    }

}
