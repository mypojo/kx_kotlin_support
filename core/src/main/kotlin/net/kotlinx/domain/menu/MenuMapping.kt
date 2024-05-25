package net.kotlinx.domain.menu

/**
 * API 와 1:1로 매핑되는 매핑
 * 실제 http 요청당 1개 만들어짐
 * */
data class MenuMapping(
    /** 컨트롤러에 매핑된 URL  */
    val url: String,
    /** 컨트롤러 클래스명*/
    val className: String,
    /** 컨트롤러 메소드명 */
    val methodName: String,
    /** 메뉴 리턴 타입 */
    val type: String,
    /** 메뉴 설명 */
    val description: String,
    /** 메뉴 */
    val menu: Menu,
)