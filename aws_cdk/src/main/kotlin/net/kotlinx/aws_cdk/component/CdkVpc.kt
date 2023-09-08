package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.iam.AnyPrincipal
import software.amazon.awscdk.services.iam.PolicyStatement


/**
 * VPC 관련 설졍
 * 가능하면 한번에 서브넷을 구성하는게 좋고,
 * 그게 아니라면 좀 복잡해진다.. (수동으로 서브넷 나누고 할당 등..)
 *  */
class CdkVpc(
    val project: CdkProject,
    override var deploymentType: DeploymentType = DeploymentType.dev,
    block: CdkVpc.() -> Unit = {}
) : CdkDeploymentType {

    var vpcCidr: String = "10.1.0.0/16"

    /** 최초  */
    var cidrMask: Int = 24
    var subnetTypes: List<SubnetType> = listOf(SubnetType.PUBLIC, SubnetType.PRIVATE_WITH_EGRESS)
    var maxAzs: Int = 2
    var natGateways: Int = 1

    /** VPC 이름 */
    override val logicalName: String = "${project.projectName}_vpc_${deploymentType}"

    val feer: IPeer
        get() = Peer.ipv4(iVpc.vpcCidrBlock)

    init {
        block(this)
    }

    var subnetCnt: Int = subnetTypes.size * maxAzs //초기화 이후에 사용

    /** 결과 */
    lateinit var iVpc: IVpc

    /**
     * 필요에따라 오버라이드. ex) VPC 하나에 다수의 프로젝트 설정
     *  -> 여기서 서브넷 추가시,  vpc에서 서브넷을 조회할때 최초 생성된 서브넷만 조회된다. 이거말고도 문제가 많음..
     *  따라서 프로젝트별로 VPC를 만들고 그냥 이들을 연결하자
     *  */
    fun create(stack: Stack, block: VpcProps.Builder.() -> Unit = {}) {
        val vpcProps = VpcProps.builder()
            .ipAddresses(IpAddresses.cidr(vpcCidr))
            .natGateways(natGateways)
            .vpcName(logicalName)
            .maxAzs(maxAzs)
            .subnetConfiguration(subnetTypes.map { subnetConfiguration(project.projectName, it) })
            .apply(block)
            .build()
        iVpc = Vpc(stack, logicalName, vpcProps)
        subnetRename() //간단으로 만든건 이름 이상하게 나옴.
        gatewayVpcEndpoint()
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
            val subnetName = "${projectName}_subnet_$type/${zoneName}_${deploymentType}"
            val subnet = PrivateSubnet.Builder.create(stack, subnetName)
                .vpcId(iVpc.vpcId).availabilityZone(az).cidrBlock("${block}.${subnetCnt++}.0/${cidrMask}").build()
            TagUtil.name(subnet, subnetName)
            subnet
        }

        val naclName = "${projectName}_nacl_${type}_${deploymentType}"
        val nacl = NetworkAcl.Builder.create(stack, naclName).vpc(iVpc).subnetSelection(
            SubnetSelection.builder().subnets(subnets).build()
        ).build()
        entrys.forEach { nacl.addEntry("${naclName}_${it.key}", it.value) }
        TagUtil.name(nacl, naclName)
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
                TagUtil.name(subnet, "${name}_subnet_${subnetSuffix}/${zoneName}_${this.deploymentType}")
            }
        }
    }

    /**
     * name으로는 못가져올 수 있다. (미삭제 등등). 이때는 ID로 가져와야 한다.
     *  */
    fun load(stack: Stack, vpcId: String): CdkVpc {
        iVpc = Vpc.fromLookup(stack, logicalName, VpcLookupOptions.builder().vpcId(vpcId).isDefault(false).build())
        return this
    }

    fun load(stack: Stack): CdkVpc {
        iVpc = Vpc.fromLookup(stack, logicalName, VpcLookupOptions.builder().vpcId(logicalName).isDefault(false).build())
        return this
    }

    /**
     * 공짜 2개 기본 세팅 하기
     *  */
    fun gatewayVpcEndpoint(services: List<GatewayVpcEndpointAwsService> = listOf(GatewayVpcEndpointAwsService.S3, GatewayVpcEndpointAwsService.DYNAMODB)) {
        services.forEach { service ->
            val serviceName = service.name.substringAfterLast(".")
            val endpointName = "${this.project.projectName}_${this.deploymentType}_endpoint_${serviceName}"
            val endpoint = iVpc.addGatewayEndpoint(endpointName, GatewayVpcEndpointOptions.builder().service(service).build())
            endpoint.addToPolicy(
                PolicyStatement.Builder.create()
                    .principals(listOf(AnyPrincipal()))
                    .actions(listOf("*"))
                    .resources(listOf("*"))
                    .build()
            )
            TagUtil.name(endpoint, endpointName) //지금 버그로 태그 입력 안됨 (2022.01)
        }
    }

    /**
     * 해당 서브타입 전체에 적용됨
     * 하위 프로젝트에서 그대로 호출해도 추가된 부분만 잘 적용됨
     *  */
    fun nacl(stack: Stack, subnetType: SubnetType, entrys: Map<String, CommonNetworkAclEntryOptions>) {
        val naclId = "${project.projectName}_nacl_${subnetType.name.lowercase()}_${deploymentType}"
        val nacl = NetworkAcl.Builder.create(stack, naclId).vpc(iVpc).subnetSelection(
            SubnetSelection.builder().subnetType(subnetType).build()
        ).build()
        entrys.forEach { nacl.addEntry("${naclId}_${it.key}", it.value) }
        TagUtil.name(nacl, naclId)
    }


}