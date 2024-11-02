package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

data class S3LogicFailEvent(val path: S3LogicPath, val inputData: S3LogicInput, val e: Exception)