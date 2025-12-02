package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.GroupDefinition
import aws.sdk.kotlin.services.costexplorer.model.GroupDefinitionType

/**
 * 서울리즌 안됨 주의!
 * https://docs.aws.amazon.com/ko_kr/awsaccountbilling/latest/aboutv2/ce-filtering.html
 *  */
object CostExplorerUtil {

    //==================================================== 코스트 종류 ======================================================

    /**
     * 진짜 청구된 정확한 비용 (가장 명확)
     * 할인이나 크레딧 적용 전의 실제 청구 비용
     */
    const val UNBLENDED_COST = "UnblendedCost"

    /**
     * 여러 계정 묶였을 때 '평균 단가'로 계산된 비용 (비추)
     * AWS Organizations에서 통합 결제 사용 시 모든 계정의 평균 요금으로 계산
     */
    const val BLENDED_COST = "BlendedCost"

    /**
     * RI/SP 비용을 사용량 기준으로 일 단위로 나눠 반영한 비용 (RI/SP 사용 시 추천)
     * Reserved Instance, Savings Plans의 선결제 비용을 기간에 걸쳐 분할 반영
     */
    const val AMORTIZED_COST = "AmortizedCost"

    /**
     * 크레딧·리펀드 반영한 AmortizedCost
     * 할인, 크레딧, 환불 등을 반영한 순수 비용
     */
    const val NET_AMORTIZED_COST = "NetAmortizedCost"

    /**
     * 크레딧·리펀드 반영한 UnblendedCost
     * 할인, 크레딧, 환불 등을 반영한 순수 비용
     */
    const val NET_UNBLENDED_COST = "NetUnblendedCost"

    /**
     * 실제 사용량 (GB, 시간 등)
     * 비용이 아닌 리소스 사용량 지표
     */
    const val USAGE_QUANTITY = "UsageQuantity"

    /**
     * EC2 RI 용량 비교를 위한 표준화 지표 (RI 분석 때만 필요)
     * 서로 다른 인스턴스 크기를 비교할 때 사용
     */
    const val NORMALIZED_USAGE_AMOUNT = "NormalizedUsageAmount"

    //==================================================== 기타 ======================================================

    /** 월 5$ 이내 저렴하게 나오는것들의 키값(서비스명, 태그) 무시  */
    val IGNORES: Set<String> = setOf("Tax", "AWS Cost Explorer", "AWS CodeCommit", "Route 53")

    /** 긴 단어를 일상적으로 사용하는 단어로 교체해줌 */
    val REPLACER: Map<String, String> = mapOf(
        "Simple Storage Service" to "S3",
        "EC2 - Other" to "EC2-Other",
        "Elastic Compute Cloud - Compute" to "EC2-Compute",
        "Elastic Load Balancing" to "ELB",
        "Elastic MapReduce" to "EMR",
        "Elasticsearch Service" to "Elasticsearch",
        "Relational Database Service" to "RDS",
    )

    //==================================================== 그룹 ======================================================

    /** 서비스 단위로 그룹바이 한다. ex) ec2, s3 등등  */
    val BY_SERVICE = GroupDefinition { this.key = "SERVICE"; this.type = GroupDefinitionType.Dimension }

    /**
     * 태그로 그룹바이
     * 주의!
     * 태그를 붙이고 비용설정에서 해당 태그를 활성화 시킨 후 부터 적용된다
     * @param tagKeys 여러 태그를 주입하면 and 조건으로 리턴된다.
     */
    fun groupByTags(tagKeys: List<String>): List<GroupDefinition> {
        return tagKeys.map {
            GroupDefinition {
                this.key = it
                this.type = GroupDefinitionType.Tag
            }
        }
    }

}