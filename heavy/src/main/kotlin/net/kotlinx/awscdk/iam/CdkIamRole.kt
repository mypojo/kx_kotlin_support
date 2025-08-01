package net.kotlinx.awscdk.iam

import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.*

/**
 * 자주 사용되는 IAM 정의해서 하드코딩 방지
 * */
class CdkIamRole {

    @Kdsl
    constructor(block: CdkIamRole.() -> Unit = {}) {
        apply(block)
    }

    /**
     * 대분류는 -, 소분류는 _ 로 분리
     * ex) app-admin-ecs_task
     *  */
    lateinit var roleName: String

    /**
     * 신뢰할 수 있는 assume 서비스명
     * ex) ecs-tasks.amazonaws.com
     * 여기 데이터가 있으면 자동으로 assumedBy 로 변경해줌
     *
     * 제거하고 싶은데, 많이 쓰고있어서 일단 그냥둠
     * */
    var services: List<String> = emptyList()

    /**
     * STS 가능한 Principal
     * 서비스 or IAM 계정 등등
     * ex) listOf(ArnPrincipal("arn:aws:iam::aaaa:root")
     *  */
    var assumedBy: List<IPrincipal> = emptyList()

    /** 인라인 액션들. ADMIN 역할이 아니라면 다 지정할것 */
    var actions: List<String> = emptyList()

    /**
     * ADMIN 역할이 아니라면 다 지정할것
     * ex) ManagedPolicy.fromAwsManagedPolicyName("AWSCodeCommitPowerUser")
     * */
    var managedPolicy: List<IManagedPolicy> = emptyList()

    /** 간단 변환 */
    fun managedPolicy(vararg name: String) {
        managedPolicy = name.map { ManagedPolicy.fromAwsManagedPolicyName(it) }
    }

    /** 권한 가져옴 (다른 스택에서) */
    fun load(stack: Stack): CdkIamRole {
        try {
            iRole = Role.fromRoleName(stack, this.roleName, this.roleName)
        } catch (e: Exception) {
            println(" -> [${stack.stackName}] object already loaded -> $roleName")
        }
        return this
    }

    /** 결과 */
    lateinit var iRole: IRole

    /**
     * 권한 생성
     *  */
    fun create(stack: Stack): CdkIamRole {

        if (services.isNotEmpty()) {
            assumedBy += services.map { ServicePrincipal(it) }
        }

        val inlinePolicies = run {
            if (actions.isEmpty()) {
                emptyMap()
            } else {
                mapOf(
                    "${roleName}-only" to PolicyDocument(
                        PolicyDocumentProps.builder().statements(
                            listOf(
                                PolicyStatement(
                                    PolicyStatementProps.builder()
                                        .effect(Effect.ALLOW)
                                        .resources(listOf("*"))
                                        .actions(actions)
                                        .build()
                                )
                            )
                        ).build()
                    )
                )
            }
        }
        iRole = Role(
            stack, this.roleName, RoleProps.builder()
                .roleName(roleName)
                .assumedBy(CompositePrincipal(*assumedBy.toTypedArray()))
                .managedPolicies(managedPolicy) //관리형 추가
                .inlinePolicies(inlinePolicies) //인라인 추가
                .build()
        )
        return this
    }

}

