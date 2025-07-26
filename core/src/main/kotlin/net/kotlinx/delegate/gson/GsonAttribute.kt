package net.kotlinx.delegate.gson

import net.kotlinx.json.gson.GsonData

/** 
 * Gson 기반의 속성 맵을 위한 인터페이스
 * JSON 형식의 속성을 저장하고 관리하는데 사용됨
 */
interface GsonAttribute {
    
    /**
     * GsonData 형태의 속성 맵
     * Gson 라이브러리의 JsonElement를 사용하여 JSON 데이터를 직접 처리
     */
    var attributes: GsonData
}