package net.kotlinx.aws.module.batchStep

import net.kotlinx.aws.AwsNaming
import net.kotlinx.core.gson.GsonData
import kotlin.reflect.KClass

/**
 * 전체 전달되는 event Map을 래핑.
 * 범용적으로 사용하기 위해서 객체 매핑을 사용하지 않는다.
 * 참고!! json이 Any로 입력되면  LinkedHashMap 으로 매핑된다.
 * */
internal class BatchStepContext(event: Map<String, Any>) {

    val gsonData: GsonData = GsonData.fromObj(event)

    /**
     * 인풋 중에서 option
     * 결과도 여기에 들어감
     * 반복 실행일경우 처음이면 empty
     *  */
    val option: GsonData = gsonData[AwsNaming.option]

    val mode: BatchStepMode? by lazy { option["mode"].str?.let { BatchStepMode.valueOf(it) } }

    /** 옵션을 객체화 시킴 */
    val optionInput: BatchStepInput by lazy { BatchStepInput.parse(option.toString()) } //없는 필드는 무시

    //==================================================== 각 단계 ======================================================

    /** 옵션을 객체화 시킴 */
    inline operator fun <reified T> get(clazz: KClass<*>): T {
        val json = option[clazz.simpleName!!][AwsNaming.body].toString()
        return GsonData.parse(json).fromJson<T>()
    }
}