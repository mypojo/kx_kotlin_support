package net.kotlinx.aws.cognito

import aws.sdk.kotlin.services.cognitoidentityprovider.model.ListUsersResponse
import aws.sdk.kotlin.services.cognitoidentityprovider.model.UserType
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import net.kotlinx.kotest.modules.BeSpecLight

/**
 * CognitoSupport.kt 에 대한 간단 코테스트
 * - 다른 테스트들과 동일하게 BeSpecLight 상속 사용
 * - 실제 AWS 호출을 하지 않고, 페이지 응답을 직접 만들어 평탄화 로직을 검증
 */
class CognitoSupportKtTest : BeSpecLight() {

    init {
        Given("listAllUsers 페이지 평탄화") {
            When("두 페이지에 사용자들이 나뉘어 있을 때") {
                Then("모든 사용자들이 순서대로 하나의 플로우로 리턴된다") {
                    //given
                    val u1 = UserType { username = "u1" }
                    val u2 = UserType { username = "u2" }
                    val u3 = UserType { username = "u3" }

                    val page1 = ListUsersResponse { users = listOf(u1, u2) }
                    val page2 = ListUsersResponse { users = listOf(u3) }

                    val pages = flowOf(page1, page2)

                    //when
                    val result = collectUsersFromPages(pages).toList()

                    //then
                    result.shouldContainExactly(listOf(u1, u2, u3))
                }
            }

            When("페이지의 users 항목이 null 인 경우") {
                Then("빈 결과가 리턴된다") {
                    //given
                    val pageWithNull = ListUsersResponse { users = null }
                    val pages = flowOf(pageWithNull)

                    //when
                    val result = collectUsersFromPages(pages).toList()

                    //then
                    result.shouldContainExactly(emptyList())
                }
            }
        }
    }
}
