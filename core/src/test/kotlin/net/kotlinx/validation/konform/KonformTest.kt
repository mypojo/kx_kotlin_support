//package net.kotlinx.validation.konform
//
//import com.lectra.koson.obj
//import io.konform.validation.Validation
//import io.konform.validation.jsonschema.maxItems
//import io.konform.validation.jsonschema.maxLength
//import io.konform.validation.jsonschema.maximum
//import io.konform.validation.jsonschema.minimum
//import io.konform.validation.required
//import io.kotest.matchers.shouldBe
//import net.kotlinx.json.koson.toGsonData
//import net.kotlinx.kotest.KotestUtil
//import net.kotlinx.kotest.initTest
//import net.kotlinx.kotest.modules.BeSpecHeavy
//
///**
// * 사용해보니 프로젝트와 잘 맞지 않아서 포기
// * */
//class KonformTest : BeSpecHeavy() {
//
//    init {
//        initTest(KotestUtil.FAST)
//
//        /** kotest 플러그인이 있긴한데 굳이 쓰지는 않는다. */
//        Given("https://github.com/konform-kt/konform") {
//
//            data class UserProfile(
//                val fullName: String,
//                val age: Int?,
//                val type: String,
//                val attendees: List<String>,
//                val hint: String? = null,
//            )
//
//            val defaultValidate = Validation {
//                UserProfile::fullName required {
//                    minLengthKr("풀네임", 2)
//                    maxLength(100)
//                }
//            }
//
//            val validateUser = Validation {
//                run(defaultValidate)
//
//                UserProfile::age ifPresent {
//                    minimum(0)
//                    maximum(150)
//                }
//
//                UserProfile::attendees required {
//                    maxItems(2) hint "참여자 수는 2명 이내여야 합니다."
//                }
//
//                UserProfile::type {
//                    maxLength(4)
//                }
//
//                UserProfile::hint {
//                    required {
//                        maxLength(100)
//                    }
//                }
//            }
//
//            Then("벨리데이션 체크") {
//                val user = UserProfile("A", -1, "type", listOf("a", "b", "c"))
//                val results = validateUser(user)
//                results.printSimple()
//                results.errors.size shouldBe 4
//            }
//
//            Then("http 요청 -> gson 변환 ->  벨리데이션 체크") {
//
//                val json = obj {
//                    "fullName" to null
//                    "type" to "safe"
//                }.toGsonData()
//
//                val user = json.fromJson<UserProfile>()
//                val results = validateUser(user)
//                results.printSimple()
//                results.errors.size shouldBe 3
//            }
//
//        }
//
//    }
//
//
//}