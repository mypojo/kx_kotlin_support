package net.kotlinx.aws.schedule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.DayOfWeek

class CronExpressionTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("크론 표현식 생성") {

            Then("매일 특정시간1") {
                val express = CronExpression {
                    configHours = listOf(8, 15)
                }
                println(express)
            }

            Then("매일 특정시간2") {
                val express = CronExpression {
                    minute = "15"
                    configHours = listOf(9, 20)
                    configDaysOfWeek = listOf(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI)
                }
                println(express)
            }

            Then("파싱 테스트") {
                // 1. 기본 시간 파싱 테스트
                val cron1 = CronExpression.parse("0 9 ? * * *")
                cron1.configHours shouldBe listOf(9)
                cron1.configDaysOfWeek shouldBe null
                cron1.toString() shouldBe "0 9 ? * * *"

                // 2. 여러 시간 파싱 테스트
                val cron2 = CronExpression.parse("0 9,18,21 ? * * *")
                cron2.configHours shouldBe listOf(9, 18, 21)
                cron2.configDaysOfWeek shouldBe null
                cron2.toString() shouldBe "0 9,18,21 ? * * *"

                // 3. 요일 파싱 테스트 (문자 형태)
                val cron3 = CronExpression.parse("0 10 ? * MON,TUE,WED,THU,FRI *")
                cron3.configHours shouldBe listOf(10)
                cron3.configDaysOfWeek shouldBe listOf(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI)
                cron3.toString() shouldBe "0 10 ? * MON,TUE,WED,THU,FRI *"

                // 4. 요일 파싱 테스트 (숫자 형태)
                val cron4 = CronExpression.parse("0 15 ? * 6,7 *")
                cron4.configHours shouldBe listOf(15)
                cron4.configDaysOfWeek shouldBe listOf(DayOfWeek.FRI, DayOfWeek.SAT)
                cron4.toString() shouldBe "0 15 ? * FRI,SAT *"

                // 5. 복합 테스트 (시간 + 요일)
                val cron5 = CronExpression.parse("0 9,18 ? * MON,WED,FRI *")
                cron5.configHours shouldBe listOf(9, 18)
                cron5.configDaysOfWeek shouldBe listOf(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI)
                cron5.toString() shouldBe "0 9,18 ? * MON,WED,FRI *"

                // 6. 모든 값이 * 인 경우
                val cron6 = CronExpression.parse("0 * ? * * *")
                cron6.configHours shouldBe null
                cron6.configDaysOfWeek shouldBe null

                // 7. 주말 테스트
                val cron7 = CronExpression.parse("0 10 ? * SAT,SUN *")
                cron7.configHours shouldBe listOf(10)
                cron7.configDaysOfWeek shouldBe listOf(DayOfWeek.SAT, DayOfWeek.SUN)
                cron7.configDaysOfWeek!!.all { it.isWeekend } shouldBe true

                // 8. 긴 형태 요일명 테스트
                val cron8 = CronExpression.parse("0 12 ? * MONDAY,WEDNESDAY,FRIDAY *")
                cron8.configDaysOfWeek shouldBe listOf(DayOfWeek.MON, DayOfWeek.WED, DayOfWeek.FRI)

                // 9. 파싱-toString 라운드트립 테스트
                val originalCron = "0 8,12,18 ? * MON,TUE,WED,THU,FRI *"
                val parsedCron = CronExpression.parse(originalCron)
                val regeneratedCron = parsedCron.toString()
                regeneratedCron shouldBe originalCron

                // 10. 잘못된 형식 테스트
                shouldThrow<IllegalArgumentException> {
                    CronExpression.parse("0 9 ? *") // 부족한 부분
                }

                shouldThrow<IllegalArgumentException> {
                    CronExpression.parse("0 25 ? * * *") // 잘못된 시간
                }

                "55 07,9 ? * 2-6 *".let {
                    val cron = CronExpression.parse(it)
                    cron.minute shouldBe "55"
                    cron.configHours shouldBe listOf(7, 9)
                    //월화수목금토
                    cron.configDaysOfWeek shouldBe listOf(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI)

                    val wanted = "55 7,9 ? * MON,TUE,WED,THU,FRI *"
                    cron.toString() shouldBe wanted

                    val cron2 = CronExpression.parse(it)
                    cron.configHours shouldBe cron2.configHours
                    cron.configDaysOfWeek shouldBe cron2.configDaysOfWeek
                }

                log.info { "크론 표현식 파싱 테스트 완료" }
            }
        }
    }

}

