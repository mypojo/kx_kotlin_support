package net.kotlinx.core1.string



/** 다국어(한글등)이면 true를 리턴한다.  */
inline fun Char.isOther(): Boolean  = Character.getType(this) == Character.OTHER_LETTER.toInt()


/**
 * 싱글이면 1칸(영문,숫자,특문), 아니면 2칸(한글) -> 텍스트 그리드를 만들때 사용한다.
 * 한글 초성만 있을 경우 1바이트로 인식하지만 2칸을 차지하기 때문에 바이트로 하면 안된다.
 */
inline fun Char.space(): Int  = if(this.isOther()) 2 else 1