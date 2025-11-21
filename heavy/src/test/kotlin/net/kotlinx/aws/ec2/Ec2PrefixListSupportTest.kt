package net.kotlinx.aws.ec2

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

/**
 * Ec2SupportKtTest 스타일을 참고한 템플릿 테스트
 */
class Ec2PrefixListSupportTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile49) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("Managed Prefix List CIDR 추가/삭제 템플릿") {

            val prefixListId = "pl-xxx"
            val cidr = "1.2.3.4/32"

            Then("CIDR 리스팅") {
                val list = aws.ec2.getPrefixListCidrs(prefixListId)
                list.print()
            }

            Then("CIDR 존재 여부 확인 (템플릿)") {
                //49.254.179.205/32
                aws.ec2.addIpToPrefixList(
                    prefixListId, listOf(
                        "49.yyy5/32" to "sin"
                    )
                )
                //aws.ec2.updateIpToPrefixList(prefixListId,)


            }

            Then("CIDR 삭제 (템플릿)") {
                // 예시: aws.ec2.removeIpFromPrefixList(prefixListId, cidr)
            }

        }
    }
}
