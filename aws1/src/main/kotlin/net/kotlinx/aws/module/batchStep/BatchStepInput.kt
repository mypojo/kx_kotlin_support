package net.kotlinx.aws.module.batchStep

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.kotlinx.core.serial.SerialJsonCompanion
import net.kotlinx.core.serial.SerialJsonObj
import net.kotlinx.core.serial.SerialJsonSet
import java.util.*

/**
 * SFN 에 root로 들어가는 json 객체. 스키마 참조용임
 * SFN CDK에서 추가 설정을 해야 실제 코드로 전달된다
 * AWS_BATCH 나 CDK 설정 등과의 통합을 위해서 한번 감싼 형태로 전달됨
 * 그냥.. kotlin Serializable 함 써봤음
 * */
@Serializable
class BatchStepInput : SerialJsonObj {

    /** 기본적으로 Map 모드 */
    var mode: BatchStepMode = BatchStepMode.Map

    /** 초기화로 새로 채번. 리트라이시 오버라이드. */
    val sfnId: String = UUID.randomUUID().toString()

    /** 이게 있으면 재시도로 간주. 재시도이면 업로드 안함.  */
    var retrySfnId: String? = null

    /** 실제 데이터를 처리할 디렉토리를 구하기 위한 ID */
    val targetSfnId: String
        get() = retrySfnId ?: sfnId


    //==================================================== BatchStepMode.List 에 사용 ======================================================

    /**
     * 최대 동시성. 최대 1000개
     * 주의!! 상대 서버가 받아줄 수 있을 만큼만 보내기.  크롤링의 경우 너무 많으면 역효과가 남.
     * 응답속도(람다비용)이 가장 중요한 최적화 요소임.
     * 타임아웃 4초 정도 주고 간헐적으로 오류가 날 정도가 적당함
     *  */
    var maxConcurrency: Int = 100

    /**
     * 대기시간 (초). 정수로 입력해야한다.
     * 로컬에서 4초 -> 15초 정도 설정해도 랜덤하게 돌리다보면 5초 까지도 떨어짐. 2배 잡으면 될듯.
     * 주의!! 타임아웃 난다고 길게 줄 필요 없음. WAF를 회피할 정도로 입력하면 됨
     *  */
    var waitSeconds: Int = 6

    /** 콜드 스타트시 대기시간 (초). 정수로 입력해야한다.  */
    var waitColdstartSeconds: Int = waitSeconds + 10

    override fun toJson(): String = SerialJsonSet.KSON_OTHER.encodeToString(this)

    companion object : SerialJsonCompanion {

        override fun parseJson(json: String): BatchStepInput = SerialJsonSet.KSON_OTHER.decodeFromString<BatchStepInput>(json)

    }

}