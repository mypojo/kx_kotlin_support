package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.ResourcePermission
import net.kotlinx.aws.AwsConfig

object QuicksightPermissionUtil {

    /** 데이터셋 */
    fun toDataSet(awsConfig: AwsConfig, users: List<String>): List<ResourcePermission> = users.map {
        ResourcePermission {
            principal = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:user/default/${it}"
            actions = listOf(
                "quicksight:PassDataSet",
                "quicksight:DescribeIngestion",
                "quicksight:CreateIngestion",
                "quicksight:UpdateDataSet",
                "quicksight:DeleteDataSet",
                "quicksight:DescribeDataSet",
                "quicksight:CancelIngestion",
                "quicksight:DescribeDataSetPermissions",
                "quicksight:ListIngestions",
                "quicksight:UpdateDataSetPermissions",
            )
        }
    }

    /** 데이터소스 권한으로 변경 */
    fun toDataSource(awsConfig: AwsConfig, users: List<String>): List<ResourcePermission> = users.map {
        ResourcePermission {
            principal = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:user/default/${it}"
            actions = listOf(
                "quicksight:DescribeDataSource",
                "quicksight:DescribeDataSourcePermissions",
                "quicksight:PassDataSource",
                "quicksight:UpdateDataSource",
                "quicksight:DeleteDataSource",
                "quicksight:UpdateDataSourcePermissions",
            )
        }
    }

    /** 폴더 권한으로 변경 */
    fun toFolder(awsConfig: AwsConfig, users: List<String>): List<ResourcePermission> = users.map {
        ResourcePermission {
            principal = "arn:aws:quicksight:${awsConfig.region}:${awsConfig.awsId}:user/default/${it}"
            actions = listOf(
                "quicksight:CreateFolder",
                "quicksight:DescribeFolder",
                "quicksight:UpdateFolder",
                "quicksight:DeleteFolder",
                "quicksight:CreateFolderMembership",
                "quicksight:DeleteFolderMembership",
                "quicksight:DescribeFolderPermissions",
                "quicksight:UpdateFolderPermissions",
            )
        }
    }

}