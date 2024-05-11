package net.kotlinx.collection


/**
 * DSL용 가변 리스트
 * DSL 내에서 + 로 List를 추가하고싶을때 사용
 * ex) fun descs(block: MutableListString.() -> Unit) { descs = MutableListString().apply(block).toList() }
 * 참고로 문자가 아닌 애들은 그냥 쓰면됨
 *  */
class MutableListString : ArrayList<String>() {

    /**
     * ex) +"설명문구"
     *  */
    operator fun String.unaryPlus() {
        add(this)
    }
}
