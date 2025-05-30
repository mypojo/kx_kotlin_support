//package net.kotlinx.validation.konform
//
//import io.konform.validation.ValidationResult
//import net.kotlinx.string.toTextGridPrint
//
//
///** 간단 결과 출력 */
//fun ValidationResult<*>.printSimple() {
//    val results = this
//    listOf("이름", "실패사유").toTextGridPrint {
//        results.errors.map {
//            arrayOf(it.dataPath.removePrefix("."), it.message)
//        }
//    }
//}