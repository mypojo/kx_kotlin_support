package net.kotlinx.aws.budgets

import aws.sdk.kotlin.services.budgets.model.*
import aws.smithy.kotlin.runtime.time.Instant
import net.kotlinx.core.Kdsl
import net.kotlinx.time.TimeUtil
import net.kotlinx.time.toLocalDateTime
import net.kotlinx.time.toLong
import java.time.LocalDate

/**
 * 자주 사용되는 간단 예산설정
 * */
class BudgetBasicMonthlyAlertTemplate : BudgetTemplate {

    @Kdsl
    constructor(block: BudgetBasicMonthlyAlertTemplate.() -> Unit = {}) {
        apply(block)
    }

    override lateinit var awsId: String

    /** 예산 이름 */
    override var budgetName: String = "basicMonthlyAlert"

    /** 경보 설정 */
    var notifications: List<Pair<NotificationType, Double>> = listOf(
        NotificationType.Forecasted to 120.0,
        NotificationType.Actual to 120.0,
    )

    /** 이메일  */
    var emails: List<String> = emptyList()

    /** SNS ARN  */
    var snsList: List<String> = emptyList()

    override fun toCreateBudgetRequest(): CreateBudgetRequest {

        val budget = Budget {
            this.budgetName = this@BudgetBasicMonthlyAlertTemplate.budgetName
            this.budgetType = BudgetType.Cost
            this.costFilters = emptyMap() // 전체 사용
            this.costTypes = CostTypes {
                // UI 디폴트 설정 따름 => 아래 2개 항목 디폴트 false
                includeCredit = false
                includeRefund = false
            }

            this.timePeriod {
                val startTime = LocalDate.now().withDayOfMonth(1).toLocalDateTime()
                start = Instant.fromEpochSeconds(startTime.toLong(TimeUtil.UTC) / 1000)  //UTC 로 변경해야함.  한국시간으로 해버리면 하루 더 깍임
                end = Instant.fromEpochSeconds(3706473600) //디폴트 최대시간
            }
            this.timeUnit = TimeUnit.Monthly //기본 월단위

            /** 하드코딩된 비용 대신, 지난 비용 기반으로 계산 */
            this.autoAdjustData = AutoAdjustData {
                autoAdjustType = AutoAdjustType.Historical
                historicalOptions = HistoricalOptions {
                    budgetAdjustmentPeriod = 2 // 마지막 N개월 동안의 사용량을 기준으로 예산을 조정
                }
            }
        }

        val subs = run {
            val subEmail = emails.map {
                Subscriber {
                    subscriptionType = SubscriptionType.Email
                    address = it
                }
            }
            val subSns = snsList.map {
                Subscriber {
                    subscriptionType = SubscriptionType.Sns
                    address = it
                }
            }
            subEmail + subSns
        }
        check(subs.isNotEmpty())

        return CreateBudgetRequest {
            accountId = awsId
            this.budget = budget
            notificationsWithSubscribers = notifications.map {
                NotificationWithSubscribers {
                    notification = Notification {
                        notificationType = it.first
                        comparisonOperator = ComparisonOperator.GreaterThan
                        threshold = it.second
                    }
                    subscribers = subs
                }
            }
        }
    }


}
