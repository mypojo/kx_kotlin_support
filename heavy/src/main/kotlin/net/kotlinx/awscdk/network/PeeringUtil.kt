package net.kotlinx.awscdk.network

import net.kotlinx.aws.AwsConfig
import net.kotlinx.awscdk.basic.TagSet
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.koin.Koins.koin
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.CfnVPCPeeringConnection
import software.amazon.awscdk.services.ec2.CfnVPCPeeringConnectionProps


/**
 * 역할을 줘야하는데 복잡하다..
 * */
object PeeringUtil {

    /**
     * EIP 간단등록
     * 현재 시크릿(프로파일)을 사용해서 peerOwnerId 로 peerRoleArn 를 사용해 STS 한 후 peerVpcId 와 vpcId 를 이어주는 피어링을 생상한다.
     *
     * peerRegion = 연결할 계정 리즌
     * peerVpcId = 연결할 계정 VpcId
     * peerOwnerId = 연결할 계정 ID (외부 계정 연결시에만 필요)
     * peerRoleArn = 피어링 역할 (외부 계정 연결시에만 필요)
     *
     * ### 피어링 주의사항
     * 1. 일반적으로 admin이 담긴거 주면됨 & trust에 도 있어야함 (팀계정에 권한 부여하듯이 해주면됨) -> 보안문제 있을 수 있음
     * 2. 최소 permission만 담아서 role 생성 & trust 부여 -> 이게 정석이지만 귀찮음
     * 3. 그냥 손으로 피어링 만들고 수락하기 -> 간편하지만 IAC 누락이 생겨서 찝찝함 (비추천)
     *
     * 실행하면 상태가  Pending acceptance  -> Active  로 변함 (좀 기다려야함)
     * 작업 후에는 정리를 위해서 반대편 피어링 커넥션에서 Naming을 해주자. (이게 naming까지 추가해주지는 않는다)
     *
     * DNS settings 의 경우 아직 CDK에서 지원하지 않는듯함 (2024.11 기준)
     *
     * ### 피어링 이후 주의사항 (DB 기준)
     * 1. 피어링에 DNS 옵션 켜기  -> DB 엔드포인트가 IP가 아닌 도메인으로 되어있기때문에 해야함 -> 걍 손으로 하자
     * 2. 양쪽 라우팅 잡아줘야함. dev의 private에는 목적지가 prod IP 대역인경우 피어링으로, prod 의 isolated에는 목적지가 dev인 경우 피어링으로
     *  => 이유는 포스트그레스큐엘 같은경우 3way 핸드쉐이킹을 하기때문이라고 함
     * 3. DB의 시큐리티 그룹에 , 피어링을 추가해줘야함
     *  */
    fun peering(stack: Stack, name: String, block: CfnVPCPeeringConnectionProps.Builder.() -> Unit): CfnVPCPeeringConnection {
        val deploymentType = koin<DeploymentType>()
        val logicalName = "${name}-${deploymentType.name.lowercase()}"
        val peer = CfnVPCPeeringConnection(
            stack, "vpc_peering-${logicalName}", CfnVPCPeeringConnectionProps.builder()
                .peerRegion(AwsConfig.REGION_KR) //디폴트로 한국리전 넣어줌
                .apply(block)
                .build()
        )
        TagSet.Name.tag(peer, logicalName)
        TagUtil.tagDefault(peer)
        return peer
    }


}