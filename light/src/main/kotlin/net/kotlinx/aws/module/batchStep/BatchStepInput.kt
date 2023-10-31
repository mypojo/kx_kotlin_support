package net.kotlinx.aws.module.batchStep

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.kotlinx.core.serial.SerialJsonCompanion
import net.kotlinx.core.serial.SerialJsonObj
import net.kotlinx.core.serial.SerialJsonSet

@DslMarker
annotation class BatchStepInputDsl

/**
 * SFN 실행시 이거 거대로 입력 -> 이게 json으로 변환되서 SFN 실행 -> SFN(람다/배치) 에서 수신해서 다시 객체화
 * SFN CDK에서 추가 설정을 해야 실제 코드로 전달된다
 * 주의!!! SFN이 읽는 파라메터는 body에 둬도 되지만, 실제 코드에 전달할 파라메터는 2뎁스로 해야햠.
 * #1 람다(CdkSfnMapInline)   -> 그 자체($)로 파라메터 매핑이 가능하지만
 * #2 AWS BATCH(CdkSfnBatch) -> args 로 입력받는 형태라서, 2뎁스로 만들어야 매핑 가능함 (1뎁스 되는지는?? 몰라. 될거 같지만 일단 2뎁스로 감)
 * */
@Serializable
class BatchStepInput : SerialJsonObj {

    @BatchStepInputDsl
    constructor(block: BatchStepInput.() -> Unit) {
        apply(block)
    }

    /** 모드 */
    var mode: BatchStepMode = BatchStepMode.MAP_INLINE

    /** SFN에서 직접 전달해주는 메소드 */
    var method: String? = null

    /** LIST 용 옵션 */
    lateinit var option: BatchStepOption

    //==================================================== 변환 세트 ======================================================

    override fun toJson(): String = SerialJsonSet.KSON.encodeToString(this)

    companion object : SerialJsonCompanion {
        override fun parseJson(json: String): BatchStepInput = SerialJsonSet.KSON.decodeFromString<BatchStepInput>(json)
    }

}




