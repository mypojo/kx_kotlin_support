package net.kotlinx.aws.cognito

import io.kotest.matchers.string.shouldContain
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class CognitoLoginUrlBuilderTest : BeSpecHeavy() {

    init {
        Given("CognitoLoginUrlBuilder") {
            val builder = CognitoLoginUrlBuilder(
                authDomain = "auth.example.com",
                clientId = "test-client-id",
                redirectUri = "https://localhost/callback"
            )

            When("buildGoogleLoginUrl 호출 시") {
                val url = builder.buildLoginUrl(state = "teststate", providerName = "Google")

                Then("정상적인 Google 로그인 URL이 생성되어야 함") {
                    url shouldContain "https://auth.example.com/oauth2/authorize"
                    url shouldContain "identity_provider=Google"
                    url shouldContain "response_type=code"
                    url shouldContain "client_id=test-client-id"
                    url shouldContain "redirect_uri=https%3A%2F%2Flocalhost%2Fcallback"
                    url shouldContain "state=teststate"
                }
            }

            When("codeChallenge 포함하여 buildGoogleLoginUrl 호출 시") {
                val url = builder.buildLoginUrl(state = "teststate", codeChallenge = "challenge123", providerName = "Google")

                Then("PKCE 관련 파라미터가 포함되어야 함") {
                    url shouldContain "code_challenge=challenge123"
                    url shouldContain "code_challenge_method=S256"
                }
            }
        }
    }
}
