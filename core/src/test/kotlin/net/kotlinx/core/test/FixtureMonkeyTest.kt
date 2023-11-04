//package net.kotlinx.core.test
//
//import com.navercorp.fixturemonkey.FixtureMonkey
//import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
//import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
//import com.navercorp.fixturemonkey.kotlin.minSizeExp
//import com.navercorp.fixturemonkey.kotlin.setExp
//import org.junit.jupiter.api.Test
//import java.time.Instant
//
///**
// *  네이버 픽스쳐몽키 테스트
// */
//class FixtureMonkeyTest : TestRoot() {
//
//    data class Order (
//        val id: Long,
//
//        val orderNo: String,
//
//        val productName: String,
//
//        val quantity: Int,
//
//        val price: Long,
//
//        val items: List<String>,
//
//        val orderedAt: Instant
//    )
//
//    @Test
//    fun sampleTest() {
//        // given
//        val fixture = FixtureMonkey.builder().plugin(KotlinPlugin()).build()
//        for (i:Int in 1..5) {
//            println(fixture.giveMeOne(Order::class.java))
//        }
//    }
//
//    @Test
//    fun test() {
//        // given
//        val sut = FixtureMonkey.builder().plugin(KotlinPlugin()).build()
//
//        // when
//        val actual = sut.giveMeBuilder<Order>()
//            .setExp(Order::orderNo, "1")
//            .setExp(Order::productName, "Line Sally")
//            .minSizeExp(Order::items, 1)
//            .sample()
//
//        // then
////        then(actual.orderNo).isEqualTo("1")
////        then(actual.productName).isEqualTo("Line Sally")
////        then(actual.items).hasSizeGreaterThanOrEqualTo(1)
//
//    }
//
//}