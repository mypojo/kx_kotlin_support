package net.kotlinx.file

import net.kotlinx.regex.RegexSet


object FileNameUtil {

    /** 파일이름으로 사용 가능하게 변경 */
    fun toFileName(name: String, replace: String = ""): String {
        return name.replace(RegexSet.FILE_NAME, replace) // 금지된 문자 제거
            .replace(Regex("\\s+"), "_")  // 공백을 언더스코어로 대체
            .trim() // 앞뒤 공백 제거
            .take(255) // 최대 길이 제한 (일반적으로 255자)
    }

}

