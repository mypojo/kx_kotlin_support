package net.kotlinx.awscdk.ec2

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.iam.IRole

/**
 * 비스천 호스트 설정.
 * 2022.12 기준 아직 DB 포워딩을 다이렉트로 해주지 않는다
 * */
class CdkBastion : CdkInterface {

    @Kdsl
    constructor(block: CdkBastion.() -> Unit = {}) {
        apply(block)
    }

    /** VPC 이름 */
    override val logicalName: String
        get() = "${project.profileName}-ec2_${name}-${deploymentType.name.lowercase()}"

    /** 이름 */
    var name: String = "bastion"

    /**
     * SSM 역할
     *  */
    lateinit var role: IRole

    /** VPC */
    lateinit var vpc: IVpc

    /** 배스천 호스트용 SG */
    lateinit var sg: ISecurityGroup

    /**
     * 최초 실행시킬 커맨드
     *  */
    var commands: List<String> = emptyList()

    /** 결과 */
    lateinit var instance: Instance

    fun create(stack: Stack, block: InstanceProps.Builder.() -> Unit = {}): CdkBastion {
        instance = Instance(
            stack, logicalName, InstanceProps.builder()
                .instanceName(logicalName)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_WITH_EGRESS).build())
                .allowAllOutbound(true) //다 열어준다. SG에서 막을것
                .role(role)
                .securityGroup(sg)
                .instanceType(InstanceType.of(InstanceClass.T2, InstanceSize.NANO))  //가장 작은 T 인스턴스 사용하면됨. MICRO -> NANO
                .machineImage(
                    /** 아마존 제공 제품을 써야지, 기본 에이전트들이 깔려있음. 대충 지원하는 최신 버전 */
                    AmazonLinuxImage.Builder.create()
                        .generation(AmazonLinuxGeneration.AMAZON_LINUX_2023) //AMAZON_LINUX_2
                        .kernel(AmazonLinuxKernel.KERNEL6_1)  //KERNEL5_X
                        .build()
                )
                .apply {
                    //커맨드 있으면 실행
                    if (commands.isNotEmpty()) {
                        //userData(UserData.custom(commands.joinToString("\n")))
                        userData(
                            UserData.forLinux().apply {
                                addCommands(*commands.toTypedArray())
                            }
                        )
                    }
                }
                .apply(block).build()
        )
        TagUtil.tag(instance, deploymentType)
        return this
    }

    companion object {

        /**
         * 명령어를 만들어준다.
         * @param fromPort  들어오는 포트 ex) 33060
         * @param toUrl 연결해줄 주소o ex)  xx.cluster-cbcphsmx83n9.ap-northeast-2.rds.amazonaws.com:3306
         * */
        fun cmdConnect(fromPort: Int, toUrl: String): List<String> {
            return listOf(
                "sudo yum install socat -y",
                "sudo socat -d -d TCP4-LISTEN:${fromPort},fork TCP4:${toUrl} &",
            )

        }
    }

}