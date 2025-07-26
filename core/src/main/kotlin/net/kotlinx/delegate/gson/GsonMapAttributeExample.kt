package net.kotlinx.delegate.gson

import com.google.gson.JsonElement
import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet

/**
 * GsonMapAttribute와 GsonMapAttributeDelegate 사용 예시
 * JSON 형식의 속성을 가진 메뉴 클래스
 */
class GsonMenu : GsonAttribute {
    // GsonData 형태의 속성 맵 초기화
    override var attributes: GsonData = GsonData.obj()
    
    // 위임 속성 정의
    var name: String by GsonAttributeDelegate()
    var price: Int by GsonAttributeDelegate()
    var available: Boolean by GsonAttributeDelegate()
    var tags: JsonElement by GsonAttributeDelegate()
    
    /**
     * JSON 문자열로 직렬화
     */
    fun toJson(): String {
        return GsonSet.GSON.toJson(attributes.delegate)
    }
    
    /**
     * 이쁘게 포맷된 JSON 문자열로 직렬화
     */
    fun toPrettyJson(): String {
        return attributes.toPreety()
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
        
        /**
         * JSON 문자열에서 GsonMenu 객체로 역직렬화
         */
        fun fromJson(json: String): GsonMenu {
            val menu = GsonMenu()
            menu.attributes = GsonData.parse(json)
            return menu
        }
    }
}

/**
 * 사용 예시
 */
fun gsonMapAttributeExample() {
    // 로거 생성
    val log = KotlinLogging.logger {}
    
    // 새 메뉴 객체 생성 및 속성 설정
    val menu = GsonMenu()
    menu.name = "아메리카노"
    menu.price = 4500
    menu.available = true
    
    // 배열 속성 추가
    val tagsArray = GsonData.array()
    tagsArray.add("커피")
    tagsArray.add("음료")
    tagsArray.add("핫")
    menu.tags = tagsArray.delegate
    
    // JSON으로 직렬화
    val json = menu.toJson()
    log.info { "직렬화된 JSON: $json" }
    
    // 이쁘게 포맷된 JSON으로 출력
    val prettyJson = menu.toPrettyJson()
    log.info { "이쁘게 포맷된 JSON:\n$prettyJson" }
    
    // JSON에서 역직렬화
    val deserializedMenu = GsonMenu.fromJson(json)
    log.info { "역직렬화된 이름: ${deserializedMenu.name}" }
    log.info { "역직렬화된 가격: ${deserializedMenu.price}" }
    log.info { "역직렬화된 가용성: ${deserializedMenu.available}" }
    log.info { "역직렬화된 태그: ${deserializedMenu.tags}" }
}