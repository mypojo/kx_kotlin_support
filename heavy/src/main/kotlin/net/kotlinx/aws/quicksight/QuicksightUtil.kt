package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.ResourcePermission
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.s3.S3Data

object QuicksightUtil {


    /**
     * 퀵사이트 S3 매니페스트
     * https://docs.aws.amazon.com/quicksight/latest/user/supported-manifest-file-format.html
     * CSV 말고 다른거는 커스텀 할것
     * */
    fun s3Manifest(files: List<S3Data>): String = obj {
        "fileLocations" to arr[
            obj {
                "URIs" to arr[files.map { it.toFullPath() }]
            }
        ]
        "globalUploadSettings" to obj {
            "format" to "CSV"
        }
    }.toString()

    /** 퀵사이트 권한으로 변경 */
    fun toPermissions(awsConfig: AwsConfig, users: List<String>): List<ResourcePermission> = users.map {
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

}