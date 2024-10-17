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
     * */
    lateinit var services: List<String>

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
        iRole = Role(
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

/** 자주 사용되는거 모음 */
object CdkManagedPolicySet {

    const val SCHEDULER = "AmazonEventBridgeSchedulerFullAccess"

    /**
     * SSM을 사용할 수 있는 역할
     * ex) 백스천 호스트 서버
     *  */
    const val SSM = "AmazonSSMManagedInstanceCore"

    /** ECS 단순 실행 역할 */
    const val ECS = "service-role/AmazonECSTaskExecutionRolePolicy"

}