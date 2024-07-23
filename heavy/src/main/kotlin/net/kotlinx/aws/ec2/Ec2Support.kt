package net.kotlinx.aws.ec2

import aws.sdk.kotlin.services.ec2.*
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


/**
 * NAT 삭제
 * ALB가 시간당 0.0225 인대 비해 NAT는 $0.059 로 두배이상 비싸다. 이때문에 생성/삭제 컨트롤리 필요할 수 있다.
 * 삭제에 한참 걸림..
 *  */
suspend fun Ec2Client.deleteNatGateway(natGatewayId: String) {
    this.deleteNatGateway {
        this.natGatewayId = natGatewayId
    }
}

/**
 * NAT 생성
 * 주의!! 라우팅 테이블 편집은 별도로 또 해줘야 한다!
 *  */
suspend fun Ec2Client.createNatGateway(publicSubnetId: String, allocationId: String) {
    this.createNatGateway {
        this.subnetId = publicSubnetId
        this.allocationId = allocationId
    }
    this.replaceRoute {
        ///
    }
}

/** 나중에 시간나면 자동 라우팅 만들자 */
data class VpcInfo(
    val publicSubnetId: String,
    val privateSubnetId: String,
    val natGatewayId: String,
    /** 엘라스틱 IP의 ID */
    val allocationId: String,
)

