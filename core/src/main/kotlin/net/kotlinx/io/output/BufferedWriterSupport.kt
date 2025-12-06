package net.kotlinx.io.output

import net.kotlinx.json.gson.GsonData
import java.io.BufferedWriter

/**
 * BufferedWriter 관련 확장 함수 모음
 * - 스프링 StreamingResponseBody 등에서 JSON line 쓰기용
 */
fun BufferedWriter.writeLine(gson: GsonData) {
    this.write(gson.toString())
    this.write("\n")
    this.flush()
}
