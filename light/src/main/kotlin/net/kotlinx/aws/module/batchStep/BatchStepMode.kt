package net.kotlinx.aws.module.batchStep


/**
 * https://www.notion.so/mypojo/Batch-Step-8ccf05e6f2e14d37998b050114945d63
 * */
enum class BatchStepMode {

    /**
     * CdkSfnMapInline 사용
     * 파라메터에 포함(미리 로드)된 S3 청크들을 최대 40개씩 동시실행
     *  */
    MAP_INLINE,

    /**
     * 실시간으로 특정 경로의 S3를 리스트 조회해서 최대 X개 동시실행 & wait
     * */
    LIST,
    ;


}