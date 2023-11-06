package net.kotlinx.aws.module.batchStep


/**
 * https://www.notion.so/mypojo/Batch-Step-8ccf05e6f2e14d37998b050114945d63
 * */
enum class BatchStepMode {

    /**
     * CdkSfnMapInline 사용
     * 특정 S3 디렉토리를 읽서 전체 파일을 다음 스텝(MapInline)으로 전달함
     * 파라메터에 포함된 S3 청크들을 최대 40개씩 동시실행
     *  */
    MAP_INLINE,

    /**
     * 커스텀 람다로직 사용
     * 특정 S3 디렉토리를 매번 실시간으로 리스팅해서 X개를 람다 실행 -> wait -> .. 반복
     * */
    LIST,
    ;


}