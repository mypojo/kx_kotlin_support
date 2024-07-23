package net.kotlinx.collection

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isEndOfWord = false
}

/**
 * AI가 주워옴
 * 접두어 검색기
 * ex) 메뉴 매핑 조회
 */
class Trie(words: Collection<String>) {

    private val root = TrieNode()

    init {
        words.forEach { insert(it) }
    }

    private fun insert(word: String) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfWord = true
    }

    /**
     * 부분 문자를 입력해서 여기에 prefix로 매칭되는 완성된 결과를 가져오기
     * ex) 청바지 -> 청바지반바지, 청바지싼곳
     * */
    fun findPrefixeMatchs(prefix: String): List<String> {
        val result = mutableListOf<String>()
        var current = root
        for (char in prefix) {
            if (char !in current.children) return result
            current = current.children[char]!!
        }
        findAllWordsFromNode(current, prefix, result)
        return result
    }

    private fun findAllWordsFromNode(node: TrieNode, prefix: String, result: MutableList<String>) {
        if (node.isEndOfWord) {
            result.add(prefix)
        }
        for ((char, childNode) in node.children) {
            findAllWordsFromNode(childNode, prefix + char, result)
        }
    }

    /**
     * 완성된 문자를 입력해서 prefix 일치 가능한 모든 부분들을 찾기
     * ex) 풀 url을 입력해서 , 접두어가 일치하는 메뉴 찾기
     * */
    fun findPrefixes(word: String): List<String> {
        val result = mutableListOf<String>()
        var current = root
        val sb = StringBuilder()

        for (char in word) {
            if (char !in current.children) break
            current = current.children[char]!!
            sb.append(char)
            if (current.isEndOfWord) {
                result.add(sb.toString())
            }
        }

        return result
    }

}


