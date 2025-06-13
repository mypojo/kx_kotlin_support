package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.QuickSightClient
import aws.sdk.kotlin.services.quicksight.generateEmbedUrlForAnonymousUser
import aws.sdk.kotlin.services.quicksight.model.AnonymousUserDashboardVisualEmbeddingConfiguration
import aws.sdk.kotlin.services.quicksight.model.AnonymousUserEmbeddingExperienceConfiguration
import aws.sdk.kotlin.services.quicksight.model.DashboardVisualId
import aws.sdk.kotlin.services.quicksight.model.GenerateEmbedUrlForAnonymousUserResponse
import net.kotlinx.aws.awsConfig
import kotlin.time.Duration.Companion.hours

data class QuickSightEmbedUrlReq(
    val dashboardId: String,
    val visualId: String,
    val sessionTags: Map<String, String> = emptyMap()
)

/**
 * 익명 유저 임베드 URL 생성
 * 만들어진 URL은 5분 이내로 사용되어야함
 * 만들어지면 sessionLifetimeInMinutes 만큼 유지됨 -> 이건 보안옵션이며 10시간 지정해도 20분 쓴다면 1세션만 과금됨
 * */
suspend fun QuickSightClient.generateEmbedUrlForAnonymousUser(config: QuickSightEmbedUrlReq): GenerateEmbedUrlForAnonymousUserResponse {
    val awsConfig = this.awsConfig
    return this.generateEmbedUrlForAnonymousUser {
        awsAccountId = awsConfig.awsId
        namespace = "default"  //기본 쓰면 될듯?
        sessionLifetimeInMinutes = 10.hours.inWholeMinutes
        authorizedResourceArns = listOf("arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:dashboard/${config.dashboardId}")
        experienceConfiguration = AnonymousUserEmbeddingExperienceConfiguration {
            dashboardVisual = AnonymousUserDashboardVisualEmbeddingConfiguration {
                initialDashboardVisualId = DashboardVisualId {
                    this.dashboardId = config.dashboardId
                    this.visualId = config.visualId
                }
            }
        }
        this.sessionTags = config.sessionTags.map { (key, value) -> 
            aws.sdk.kotlin.services.quicksight.model.SessionTag {
                this.key = key
                this.value = value
            }
        }
    }
}