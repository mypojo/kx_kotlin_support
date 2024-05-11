package net.kotlinx.awscdk.util

import software.amazon.awscdk.services.ec2.*


/** 자주 사용하는 설정 */
object NaclUtil {

    /** 해당 포트 전체 오픈 */
    fun portOpen(ruleNum: Int, port: Int, cidr: String? = null): Pair<String, CommonNetworkAclEntryOptions> {
        val name = "inbound-open-$ruleNum-$port"
        return name to CommonNetworkAclEntryOptions.builder()
            .networkAclEntryName(name)
            .ruleNumber(ruleNum)
            .cidr(cidr?.let { AclCidr.ipv4(cidr) } ?: AclCidr.anyIpv4()) //IP 대역이 지정되지 않으면 any로 간주
            .traffic(AclTraffic.tcpPort(port))
            .direction(TrafficDirection.INGRESS)
            .ruleAction(Action.ALLOW)
            .build()!!
    }

    /** 응답에 사용되는 임시포트. 외부 연결시 응답 받아야 해서 전부 열어줘야함  */
    val DEFAULT_IN_TEMP: Pair<String, CommonNetworkAclEntryOptions> = "inbound-open-temp" to CommonNetworkAclEntryOptions.builder()
        .networkAclEntryName("inbound-open-temp")
        .ruleNumber(900)
        .cidr(AclCidr.anyIpv4())
        .traffic(AclTraffic.tcpPortRange(1024, 65535))
        .direction(TrafficDirection.INGRESS)
        .ruleAction(Action.ALLOW)
        .build()!!

    /** NACL_OUT 디폴트 = 전체 오픈. */
    val DEFAULT_OUT: Pair<String, CommonNetworkAclEntryOptions> = "outbound-all-open" to CommonNetworkAclEntryOptions.builder()
        .networkAclEntryName("outbound-all-open")
        .ruleNumber(100)
        .cidr(AclCidr.anyIpv4())
        .traffic(AclTraffic.allTraffic())
        .direction(TrafficDirection.EGRESS)
        .ruleAction(Action.ALLOW)
        .build()!!


}