package net.kotlinx.domain.batchStep

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kotlinx.core.Kdsl
import net.kotlinx.json.serial.SerialJsonSet
import net.kotlinx.json.serial.SerialParseJson
import net.kotlinx.json.serial.SerialToJson


/**
 * SFN 실행시 이거 그대로 입력 -> 이게 json으로 변환되서 SFN 실행 -> SFN(람다/배치) 에서 수신해서 다시 객체화
 * SFN CDK에서 추가 설정을 해야 실제 코드로 전달된다
 * 주의!!! SFN이 읽는 파라메터는 body에 둬도 되지만, 실제 코드에 전달할 파라메터는 2뎁스로 해야햠.
 * #1 람다(CdkSfnMapInline)   -> 그 자체($)로 파라메터 매핑이 가능하지만
 * #2 AWS BATCH(CdkSfnBatch) -> args 로 입력받는 형태라서, 2뎁스로 만들어야 매핑 가능함 (1뎁스 되는지는?? 몰라. 될거 같지만 일단 2뎁스로 감)
 * */
@Serializable
class BatchStepParameter : SerialToJson {

    /**
     * 주의!! 실제 BatchStepParameter 에는 별로 넣을게 없다.
     * 따라서 블록에는 BatchStepOption 를 사용하니 주의할것!!
     * */
    @Kdsl
    constructor(method: String? = null, block: BatchStepOption.() -> Unit) {
        this.method = method
        option = BatchStepOption(block)
    }

    /** SFN에서 직접 전달해주는 메소드. 혹시 받을 일이 있을까 해서 넣어둠 */
    var method: String? = null

    /**
     * 실제 옵션
     * CDK의 $.option 과 스키마가 일치해야 한다
     *  */
    val option: BatchStepOption

    //==================================================== 변환 세트 ======================================================

    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)

    companion object Parse : SerialParseJson {
        override fun parseJson(json: String): BatchStepParameter = SerialJsonSet.JSON_OTHER.decodeFromString<BatchStepParameter>(json)
    }

}




