package net.kotlinx.awscdk.network

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.CdkParameter
import net.kotlinx.awscdk.basic.TagSet
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.iam.AnyPrincipal
import software.amazon.awscdk.services.iam.PolicyStatement


/**
 * VPC 관련 설졍
 * 가능하면 한번에 서브넷을 구성하는게 좋고,
 * 그게 아니라면 좀 복잡해진다.. (수동으로 서브넷 나누고 할당 등..)
 *
 * koin 으로 등록해서 사용하자.
 *  */
class CdkVpc : CdkInterface {

    @Kdsl
    constructor(block: CdkVpc.() -> Unit = {}) {
        apply(block)
    }

    /** 이거 전체 회사내에서 안겹치게 잘 해야함!! (겹치면 피어링 안됨) */
    var vpcCidr: String = "10.1.0.0/16"

    /** 최초  */
    var cidrMask: Int = 24

    /**
     * 보통 NAT가 ALB에 비해서 3배 정도 비싸니 확인할것
     * ALB 월비용 16$ / EIP비용 11$
     * NAT가 있어야 ip 화이트리스트 업무에 대응 가능하다
     * NAT 월비용 44$
     * */
    var subnetTypes: List<SubnetType> = listOf(SubnetType.PUBLIC, SubnetType.PRIVATE_WITH_EGRESS)
    var maxAzs: Int = 2

    /** NAT 제거 가능하면 하지말것! 가끔 CDK 꼬이면 버그 있음 */
    var natGateways: Int = 1

    /** VPC 이름 */
    override val logicalName: String = "${projectName}_vpc-${suff}"

    val feer: IPeer
        get() = Peer.ipv4(iVpc.vpcCidrBlock)

    var subnetCnt: Int = subnetTypes.size * maxAzs //초기화 이후에 사용

    /** 결과 */
    lateinit var iVpc: IVpc

    /**
     * 설정할 GatewayVpcEndpointAwsService
     * 디폴트로는 무료 2종 넣어준다
     * 주의! IP 화아트리스트로 S3 버킷을 다운로드 해야하는경우 이걸 꺼야 상대방에서 NAT를 통과한 IP로 허용할 수 있다
     * */
    var vpcEndpointServices: List<GatewayVpcEndpointAwsService> = listOf(GatewayVpcEndpointAwsService.S3, GatewayVpcEndpointAwsService.DYNAMODB)

    /**
     * 필요에따라 오버라이드. ex) VPC 하나에 다수의 프로젝트 설정
     *  -> 여기서 서브넷 추가시,  vpc에서 서브넷을 조회할때 최초 생성된 서브넷만 조회된다. 이거말고도 문제가 많음..
     *  따라서 프로젝트별로 VPC를 만들고 그냥 이들을 연결하자
     *
     *  주의!! 디폴트(main) ACL, SG , 라우팅테이블 등의 이름은 지정해주지 못한다
     *  */
    fun create(stack: Stack, block: VpcProps.Builder.() -> Unit = {}) {
        val vpcProps = VpcProps.builder()
            .ipAddresses(IpAddresses.cidr(vpcCidr))
            .natGateways(natGateways)
            .vpcName(logicalName)
            .maxAzs(maxAzs)
            .subnetConfiguration(subnetTypes.map { subnetConfiguration(awsConfig.profileName!!, it) })
            .apply(block)
            .build()
        iVpc = Vpc(stack, logicalName, vpcProps)
        VPC_ID.put(stack, iVpc.vpcId)
        subnetRename() //간단으로 만든건 이름 이상하게 나옴.
        gatewayVpcEndpoint()
        TagUtil.tagDefault(iVpc) //이거 하나 달면 전체?
    }


    /**
     * 기본 24 마스크만 해당
     * 간단 서브넷 & NACL 추가,  -> NAT나 gatewayVpcEndpoint 등 수동으로 매핑해야해서 안씀..
     *  */
    fun addPrivateSubnet(stack: Stack, projectName: String, entrys: Map<String, CommonNetworkAclEntryOptions>) {
        check(cidrMask == 24) { "cidrMask 24 만 지원" }
        val type = "private"
        val block = vpcCidr.split(".").take(2).joinToString(".") //앞의 2개 자리만 잘라줌
        val subnets = iVpc.availabilityZones.map { az ->
            val zoneName = az.substring(az.length - 1)
            val subnetName = "${projectName}_subnet_$type/${zoneName}-${suff}"
            val subnet = PrivateSubnet.Builder.create(stack, subnetName)
                .vpcId(iVpc.vpcId).availabilityZone(az).cidrBlock("${block}.${subnetCnt++}.0/${cidrMask}").build()
            TagSet.Name.tag(subnet, subnetName)
            subnet
        }

        val naclName = "${projectName}_nacl_${type}-${suff}"
        val nacl = NetworkAcl.Builder.create(stack, naclName).vpc(iVpc).subnetSelection(
            SubnetSelection.builder().subnets(subnets).build()
        ).build()
        entrys.forEach { nacl.addEntry("${naclName}_${it.key}", it.value) }
        TagSet.Name.tag(nacl, naclName)
    }

    /** 서브넷 설정으로 변환 */
    fun subnetConfiguration(name: String, subnetType: SubnetType): SubnetConfiguration =
        SubnetConfiguration.builder().name("${name}_${subnetType.name.lowercase()}/${deploymentType}").subnetType(subnetType).cidrMask(cidrMask).build()!!

    /** VPC 내의 서브넷 네이밍 테그 강제 수정 (디폴트는 너무 길고 이상함) */
    fun subnetRename() {
        listOf(
            "public" to iVpc.publicSubnets,
            "private" to iVpc.privateSubnets,
            "isolated" to iVpc.isolatedSubnets,
        ).forEach { (subnetSuffix, subnets) ->
            subnets.forEach { subnet ->
                val zoneName = subnet.availabilityZone.substring(subnet.availabilityZone.length - 1)
                val name = subnet.toString().substringAfterLast("/").substringBefore("_") //toString 해야 name 이 나온다
                TagSet.Name.tag(subnet, "${name}_subnet_${subnetSuffix}/${zoneName}_${suff}")
            }
        }
    }

    /**
     * name으로는 못가져올 수 있다. (미삭제 등등).
     * 이때문에 그냥 ID로 가져온다
     *  */
    fun load(stack: Stack): CdkVpc {
        val vpcId = VPC_ID.get(stack)
        try {
            iVpc = Vpc.fromLookup(stack, logicalName, VpcLookupOptions.builder().vpcId(vpcId).isDefault(false).build())
        } catch (e: Exception) {
            println(" -> [${stack.stackName}] object already loaded -> $logicalName")
        }
        return this
    }

    /**
     * 공짜 2개 기본 세팅 하기
     * 주의!!! 외부 S3 리소스가 NAT IP를 대상으로 화이트리스트 필터링을 사용한다면 이걸 사용하면 안된다!
     *  => 그냥 끄거나, 라우팅 테이블을 편집하거나 해야할듯
     *  */
    fun gatewayVpcEndpoint(block: PolicyStatement.Builder.() -> Unit = {}) {
        vpcEndpointServices.forEach { service ->
            val serviceName = service.name.substringAfterLast(".")
            val endpointName = "${this.projectName}_${this.deploymentType}_endpoint_${serviceName}"
            val endpoint = iVpc.addGatewayEndpoint(endpointName, GatewayVpcEndpointOptions.builder().service(service).build())
            endpoint.addToPolicy(
                PolicyStatement.Builder.create()
                    .principals(listOf(AnyPrincipal()))
                    .actions(listOf("*"))
                    .resources(listOf("*"))
                    .apply(block)
                    .build()
            )
            TagSet.Name.tag(endpoint, endpointName) //지금 버그로 태그 입력 안됨 (2022.01)
        }
    }

    /**
     * 해당 서브타입 전체에 적용됨
     * 하위 프로젝트에서 그대로 호출해도 추가된 부분만 잘 적용됨
     *  */
    fun nacl(stack: Stack, subnetType: SubnetType, entrys: Map<String, CommonNetworkAclEntryOptions>) {
        val naclId = "${projectName}_nacl_${subnetType.name.lowercase()}-${suff}"
        val nacl = NetworkAcl.Builder.create(stack, naclId).vpc(iVpc).subnetSelection(
            SubnetSelection.builder().subnetType(subnetType).build()
        ).build()
        entrys.forEach { nacl.addEntry("${naclId}_${it.key}", it.value) }
        TagSet.Name.tag(nacl, naclId)
    }

    companion object {
        /** 스토어드 파라메터용  */
        val VPC_ID = CdkParameter("vpc_id")
    }


}