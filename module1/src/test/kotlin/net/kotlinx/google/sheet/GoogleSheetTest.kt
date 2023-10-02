package net.kotlinx.google.sheet

import net.kotlinx.google.GoogleSecret
import org.junit.jupiter.api.Test
import java.io.File

internal class GoogleSheetTest {

    @Test
    fun `구글시트 읽기`() {
        val secret = GoogleSecret {
            secretDir = File("C:\\Users\\mypoj\\.google/")
        }
        val sheet = GoogleSheet(secret)
        val orgValues = sheet.load("18KFAeXWWnJYG6Si_vNo3CyPgC4QK1XIoSK33gC3dkGg", "운동정보")
        println(orgValues)
    }

}