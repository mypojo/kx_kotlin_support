package net.kotlinx.aws.budgets

import aws.sdk.kotlin.services.budgets.BudgetsClient
import aws.sdk.kotlin.services.budgets.deleteBudget
import aws.sdk.kotlin.services.budgets.model.NotFoundException
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.budget: BudgetsClient
    get() = getOrCreateClient { BudgetsClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * 예산 삭제후 새로만듬
 * 일반적인 월단위 예산 알림의 경우 굳이 수정할 필요성을 못느끼겟다.
 * */
suspend fun BudgetsClient.deleteAndCreateBudget(budget: BudgetTemplate) {
    try {
        this.deleteBudget {
            this.budgetName = budget.budgetName
            this.accountId = budget.awsId
        }
    } catch (e: NotFoundException) {
        //최초 생성이라면 삭제 스킵
    }
    this.createBudget(budget.toCreateBudgetRequest())
}



