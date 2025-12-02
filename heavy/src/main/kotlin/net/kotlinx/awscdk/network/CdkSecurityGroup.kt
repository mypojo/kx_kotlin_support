package net.kotlinx.awscdk.network

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.basic.CdkParameter
import net.kotlinx.awscdk.basic.TagSet
import net.kotlinx.awscdk.basic.TagUtil
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.*

/**
 * enum으로 설정하면됨.
 * 수정시 추가 후 삭제하는 방식 ->  5개 넘으면 오류남.. 엉성하다..
 * 이경우 Rule 다 삭제 -> 다시 추가 이런식으로 하면됨
 *
 * 주의!!
 * 최초 세칭은 룰 60개 밖에 안된다.
 * 개발 IP 화이트리스트 적용시 턱없이 부족하니, 리소스 요청 증가를 할것
 *  */
class CdkSecurityGroup(val sgName: String) : CdkEnum {

    override val logicalName: String
        get() = "${projectName}-sg_${sgName}-${suff}"

    /** 아웃바둔드 오픈 디폴트로 true */
    var allowAllOutbound: Boolean = true

    /** 대상 VPC */
    lateinit var iVpc: IVpc

    /** ID 참조용 파라메터 스토어 */
    val param: CdkParameter = CdkParameter("sg_${sgName}")

    /** 결과 */
    lateinit var iSecurityGroup: ISecurityGroup

    /** 결과 피어 */
    val feer: IPeer
        get() = Peer.securityGroupId(iSecurityGroup.securityGroupId)

    fun create(stack: Stack, block: SecurityGroupProps.Builder.() -> Unit = {}): CdkSecurityGroup {
        val props = SecurityGroupProps.builder().vpc(iVpc).allowAllOutbound(allowAllOutbound).apply(block).build()
        iSecurityGroup = SecurityGroup(stack, logicalName, props)
        TagSet.Name.tag(iSecurityGroup, logicalName)
        TagUtil.tagDefault(iSecurityGroup)
        param.put(stack, iSecurityGroup.securityGroupId)
        return this
    }

    /** 해당 포트 오픈 */
    fun open(port: Int, desc: String, peer: IPeer = Peer.anyIpv4()) {
        iSecurityGroup.addIngressRule(peer, Port.tcp(port), desc)
    }

    /** 아웃바운드 오픈 */
    fun openOutbound(port: Int, desc: String, peer: IPeer = Peer.anyIpv4()) {
        iSecurityGroup.addEgressRule(peer, Port.tcp(port), desc)
    }


//    /**
//     * 생성된 SG를 가져온다. name으로 검색해서 가져옴 -> 잘 안됨..
//     * 이거로 안되면 ID 하드코딩으로 찾으면 됨
//     *  */
//    @Deprecated("사용안함")
//    fun load(stack: Stack, vpc: IVpc): CdkSecurityGroup {
//        if (!this::iSecurityGroup.isInitialized) {
//            val queryString = "*${logicalName}**".retainFrom(RegexSet.ALPAH_NUMERIC.HAN).lowercase() //이거 이름으로 캐싱되니 주의! 삭제된게 자꾸 나온다면 검색어를 수정해야함
//            iSecurityGroup = SecurityGroup.fromLookupByName(stack, sgName, queryString, vpc)
//        }
//        return this
//    }

    /** ID로 로드한다. */
    fun load(stack: Stack): CdkSecurityGroup {
        try {
            val id = param.get(stack)
            iSecurityGroup = SecurityGroup.fromSecurityGroupId(stack, "sg_${this.sgName}-${suff}", id)
        } catch (e: Exception) {
            println(" -> [${stack.stackName}] object already loaded -> $logicalName")
        }
        return this
    }

    /**
     * 순환참조 제거용
     * ex) XxSecurityGroup.addIngressRule(stack, XxSecurityGroup.WEB.getName(deploymentType) + '-01', sgWeb.securityGroupId, 9000, 9000, 'spring boot admin')
     * 순환참조가 아닐경우 아래처럼 하면 됨
     * ex) sg_web.addIngressRule(Peer.securityGroupId(sg_web.securityGroupId), Port.tcp(9000), 'spring boot admin',);
     * ex) sgWeb.addIngressRule(Peer.anyIpv4(), Port.tcp(443), 'HTTPS',);
     *  */
    fun addIngressRule(stack: Stack, logicalId: String, sgId: String, from: Int, to: Int, desc: String) {
        CfnSecurityGroupIngressProps.builder()
            .groupId(sgId)
            .sourceSecurityGroupId(sgId)
            .fromPort(from)
            .toPort(to)
            .description(desc)
            .build()
        throw UnsupportedOperationException()
    }

}