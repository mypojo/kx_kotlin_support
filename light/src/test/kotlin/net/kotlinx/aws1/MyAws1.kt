package net.kotlinx.aws1

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.aws.toAwsClient1

object MyAws1 {

    val AWS_CONFIG = AwsConfig(profileName = "sin")
    val AWS = AWS_CONFIG.toAwsClient1()

    /** aws info */
    val AWS_INFO_LOADER: AwsInfoLoader by lazy {
        AwsInfoLoader(
            AWS,
            "xx",
            "yy",
        )
    }


}