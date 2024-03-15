package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkEnum
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.string.retainFrom
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.*

/** enum 설정 */
class CdkSecurityGroup(
    val sgName: String,
    /** ID 하드코딩된 맵 (캐시 등의 이유로 name으로 조회가 안될때) */
    var idMap: Map<DeploymentType, String> = emptyMap()
) : CdkEnum {

    override val logicalName: String
        get() = "${project.projectName}-sg_${sgName}-${deploymentType.name.lowercase()}"

    lateinit var iSecurityGroup: ISecurityGroup

    val feer: IPeer by lazy { Peer.securityGroupId(iSecurityGroup.securityGroupId) }

    /** 아웃바둔드 오픈 디폴트로 true */
    var allowAllOutbound: Boolean = true

    /** 결과 */
    lateinit var iVpc: IVpc

    fun create(stack: Stack, block: SecurityGroupProps.Builder.() -> Unit = {}): CdkSecurityGroup {
        val props = SecurityGroupProps.builder().vpc(iVpc).allowAllOutbound(allowAllOutbound).apply(block).build()
        iSecurityGroup = SecurityGroup(stack, logicalName, props)
        TagUtil.name(iSecurityGroup, logicalName)
        TagUtil.tag(iSecurityGroup, deploymentType)
        return this
    }

    /** 해당 포트 오픈 */
    fun open(port: Int, desc: String, peer: IPeer = Peer.anyIpv4()) {
        iSecurityGroup.addIngressRule(peer, Port.tcp(port), desc)
    }

    /**
     * 생성된 SG를 가져온다. name으로 검색해서 가져옴
     * 이거로 안되면 ID 하드코딩으로 찾으면 됨
     *  */
    fun load(stack: Stack, vpc: IVpc): CdkSecurityGroup {
        val queryString = "*${logicalName}**".retainFrom(RegexSet.ALPAH_NUMERIC.HAN).lowercase() //이거 이름으로 캐싱되니 주의! 삭제된게 자꾸 나온다면 검색어를 수정해야함
        iSecurityGroup = SecurityGroup.fromLookupByName(stack, sgName, queryString, vpc)
        return this
    }

    /** 네임으로 못찾을경우 임시 인식용 */
    fun load(stack: Stack, id: String): ISecurityGroup {
        return SecurityGroup.fromSecurityGroupId(stack, "sg_${this.sgName}-${deploymentType.name.lowercase()}", id)
    }

    /** 네임으로 못찾을경우 임시 인식용 */
    fun loadById(stack: Stack): ISecurityGroup {
        return load(stack, idMap[deploymentType]!!)
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