package net.kotlinx.aws_cdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.route53.*


object Route53Util {

    /**
     * private 존 1개 & 레코드 1개 매칭하는 경우에 사용
     * 전체 도메인중 부분만 적용하고 싶다면 개별 호스팅 존과 레코드를 1:1로 만들어야 한다.
     * (호스팅 존 전체가 매핑되어서 누락된건은 실제 DNS로 안감)
     * ex) CdkRoute53Util.privateHostToIp(this, vpc, "aa.bb.kr", '1.22.333.13');
     *  */
    fun privateHostToIp(stack: Stack, vpc: IVpc, host: String, dest: String) {
        val zone = PrivateHostedZone(
            stack, "${vpc.vpcId}-host_${host}", PrivateHostedZoneProps.builder()
                .vpc(vpc)
                .zoneName(host)
                .build()
        )
        ARecord(
            stack, "${vpc.vpcId}-record_${host}", ARecordProps.builder()
                .zone(zone)
                .recordName(host)
                .target(RecordTarget.fromIpAddresses(dest))
                .build()
        )
    }

    /**
     * A레코드 달아주기
     * @param target ex) RecordTarget.fromAlias(CloudFrontTarget(distribution))
     * */
    fun arecord(stack: Stack, iZone: IHostedZone, domain: String, target: RecordTarget) {
        ARecord(
            stack, "${domain}-arecord",
            ARecordProps.builder()
                .zone(iZone)
                .recordName(domain)
                .target(target)
                .build(),
        )
    }

}