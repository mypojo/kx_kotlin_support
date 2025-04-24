package net.kotlinx.awscdk.iam

import software.amazon.awscdk.services.iam.ManagedPolicy


object IamManagedPolicySet {

    /**
     * SSM (주로 배스천 터널링)
     * 디폴트 세션 타임아웃 20분 -> 60분으로 늘려서 사용할것
     * https://ap-northeast-2.console.aws.amazon.com/systems-manager/session-manager/preferences?region=ap-northeast-2
     *  */
    val SSM = ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMFullAccess")

    /** 코드커밋 사용자 */
    val CODECOMMIT = ManagedPolicy.fromAwsManagedPolicyName("AWSCodeCommitPowerUser")

    /** AWS 백업 */
    val BACKUP = listOf(
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSBackupServiceRolePolicyForBackup"),
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSBackupServiceRolePolicyForRestores"),
        ManagedPolicy.fromAwsManagedPolicyName("AWSBackupServiceRolePolicyForS3Backup"),
        ManagedPolicy.fromAwsManagedPolicyName("AWSBackupServiceRolePolicyForS3Restore"),
    )


}