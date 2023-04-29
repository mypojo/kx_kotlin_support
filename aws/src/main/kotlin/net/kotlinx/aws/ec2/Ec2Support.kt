package net.kotlinx.aws.ec2

import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.describeNetworkInterfaces
import aws.sdk.kotlin.services.ec2.model.Filter
import aws.sdk.kotlin.services.ec2.model.NetworkInterface
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

/** 네트워크 정보 (IP) 가져오기 (확인필요) */
suspend fun Ec2Client.describeNetworkInterfaces(hostName: String): List<NetworkInterface> = this.describeNetworkInterfaces {
    filters = listOf(
        Filter {
            name = "private-dns-name"
            values = listOf(hostName)
        }
    )
}.networkInterfaces!!