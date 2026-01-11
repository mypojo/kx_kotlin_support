package net.kotlinx.spring.security

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class CognitoJwtComponentTest : BeSpecLight() {

    init {

        initTest(KotestUtil.IGNORE)

        Given("CognitoJwtComponent") {
            val jwtComponent = CognitoJwtComponent("ap-northeast-2_example")

            val claims = mapOf(
                "sub" to "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                "cognito:username" to "test-user",
                "email" to "test@example.com",
                "amr" to listOf("pwd", "mfa"),
                "iss" to "https://cognito-idp.ap-northeast-2.amazonaws.com/ap-northeast-2_example",
                "aud" to "client-id-123",
                "exp" to 1700000000,
                "iat" to 1600000000,
                "jti" to "jwt-id-000",
                "identities" to listOf(
                    mapOf(
                        "userId" to "12345678901234567890",
                        "providerName" to "SignInWithApple",
                        "providerType" to "Apple",
                        "issuer" to null,
                        "primary" to "true",
                        "dateCreated" to "1700000000000"
                    )
                )
            )

            Then("convertToIdTokenInfo 호출시 identities 가 정상적으로 파싱되어야 한다") {
                val info = jwtComponent.convertToIdTokenInfo(claims)
                info.sub shouldBe "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
                info.username shouldBe "test-user"
                info.identities.size shouldBe 1
                info.identities[0].userId shouldBe "12345678901234567890"
                info.identities[0].providerName shouldBe "SignInWithApple"
                info.identities[0].providerType shouldBe "Apple"
                info.identities[0].primary shouldBe true
                info.identities[0].dateCreated shouldBe "1700000000000"

                info.amr shouldBe listOf("pwd", "mfa")
                info.iss shouldBe "https://cognito-idp.ap-northeast-2.amazonaws.com/ap-northeast-2_example"
                info.aud shouldBe listOf("client-id-123")
                info.exp shouldBe 1700000000L
                info.iat shouldBe 1600000000L
                info.jti shouldBe "jwt-id-000"
            }
        }
    }
}
