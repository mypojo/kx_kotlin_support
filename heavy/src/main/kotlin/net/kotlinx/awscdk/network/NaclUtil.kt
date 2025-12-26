package net.kotlinx.awscdk.network

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

    /**
     * 요청을 보내면 응답을 보내주는 임시포트. stateless이기 때문에 열어줘야함.
     * - source에 VPC 뿐만 아니라 전체를 지정해서 열어줘야함. (외부 요청하면 VPC가 아닌 IP가 인바운드 된다)
     * - NAT 게이트웨이가 사용함
     * - ssm 게이트웨이 등 private 서브넷 끼리 통신할때 사용됨
     * - ex) private02 SSM 게이트웨이가 private 01 서버에 접근할때. 443도 쓰지만 임시 포트도 왕복으로 사용되어야 한다.
     * - 메인 포트인 8080도 여기 포함된다
     * - NACL은 보통 이 포트를 망 가리지 않고 다 열어줌. 따라서 멘션 생략
     *  */
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