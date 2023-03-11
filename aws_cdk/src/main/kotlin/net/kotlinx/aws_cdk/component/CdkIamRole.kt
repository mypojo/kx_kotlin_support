package net.kotlinx.aws_cdk.component

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.iam.*

/**
 * 자주 사용되는 IAM 정의해서 하드코딩 방지
 * */
class CdkIamRole(
    /**
     * 대분류는 -, 소분류는 _ 로 분리
     * ex) app-admin-ecs_task
     *  */
    val roleName: String,
    /**
     * 신뢰할 수 있는 assume 서비스명
     * ex) ecs-tasks.amazonaws.com
     * */
    val services: List<String>,

    /** 인라인 액션들. ADMIN 역할이 아니라면 다 지정할것 */
    val actions: List<String>,

    /** ADMIN 역할이 아니라면 다 지정할것 */
    val fixedManagedPolicies: List<IManagedPolicy>,

    ) {

    var irole: IRole? = null

    /** 권한 가져옴 (다른 스택에서) */
    fun load(stack: Stack): CdkIamRole {
        irole = Role.fromRoleName(stack, this.roleName, this.roleName)
        return this
    }

    /**
     * 권한 생성
     * @param managedPolicy 관리자 통합 권한 등, 수동으로 관리할때 사용
     *  */
    fun create(stack: Stack, managedPolicy: List<IManagedPolicy> = fixedManagedPolicies): CdkIamRole {
        check(services.isNotEmpty())
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
        irole = Role(
            stack, this.roleName, RoleProps.builder()
                .roleName(roleName)
                .assumedBy(
                    CompositePrincipal(
                        *services.map { ServicePrincipal(it) }.toTypedArray()
                    )
                )
                .managedPolicies(managedPolicy) //관리형 추가
                .inlinePolicies(inlinePolicies) //인라인 추가
                .build()
        )
        return this
    }

}