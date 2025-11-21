package net.kotlinx.aws.waf

import aws.sdk.kotlin.services.wafv2.Wafv2Client
import aws.sdk.kotlin.services.wafv2.getIpSet
import aws.sdk.kotlin.services.wafv2.model.Scope
import aws.sdk.kotlin.services.wafv2.updateIpSet
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

val AwsClient.waf: Wafv2Client
    get() = getOrCreateClient { Wafv2Client { awsConfig.build(this) }.regist(awsConfig) }

/**
 * IP set 을 desired 주소 집합과 동일하도록 동기화
 * @param id arn 이름 오른쪽에 붙은 코드
 * @param ips 49.xx.yy.zz/32  이런식으로 넣어야함
 * 약 5초 정도 지나면 반영되는듯
 *
 * 클랴우드 프론트(Scope.Cloudfront)용이면 반드시 리전 설정을 북미로 해야함!!
 */
suspend fun Wafv2Client.updateIpSet(name: String, id: String, ips: List<String>, scope: Scope = Scope.Cloudfront) {
    //동시성 제어(lockToken)가 필수 옵션이라서 토큰을 얻어옴
    val resp = getIpSet {
        this.id = id
        this.name = name
        this.scope = scope
    }
    updateIpSet {
        this.id = id
        this.name = name
        this.scope = scope
        this.lockToken = resp.lockToken
        this.addresses = ips
    }
}

