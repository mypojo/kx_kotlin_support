package net.kotlinx.aws.batch

import net.kotlinx.core.Kdsl

/**
 * 일단 작업대기.. 양이 많다.
 * 그냥 뮤터블 한 태그 쓰는걸로..
 * */
class BatchJobDefinition {

    @Kdsl
    constructor(block: BatchJobDefinition.() -> Unit = {}) {
        apply(block)
    }


}