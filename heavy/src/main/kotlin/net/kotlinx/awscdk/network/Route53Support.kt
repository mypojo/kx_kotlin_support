package net.kotlinx.awscdk.network

import software.amazon.awscdk.services.cloudfront.IDistribution
import software.amazon.awscdk.services.elasticloadbalancingv2.ILoadBalancerV2
import software.amazon.awscdk.services.route53.RecordTarget
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
import software.amazon.awscdk.services.route53.targets.LoadBalancerTarget

/** 도메인 연결용 레코드 정보 리턴 */
fun IDistribution.toRecordTarget(): RecordTarget = RecordTarget.fromAlias(CloudFrontTarget(this))

/** 도메인 연결용 레코드 정보 리턴 */
fun ILoadBalancerV2.toRecordTarget(): RecordTarget = RecordTarget.fromAlias(LoadBalancerTarget(this))