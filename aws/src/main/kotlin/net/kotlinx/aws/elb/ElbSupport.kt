package net.kotlinx.aws.elb

import aws.sdk.kotlin.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client
import aws.sdk.kotlin.services.elasticloadbalancingv2.deregisterTargets
import aws.sdk.kotlin.services.elasticloadbalancingv2.model.LoadBalancer
import aws.sdk.kotlin.services.elasticloadbalancingv2.model.TargetDescription
import aws.sdk.kotlin.services.elasticloadbalancingv2.registerTargets


//==================================================== 지원 메소드 ======================================================
/**
 * ELB에 인스턴스를 등록해준다. EC2 콘솔(https://ap-northeast-2.console.aws.amazon.com/ec2/v2/home)에 있는 기능과 동일함
 * ex) WAS 기동 후 모든 리소스(인메모리 로드/외부연결 등) 체크가 완료 후 정상 작동이 확인되면 ELB와 연결
 * @param targetGroupArn ex) arn:aws:elasticloadbalancing:us-west-2:123456789012:targetgroup/my-targets/73e2d6bc24d8a067
 *
 */
suspend fun ElasticLoadBalancingV2Client.registerTargets(targetGroupArn: String, vararg instanceIds: String) {
    this.registerTargets {
        this.targetGroupArn = targetGroupArn
        this.targets = instanceIds.map { TargetDescription { this.id = it } }
    }
}

suspend fun ElasticLoadBalancingV2Client.deregisterTargets(targetGroupArn: String, vararg instanceIds: String) {
    this.deregisterTargets {
        this.targetGroupArn = targetGroupArn
        this.targets = instanceIds.map { TargetDescription { this.id = it } }
    }
}

/** 뭐 ELB가 많아야 얼마나 많겠다. 다 가져온다.  */
suspend fun ElasticLoadBalancingV2Client.describeLoadBalancers(): List<LoadBalancer> = this.describeLoadBalancers().loadBalancers!!
