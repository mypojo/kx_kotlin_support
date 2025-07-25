package net.kotlinx.xml

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.kotlinx.io.input.InputResource
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

/**
 * stax 사용해서 flow 변환
 * XML 내용을 Map으로 변환하여 반환 -> 1뎁스만 지원함!!
 * 각 XML 요소의 텍스트 내용과 속성을 XmlData 객체로 저장
 * @param separator 추출할 xml 태그명
 */
fun InputResource.toFlowXml(separator: String): Flow<Map<String, XmlData>> = flow {

    val xmlInputFactory = XMLInputFactory.newInstance().apply {
        // 메모리 효율성을 위한 설정
        setProperty(XMLInputFactory.IS_COALESCING, true)
        setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
        setProperty(XMLInputFactory.SUPPORT_DTD, false)
        setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
    }

    val reader = xmlInputFactory.createXMLStreamReader(this@toFlowXml.inputStream)

    try {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    if (reader.localName == separator) {
                        val xmlContent = parseXmlContent(reader, separator)
                        emit(xmlContent)
                    }
                }
            }
        }
    } finally {
        reader.close()
    }
}

/**
 * XML 엘리먼트를 파싱하여 Map으로 변환
 * 모든 태그의 텍스트 내용과 속성을 XmlData 객체로 저장
 */
private fun parseXmlContent(reader: XMLStreamReader, separator: String): Map<String, XmlData> {
    val contentMap = mutableMapOf<String, XmlData>()

    while (reader.hasNext()) {
        when (reader.next()) {
            XMLStreamConstants.START_ELEMENT -> {
                val tagName = reader.localName
                // 중첩된 태그가 아닌 경우에만 처리
                if (tagName != separator) {
                    // 속성 수집
                    val attributes = mutableMapOf<String, String>()
                    for (i in 0 until reader.attributeCount) {
                        val attrName = reader.getAttributeLocalName(i)
                        val attrValue = reader.getAttributeValue(i)
                        attributes[attrName] = attrValue
                    }

                    val tagValue = reader.elementText ?: ""
                    contentMap[tagName] = XmlData(text = tagValue, attributes = attributes)
                }
            }

            XMLStreamConstants.END_ELEMENT -> {
                if (reader.localName == separator) {
                    return contentMap
                }
            }
        }
    }

    throw IllegalStateException("Unexpected end of XML while parsing content")
}