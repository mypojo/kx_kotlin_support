package net.kotlinx.string


object StringJsonUtil {

    /**
     * 일부 AI 모델에서 json 결과를 아래처럼 인코딩된 백틱으로 전달해준다.
     * 이를 정상으로 파싱해줌
     */
    fun cleanJsonText(rawJsonText: String): String {
        return rawJsonText
            // 1. 앞뒤 백틱 제거 (```json, ``` 등)
            .replace(Regex("^```(?:json)?\\s*"), "")
            .replace(Regex("\\s*```$"), "")
            // 2. 앞뒤 공백 및 개행 제거
            .trim()
    }

}