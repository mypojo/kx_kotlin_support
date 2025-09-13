package net.kotlinx.domain.batchStep

import net.kotlinx.aws.AwsNaming
import net.kotlinx.json.gson.GsonData


/**
 * 함수 링크 : https://docs.aws.amazon.com/step-functions/latest/dg/intrinsic-functions.html
 * */
object BatchStepUtil {

    /** 배치스탭 기본 규격에 맞게 옵션을 래핑해준다 */
    fun wrap(option: Any): GsonData = GsonData.obj { put(AwsNaming.OPTION, GsonData.fromObj(option)) }

}


