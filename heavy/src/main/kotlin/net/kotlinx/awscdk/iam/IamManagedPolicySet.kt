package net.kotlinx.awscdk.iam

import software.amazon.awscdk.services.iam.ManagedPolicy


object IamManagedPolicySet {

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