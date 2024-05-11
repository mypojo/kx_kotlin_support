package net.kotlinx.aws.s3

import aws.sdk.kotlin.services.s3.model.ListObjectsV2Response
import kotlinx.coroutines.flow.Flow
import net.kotlinx.concurrent.collectToList


/** 간단 도우미 */
suspend fun Flow<ListObjectsV2Response>.toList(): List<String> = this.collectToList { v -> v.contents?.map { it.key!! } ?: emptyList() }.flatten()
