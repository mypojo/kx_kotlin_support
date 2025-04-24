package net.kotlinx.aws.s3

import net.kotlinx.number.toSiText
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toKr01

fun List<S3Data>.printSimples() {
    listOf("FullPathDir", "파일명", "생성시간", "크기").toTextGridPrint {
        this.map {
            arrayOf(it.toFullPathDir(), it.fileName, it.lastModified?.toKr01(), it.size?.toSiText())
        }
    }
}
