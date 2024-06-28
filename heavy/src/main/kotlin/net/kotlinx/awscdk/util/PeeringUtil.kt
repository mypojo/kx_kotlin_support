package net.kotlinx.awscdk.util

import net.kotlinx.aws.AwsConfig
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
     * peerOwnerId = 연결할 계정 ID
     * peerRegion = 연결할 계정 리즌
     * peerVpcId = 연결할 계정 VpcId
     * peerRoleArn
     *  #1. 일반적으로 admin이 담긴거 주면됨 & trust에 도 있어야함 (팀계정에 권한 부여하듯이 해주면됨) -> 보안문제 있을 수 있음
     *  #2. 최소 permission만 담아서 role 생성 & trust 부여 -> 귀찮음
     *  #3. 그냥 손으로 피어링 만들고 수락하기 -> 간편하지만 IAC 누락이 생겨서 찝찝함
     *
     * 실행하면 상태가  Pending acceptance  -> Active  로 변함 (좀 기다려야함)
     * 작업 후에는 정리를 위해서 반대편 피어링 커넥션에서 Naming을 해주자. (이게 naming까지 추가해주지는 않는다)
     *
     *
     *  */
    fun peering(stack: Stack, name: String, block: CfnVPCPeeringConnectionProps.Builder.() -> Unit) {
        val deploymentType = koin<DeploymentType>()
        val logicalName = "${name}-${deploymentType.name.lowercase()}"
        val peer = CfnVPCPeeringConnection(stack, "vpc_peering-${logicalName}", CfnVPCPeeringConnectionProps.builder().apply(block).build())
        TagUtil.name(peer, logicalName)
        TagUtil.tag(peer, deploymentType)
    }

    fun peering(stack: Stack, name: String,vpcId:String, ) {
        peering(stack, name) {
            vpcId(vpcId)
            peerOwnerId("992365606987")
            peerVpcId("vpc-051199316c97f0013")
            peerRegion(AwsConfig.SEOUL)
            peerRoleArn("arn:aws:iam::992365606987:role/DEV")
        }
    }

}