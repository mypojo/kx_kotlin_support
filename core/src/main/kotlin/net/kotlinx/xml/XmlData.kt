package net.kotlinx.xml

/**
 * XML 데이터를 저장하는 클래스
 * @property text 태그의 텍스트 내용
 * @property attributes 태그의 속성 맵
 */
data class XmlData(
    val text: String,
    val attributes: Map<String, String> = emptyMap()
)