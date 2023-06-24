package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.GroupDefinition
import aws.sdk.kotlin.services.costexplorer.model.GroupDefinitionType

/**
 * 서울리즌 안됨 주의!
 * https://docs.aws.amazon.com/ko_kr/awsaccountbilling/latest/aboutv2/ce-filtering.html
 *  */
object CostExplorerUtil {

    /** 합산된 금액  */
    const val BLENDED_COST = "BlendedCost"

    /** 월 5$ 이내 저렴하게 나오는것들 무시  */
    val IGNORES: Set<String> = setOf("Tax", "AWS Cost Explorer", "AWS CodeCommit", "Route 53")

    /** 일상적으로 사용하는 단어로 교체해줌 */
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