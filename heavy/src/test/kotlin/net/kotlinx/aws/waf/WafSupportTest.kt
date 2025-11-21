package net.kotlinx.aws.waf

import aws.sdk.kotlin.services.wafv2.model.Scope
import net.kotlinx.aws.AwsConfig
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

/**
 * Ec2PrefixListSupportTest 스타일을 참고한 WAF 템플릿 테스트
 */
class WafSupportTest : BeSpecHeavy() {

    private val aws by lazy {
        //koin<AwsClient>(findProfile49)
        AwsConfig(region = AwsConfig.REGION_US, profileName = findProfile49).client
    }

    init {
        // 실제 실행을 권장하지 않음. 템플릿 확인용으로만 사용
        initTest(KotestUtil.IGNORE)

        Given("WAF IP Set 업데이트 템플릿") {

            // 예시 값: 실제 값으로 교체해서 사용하세요
            val ipSetName = "waxxv4"
            val ipSetId = "33xxx5b42"

            Then("IP Set 주소 동기화 (템플릿)") {
                val desiredIps = listOf(
                    "49.2xx/32",
                    "49.2trr/32"
                )
                // 기본 Scope 는 CloudFront. 배포 대상에 맞게 Application/Regional 로 교체 가능
                // 실제 실행 예시 (주석 해제 시 주의):
                aws.waf.updateIpSet(ipSetName, ipSetId, desiredIps, Scope.Cloudfront)
            }

            Then("Scope 지정 예시 (템플릿)") {
                val desiredIps = listOf("49.254.179.205/32")
                // CloudFront 가 아닌 경우 예시:
                // aws.waf.updateIpSet(ipSetId, ipSetName, desiredIps, Scope.Regional)
            }

        }
    }
}
