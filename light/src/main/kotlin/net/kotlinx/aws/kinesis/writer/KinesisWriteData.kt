package net.kotlinx.aws.kinesis.writer

import net.kotlinx.json.gson.GsonData


/**
 * 키네시스 쓰기 간편버전
 * */
data class KinesisWriteData(
    /**
     * 파티션 키
     * 고정값을 리턴하는경우
     * 1. 모든값은 고정된 샤드로 전송된다  = 로드밸런싱 되지 않음
     * 2. 하지만 동일 샤드에서는 순서가 유지됨
     * 대량의 데이터 입력시 "키워드" 처럼 분산된 값을 사용해야함
     *  */
    val partitionKey: String,
    val data: GsonData,
)