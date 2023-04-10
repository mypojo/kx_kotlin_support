package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.DeploymentType
import net.kotlinx.core1.regex.RegexSet
import net.kotlinx.core1.string.retainFrom
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.*

class CdkSecurityGroup(
    val project: CdkProject,
    val deploymentType: DeploymentType,
    val sgName: String,
) : CdkInterface {

    override val logicalName: String
        get() = "${project.projectName}-sg_${sgName}-${deploymentType}"

    lateinit var iSecurityGroup: ISecurityGroup

    val feer: IPeer
        get() = Peer.securityGroupId(iSecurityGroup.securityGroupId)

    /**
     * @param allowAllOutbound 아웃바둔드 오픈 디폴트로 true
     * */
    fun create(stack: Stack, iVpc: IVpc, allowAllOutbound: Boolean = true): CdkSecurityGroup {
        iSecurityGroup = SecurityGroup(stack, logicalName, SecurityGroupProps.builder().vpc(iVpc).allowAllOutbound(allowAllOutbound).build())
        TagUtil.name(iSecurityGroup, logicalName)
        TagUtil.tag(iSecurityGroup, deploymentType)
        return this
    }

    /** 해당 포트 오픈 */
    fun open(port: Int, desc: String, peer: IPeer = Peer.anyIpv4()) {
        iSecurityGroup.addIngressRule(peer, Port.tcp(port), desc)
    }

    /** 생성된 SG를 가져온다. name으로 검색해서 가져옴  */
    fun load(stack: Stack, vpc: IVpc): CdkSecurityGroup {
        val queryString = "*${logicalName}**".retainFrom(RegexSet.ALPAH_NUMERIC_HAN).lowercase() //이거 이름으로 캐싱되니 주의! 삭제된게 자꾸 나온다면 검색어를 수정해야함
        iSecurityGroup = SecurityGroup.fromLookupByName(stack, sgName, queryString, vpc)
        return this
    }

//    /**
//     * 순환참조 제거용
//     * ex) XxSecurityGroup.addIngressRule(stack, XxSecurityGroup.WEB.getName(deploymentType) + '-01', sgWeb.securityGroupId, 9000, 9000, 'spring boot admin')
//     * 순환참조가 아닐경우 아래처럼 하면 됨
//     * ex) sg_web.addIngressRule(Peer.securityGroupId(sg_web.securityGroupId), Port.tcp(9000), 'spring boot admin',);
//     * ex) sgWeb.addIngressRule(Peer.anyIpv4(), Port.tcp(443), 'HTTPS',);
//     *  */
//    public static addIngressRule(stack: Stack, logicalId: string, sgId: string, from: number, to: number, desc: string)
//    {
//        new CfnSecurityGroupIngress (stack, logicalId, { groupId: sgId,
//                                                         sourceSecurityGroupId: sgId,
//                                                         fromPort: from, toPort: to,
//                                                         ipProtocol: 'tcp',
//                                                         description: desc
//    })
//    }

}