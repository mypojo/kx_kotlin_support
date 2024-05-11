package net.kotlinx.collection

/** ex) 파일 확장자 체크 */
fun Set<String>.endsWithAny(text: String): Boolean = this.any { text.endsWith(it, true) }
