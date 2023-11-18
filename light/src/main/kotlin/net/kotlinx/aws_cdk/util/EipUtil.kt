package net.kotlinx.aws_cdk.util

import net.kotlinx.aws_cdk.component.CdkParameter
import net.kotlinx.core.DeploymentType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.CfnEIP


/**
 * 엘라스틱 IP는 미리 채번 후 사용하자
 * 실무에서는 IP 고정으로 사용해서 , 유실되면 안되는 일이 가끔 발생한다.
 * attrAllocationId 는 별도 스택에서 쓸거면 저장해야함
 * */
object EipUtil : KoinComponent {

    /** EIP 간단등록 */
    fun regist(stack: Stack, name: String = "nat", ip: CdkParameter? = null, id: CdkParameter? = null): CfnEIP {
        val deploymentType: DeploymentType by inject()
        val logicalName = "${name}-${deploymentType.name.lowercase()}"
        val eip = CfnEIP(stack, logicalName)
        ip?.let { it.put(stack, eip.attrPublicIp) }
        id?.let { it.put(stack, eip.attrAllocationId) }
        TagUtil.name(eip, logicalName)
        return eip
    }


}