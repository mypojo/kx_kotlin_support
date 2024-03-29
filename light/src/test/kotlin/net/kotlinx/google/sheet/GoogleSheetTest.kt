package net.kotlinx.google.sheet

import net.kotlinx.google.GoogleSecret
import org.junit.jupiter.api.Test
import java.io.File

internal class GoogleSheetTest {

    val secret = GoogleSecret {
        secretDir = File("C:\\Users\\mypoj\\.google/")
    }
    val sheet = GoogleSheet(secret.createService(), "13U-VKClgbbwhic6Jb6nsf9ITeESn7nZiEXNN6M5fsNY", "테스트용")

    @Test
    fun `구글시트 읽기`() {
        sheet.readAll().forEach {
            println(it)
        }
    }

    @Test
    fun `구글시트 쓰기`() {
        val datas = listOf(
            listOf("멍멍", "야옹"),
            listOf("으르렁", "뽀작"),
        )
        sheet.write(datas, 2 to 4)
    }

}