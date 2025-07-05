package net.kotlinx.awscdk.iam

import net.kotlinx.core.Kdsl
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.PolicyStatementProps

/**
 * PolicyStatement 간단 변환을 위한 객체
 * 보기 너무 안예뻐서 DSL용 생성
 *  */
class CdkIamPolicyStatement {

    @Kdsl
    constructor(block: CdkIamPolicyStatement.() -> Unit = {}) {
        apply(block)
    }

    lateinit var actions: List<String>

    var resources: List<String> = IamPolicyAdminUtil.ALL

    /** 형식은 임의로 지정했음 */
    var conditions: Map<String, Any> = mapOf()

    /** 간단 변환 */
    fun toPolicyStatement(): PolicyStatement = PolicyStatement(PolicyStatementProps.builder().actions(actions).resources(resources).conditions(conditions).build())
}