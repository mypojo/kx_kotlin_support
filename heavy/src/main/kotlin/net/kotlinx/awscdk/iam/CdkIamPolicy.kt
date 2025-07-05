package net.kotlinx.awscdk.iam

import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.IManagedPolicy
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.awscdk.services.iam.ManagedPolicyProps

/**
 * 커스텀 정첵 설정
 * ex) 데싸 팀에게 athena 콛솔 오픈
 * */
class CdkIamPolicy {

    @Kdsl
    constructor(block: CdkIamPolicy.() -> Unit = {}) {
        apply(block)
    }

    /** 이름 */
    lateinit var policyName: String

    /** statements 들 */
    lateinit var statements: List<CdkIamPolicyStatement>

    /** 결과 */
    lateinit var iManagedPolicy: IManagedPolicy

    /**
     * 권한 생성
     *  */
    fun create(stack: Stack) {
        this.iManagedPolicy = ManagedPolicy(
            stack, policyName + "_policy", ManagedPolicyProps.builder()
                .managedPolicyName(policyName)
                .description(policyName)
                .statements(statements.map { it.toPolicyStatement() })
                .build()
        )
    }

}