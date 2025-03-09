package net.kotlinx.collection

/**
 * ex) 파일 확장자 체크
 * 좋은방법인거 같지는 않다.. 그냥 in 쓸것!
 *  */
fun Set<String>.endsWithAny(text: String): Boolean = this.any { text.endsWith(it, true) }
