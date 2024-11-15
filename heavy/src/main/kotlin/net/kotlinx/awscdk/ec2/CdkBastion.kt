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
        get() = "${projectName}-ec2_${name}-${suff}"

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

    /**
     * 제일 싼거 쓰면됨
     * 가장 작은 T 인스턴스 사용하면됨. MICRO -> NANO
     * */
    var instanceType = InstanceType.of(InstanceClass.T2, InstanceSize.NANO)

    /** 최신 이미지 사용하면됨 */
    var image = AmazonLinuxImage.Builder.create()
        /** 아마존 제공 제품을 써야지, 기본 에이전트들이 깔려있음. 대충 지원하는 최신 버전 */
        .generation(AmazonLinuxGeneration.AMAZON_LINUX_2023)
        /** 최신 버전 사용 */
        .kernel(AmazonLinuxKernel.KERNEL6_1)
        .build()!!

    /**
     * 최소 볼륨 지정
     * 아쉽게도 네이밍 태그가 안됨. 개별로 만들어서 붙여야 할듯
     * 대신 인스턴스 삭제시 볼륨도 같이 삭제됨
     * */
    var volume = BlockDeviceVolume.ebs(
        8, // 볼륨 크기 (GB) 제일싼거
        EbsDeviceProps.builder()
            .volumeType(EbsDeviceVolumeType.GP3)  // 볼륨 타입
            .build()
    )

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
                .instanceType(instanceType)
                .machineImage(image)
                .blockDevices(
                    listOf(
                        BlockDevice.builder()
                            .deviceName("/dev/xvda")  // 루트 볼륨의 기본 디바이스 이름
                            .volume(volume)
                            .build()!!
                    )
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
         * @param fromPort  포워딩으로 설정된, 들어오는 포트 ex) 33060
         * @param toUrl 연결해줄 주소 ex)  xx.cluster-xx.ap-northeast-2.rds.amazonaws.com
         *
         * host 이름을 시크릿 매니저에서 가져올 수 있지만 이렇게 하면 너무~~~~ 느려진다. 왜인지 모름.
         * 그래서 그냥 하드코딩함 or 파라메터 스토어에 입력
         * */
        fun cmdConnect(fromPort: Int, toUrl: String, dbPort: Int = 3306): List<String> {
            return listOf(
                "sudo yum install socat -y",
                "sudo socat -d -d TCP4-LISTEN:${fromPort},fork TCP4:${toUrl}:${dbPort} &",
            )
        }
    }

}