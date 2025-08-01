package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.model.Record

typealias KinesisReaderRecordHandler = suspend (String, List<Record>) -> Unit
typealias KinesisReaderEmptyHandler = suspend (String) -> Unit