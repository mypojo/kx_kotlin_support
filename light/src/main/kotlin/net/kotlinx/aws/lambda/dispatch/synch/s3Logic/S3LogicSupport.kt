package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import org.koin.core.module.Module
import org.koin.core.qualifier.named


/** S3Logic 등록 (늦은 초기화함) */
fun Module.registS3Logic(id: String, block: S3Logic.() -> Unit) {
    single(named(id)) {
        S3Logic().apply(block).apply {
            this.id = id
        }
    }
}