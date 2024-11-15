package net.kotlinx.reflect

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import net.kotlinx.csv.readCsvLines
import net.kotlinx.csv.readCsvLinesCnt
import net.kotlinx.csv.writeCsvLines
import net.kotlinx.file.FileGzipUtil
import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.string.print
import net.kotlinx.system.OsType
import net.kotlinx.system.ResourceHolder

class BeanTest : BeSpecLog() {

    data class Poo1(
        val name: String,
    ) {

        var age: Int? = null
        var group: String? = null
    }

    class PooDto1 {
        var name: String? = null
        var age: Int? = null
        var tag: String? = null
    }

    class PooDto2(
        var name: String,
        var age: Int?,
    ) {
        var tag: String? = null
    }

    data class PooDto3(
        var name: String? = null,
        var age: Int? = null,
        var tag: String? = null,
    )

    data class Common01(
        val name: String,
        val age: Int,
        val osType: OsType,
    )

    class Common02 {
        var tag: String? = null
    }

    init {
        initTest(KotestUtil.FAST)

        Given("Bean") {
            Then("간단 사용 테스트") {
                val p1 = Poo1("홍길동").apply {
                    age = 15
                    group = "테스트"
                }

                Bean(p1).also {
                    it["name"] shouldBe "홍길동"
                    it["age"] shouldBe 15

                    it.put("age", 878)
                    it["age"] shouldBe 878
                }

                Bean(p1).convert(PooDto1::class).also {
                    Bean(it).toTextGrid().print()
                    check(it.name == p1.name)
                }
                Bean(p1).convert(PooDto2::class).also {
                    Bean(it).toTextGrid().print()
                    check(it.name == p1.name)
                }

                val fromLine = Bean.fromLine(PooDto3::class, listOf("김철수", "26", "myTag"))
                Bean(fromLine).toTextGrid().print()
            }
        }

        Given("CSV 읽고 쓰기 처리") {

            val file = ResourceHolder.WORKSPACE.slash("csvReadWriteTest").slash("demo.csv")

            val datas = listOf(
                Common01("댕댕이", 2, OsType.MAC),
                Common01("고양이", 3, OsType.LINUX),
                Common01("영감님", 67, OsType.WINDOWS),
            )

            Then("객체 -> csv") {
                file.writeCsvLines(datas.map { Bean(it).toList() })
                file.length() shouldBeGreaterThan 10
            }

            Then("csv -> 객체") {
                file.readCsvLines().forEach {
                    log.info { " -> 라인 : $it" }
                }
                val lines = file.readCsvLines().fromLines<Common01>()
                lines.print()
                println(file.readCsvLinesCnt())
            }

            xThen("테스트 데이터 읽기") {
                val file = ResourceHolder.WORKSPACE.slash("f3d3d604-353f-4eb4-9fc1-4db8d9e4a514.csv")
                val renamed = ResourceHolder.WORKSPACE.slash("f3d3d604-353f-4eb4-9fc1-4db8d9e4a514.csv.gz")
                file.renameTo(renamed)
                FileGzipUtil.unGzip(renamed)
                println(file.readCsvLinesCnt())
            }
        }

        Given("임시테스트") {
            Then("csv -> 객체") {
                println(Common01::class.constructors.maxBy { it.parameters.size })
                println(Common02::class.constructors.maxBy { it.parameters.size })
            }
        }
    }

}