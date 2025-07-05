package net.kotlinx.awscdk.iam

import net.kotlinx.aws.AwsConfig
import net.kotlinx.awscdk.iam.IamPolicyAdminUtil.ALL
import net.kotlinx.koin.Koins
import software.amazon.awscdk.services.iam.ManagedPolicy

/** 자주 사용되는거 모음 */
object CdkIamPolicySet {

    const val SCHEDULER = "AmazonEventBridgeSchedulerFullAccess"

    /**
     * SSM을 사용할 수 있는 역할
     * ex) 백스천 호스트 서버
     *  */
    const val SSM = "AmazonSSMManagedInstanceCore"

    /**
     * SSM (주로 배스천 터널링)
     * 디폴트 세션 타임아웃 20분 -> 60분으로 늘려서 사용할것
     * https://ap-northeast-2.console.aws.amazon.com/systems-manager/session-manager/preferences?region=ap-northeast-2
     *  */
    val SSM_FULL = ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMFullAccess")

    /** ECS 단순 실행 역할 */
    const val ECS = "service-role/AmazonECSTaskExecutionRolePolicy"

    /** 코드커밋 사용자 */
    val CODECOMMIT = ManagedPolicy.fromAwsManagedPolicyName("AWSCodeCommitPowerUser")

    /** AWS 백업 */
    val BACKUP = listOf(
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSBackupServiceRolePolicyForBackup"),
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSBackupServiceRolePolicyForRestores"),
        ManagedPolicy.fromAwsManagedPolicyName("AWSBackupServiceRolePolicyForS3Backup"),
        ManagedPolicy.fromAwsManagedPolicyName("AWSBackupServiceRolePolicyForS3Restore"),
    )

    /**
     * 배드락 어드민
     * 내부에서 람다를 호출하더라도, 람다는 리소스베이스로 권한이 세팅되어있어서 상관없음
     * */
    val BEDROCK_ADMIN = CdkIamPolicy {
        policyName = "app_bedrock_admin"
        statements = listOf(
            CdkIamPolicyStatement {
                actions = listOf(
                    "bedrock:*",
                    "bedrock-agent:*",
                    "bedrock-agent-runtime:*",
                )
                resources = ALL
            }
        )
    }

    /**
     * 사용자 기본권한
     * ex) 비번 변경 등
     * */
    fun userDeafult(awsId: String = Koins.koin<AwsConfig>().awsId): CdkIamPolicy {
        return CdkIamPolicy {
            policyName = "app_user_default"
            statements = listOf(
                CdkIamPolicyStatement {
                    actions = listOf(
                        "iam:ChangePassword", //셀프 비번 변경 (최초 로그인 OR 직접수정)
                        "iam:ListUsers",      //리스팅은 해야 진입하지..
                        //이하 키 발급
                        "iam:DeleteAccessKey",
                        "iam:GetAccessKeyLastUsed",
                        "iam:UpdateAccessKey",
                        "iam:CreateAccessKey",
                        "iam:ListAccessKeys",
                    )
                    resources = listOf(
                        "arn:aws:iam::${awsId}:user/\${aws:username}" //$ 이스케이핑됨 주의!!
                    )
                }
            )
        }
    }


}