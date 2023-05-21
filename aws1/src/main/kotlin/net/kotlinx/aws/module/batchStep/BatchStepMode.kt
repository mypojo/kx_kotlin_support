package net.kotlinx.aws.module.batchStep


/**
 * https://www.notion.so/mypojo/Batch-Step-8ccf05e6f2e14d37998b050114945d63
 * */
enum class BatchStepMode {

    /** 특정 경로의 S3 청크를 최대 40개씩 동시실행  */
    Map,

    /** 특정 경로의 S3를 리스트해서 최대 X개 동시실행 & wait */
    List,
    ;


}