package net.kotlinx.file

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class FileNameUtilTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("FileNameUtil") {
            Then("사용가능한 파일명 리턴") {
                FileNameUtil.toFileName("파일 이름으로 40%_* a&b 3/5 입니다") shouldBe "파일_이름으로_40%__a&b_35_입니다"
            }
        }
    }

}
