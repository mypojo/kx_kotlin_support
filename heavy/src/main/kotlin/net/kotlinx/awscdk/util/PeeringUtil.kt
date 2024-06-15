package net.kotlinx.awscdk.util

import net.kotlinx.system.DeploymentType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.CfnVPCPeeringConnection
import software.amazon.awscdk.services.ec2.CfnVPCPeeringConnectionProps


/**
 * 역할을 줘야하는데 복잡하다..
 * */
object PeeringUtil : KoinComponent {

    /**
     * EIP 간단등록
     * 실행 role 에 입력한 role 을 assume 할 수 있는 역할을 줘한다.
     * 일반적으로 admin 역할을 assume 할텐데, 거기에 이 계정의 역할도 같이 trust에 추가해주면 됨
     * 실행하면 상태가  Pending acceptance  -> Active  로 변함 (좀 기다려야함)
     * 작업 후에는 정리를 위해서 반대편 피어링 커넥션에서 Naming을 해주자.
     *
     * ex)        "Principal": {
     *                 "AWS": [
     *                     "arn:aws:iam::xx:root",
     *                     "arn:aws:iam::yy:root"
     *                 ]
     *             },
     *             "Action": "sts:AssumeRole",
     *  */
    fun peering(stack: Stack, name: String, block: CfnVPCPeeringConnectionProps.Builder.() -> Unit) {
        val deploymentType = get<DeploymentType>()
        val logicalName = "${name}-${deploymentType.name.lowercase()}"
        val peer = CfnVPCPeeringConnection(stack, "vpc_peering-${logicalName}", CfnVPCPeeringConnectionProps.builder().apply(block).build())
        TagUtil.name(peer, logicalName)
        TagUtil.tag(peer, deploymentType)
    }


}