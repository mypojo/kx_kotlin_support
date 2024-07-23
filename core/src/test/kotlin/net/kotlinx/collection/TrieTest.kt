package net.kotlinx.collection

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class TrieTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("부분 문자를 입력해서 여기에 prefix로 매칭되는 완성된 결과를 가져오기") {

            val trie = Trie(
                listOf(
                    "aa/bb", "aa/bb/c", "aa/bb/cc", "aa/bb/dd", "aa/cc", "bb/cc"
                )
            )

            Then("접두어 매핑1") {
                // 접두어로 검색
                val prefix = "aa/bb"
                val results = trie.findPrefixeMatchs(prefix)
                log.info { "Matching words for prefix '$prefix' -> $results" }
                results.size shouldBe 4
            }

            Then("접두어 매핑2") {
                // 접두어로 검색
                val prefix = "aa/bb/c"
                val results = trie.findPrefixeMatchs(prefix)
                log.info { "Matching words for prefix '$prefix' -> $results" }
                results.size shouldBe 2
            }
        }

        Given("완성된 문자를 입력해서 prefix 일치 가능한 모든 부분들을 찾기 ex) 메뉴 매핑") {

            val words = listOf("aa/bb", "aa/bb/c", "aa/bb/cc", "aa/bb/dd", "aa/cc", "bb/cc")
            val trie = Trie(words)

            Then("접두어 매핑1") {
                // 접두어로 검색
                //val prefix = "aa/bb/"
                val prefix = "aa/bb/cc/dd"
                val results = trie.findPrefixes(prefix)
                log.info { "Matching words for prefix '$prefix' -> $results" }
                //results.size shouldBe 4
            }

        }

    }

}
