package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.util.IamPolicyUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.*

/**
 * PolicyStatement 간단 변환을 위한 객체
 * 보기 너무 안예뻐서 DSL용 생성
 *  */
class CdkPolicyStatement {

    @Kdsl
    constructor(block: CdkPolicyStatement.() -> Unit = {}) {
        apply(block)
    }

    lateinit var actions: List<String>
    var resources: List<String> = IamPolicyUtil.ALL

    /** 간단 변환 */
    fun toPolicyStatement(): PolicyStatement = PolicyStatement(PolicyStatementProps.builder().actions(actions).resources(resources).build())
}

//        "StringEquals": {
//            "aws:ResourceTag/IamGroup": [
//            "AAAA"
//            ]
//        },
//        "ForAnyValue:StringEqualsIfExists": {
//            "aws:ResourceTag/IamGroup": [
//            "QQ"
//            ]
//        }

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
    lateinit var statements: List<CdkPolicyStatement>

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