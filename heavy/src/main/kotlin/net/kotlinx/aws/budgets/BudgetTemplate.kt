package net.kotlinx.aws.budgets

import aws.sdk.kotlin.services.budgets.model.CreateBudgetRequest


/**
 * 기본적인 예산 템플릿
 * */
interface BudgetTemplate {

    /** AWS ID */
    var awsId: String

    /** 예산 이름 */
    var budgetName: String

    fun toCreateBudgetRequest(): CreateBudgetRequest
}